package com.unblu.usonarstatus.service;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.ExternalStatusCheck;
import org.gitlab4j.api.models.ExternalStatusCheckResult;
import org.gitlab4j.api.models.ExternalStatusCheckStatus.Status;
import org.gitlab4j.api.models.MergeRequest;
import org.jboss.logging.Logger;

import com.unblu.usonarstatus.model.GitLabEventSimple;
import com.unblu.usonarstatus.model.SonarEventSimple;
import com.unblu.usonarstatus.model.USonarStatusResult;
import com.unblu.usonarstatus.model.USonarStatusResult.Source;
import com.unblu.usonarstatus.sonar.PullRequest;
import com.unblu.usonarstatus.sonar.PullRequestsResponse;
import com.unblu.usonarstatus.sonar.SonarClient;

import io.quarkus.info.BuildInfo;
import io.quarkus.info.GitInfo;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.core.eventbus.EventBus;

@ApplicationScoped
public class Service {
	private static final Logger LOG = Logger.getLogger(Service.class);

	static final String PROJECT_PREFIX = "project_";
	static final int PROJECT_PREFIX_LENGTH = 8;

	public static final String GITLAB_EVENT = "gitlab-event";
	public static final String SONAR_EVENT = "sonar-event";

	@Inject
	EventBus resultsBus;

	@ConfigProperty(name = "gitlab.external.check.name", defaultValue = "SONAR")
	String externalStatusCheckName;

	@ConfigProperty(name = "branch.bypass")
	Optional<String> bypassBranchPattern;

	@ConfigProperty(name = "gitlab.host", defaultValue = "https://gitlab.com")
	String gitLabHost;

	@ConfigProperty(name = "gitlab.api.token")
	String apiToken;

	@ConfigProperty(name = "gitlab.external.check.url")
	String externalCheckUrl;

	@ConfigProperty(name = "sonarqube.host", defaultValue = "https://sonarcloud.io")
	String sonarHost;

	@ConfigProperty(name = "report.mode.approval", defaultValue = "true")
	boolean reportModeApproval;

	@ConfigProperty(name = "report.mode.approver.name", defaultValue = "u-sonar-status")
	String reportModeApproverName;

	@Inject
	GitInfo gitInfo;

	@Inject
	BuildInfo buildInfo;

	private GitLabApi gitlab;

	private SonarClient sonarClient;

	@PostConstruct
	void init() throws GitLabApiException {
		gitlab = new GitLabApi(gitLabHost, apiToken);
		sonarClient = RestClientBuilder.newBuilder()
				.baseUri(URI.create(sonarHost))
				.build(SonarClient.class);
	}

	@Blocking // Will be called on a worker thread
	@ConsumeEvent(GITLAB_EVENT)
	public USonarStatusResult handleGitLabEvent(GitLabEventSimple event) {
		LOG.infof("GitlabEvent: '%s' | Starting handling GitLab event %s",
				event.getGitlabEventUUID(), event);

		String gitlabEventUUID = event.getGitlabEventUUID();

		USonarStatusResult result = createResult(gitlabEventUUID, null, Source.GITLAB);

		if (Objects.equals(event.getExternalStatusCheckName(), externalStatusCheckName)) {
			Param p = new Param();
			p.logPrefix = "GitlabEvent: '" + event.getGitlabEventUUID() + "'";
			p.gitLabProjectId = event.getProjectId();
			p.gitLabMergeRequestIid = event.getMergeRequestIid();
			p.gitLabMergeRequestSha = event.getMergeRequestLastCommitSha();
			p.gitLabExternalStatusCheckId = event.getExternalStatusCheckId();

			if (bypassBranchPattern.isPresent()
					&& event.getMergeRequestSourceBranch().matches(bypassBranchPattern.get())) {
				setExternalCheckStatus(result, p, Status.PASSED);
			} else {
				String projectKey = PROJECT_PREFIX + event.getProjectId();
				String pullRequestKey = "" + event.getMergeRequestIid();
				PullRequestsResponse response = sonarClient.listPullRequests(projectKey);
				Optional<PullRequest> findPullRequest = findPullRequest(response, pullRequestKey);
				if (findPullRequest.isPresent()) {
					PullRequest pr = findPullRequest.get();
					LOG.infof("GitlabEvent: '%s' | Found Sonar pull request %s",
							event.getGitlabEventUUID(), pr);
					if (pr.getStatus() != null) {
						p.sonarQualityGateStatus = pr.getStatus().getQualityGateStatus();
					}
					if (pr.getCommit() != null) {
						p.sonarRevision = pr.getCommit().getSha();
					}
					setExternalCheckStatusIfPossible(result, p);
				} else {
					LOG.infof("GitlabEvent: '%s' | Skipping event because could not retrieve the status in Sonar for projectKey '%s' and pullRequest '%s'",
							event.getGitlabEventUUID(), projectKey, pullRequestKey);
				}
			}
		} else {
			LOG.warnf("GitlabEvent: '%s' | Skipping event because unexpected external status check name '%s'",
					event.getGitlabEventUUID(), event.getExternalStatusCheckName());
		}
		LOG.infof("GitlabEvent: '%s' | Finished handling GitLab event with result %s",
				event.getGitlabEventUUID(), result);
		return result;
	}

	private Optional<PullRequest> findPullRequest(PullRequestsResponse response, String pullRequestKey) {
		List<PullRequest> pullRequests = response.getPullRequests();
		if (pullRequests == null) {
			return Optional.empty();
		}
		return pullRequests.stream()
				.filter(p -> Objects.equals(p.getKey(), pullRequestKey))
				.findAny();
	}

	@Blocking // Will be called on a worker thread
	@ConsumeEvent(SONAR_EVENT)
	public USonarStatusResult handleSonarEvent(SonarEventSimple event) {
		LOG.infof("SonarEvent: '%s', taskId '%s' | Starting handling Sonar event %s",
				event.getSonarEventUUID(), event.getTaskId(), event);
		USonarStatusResult result = createResult(null, event.getSonarEventUUID(), Source.SONAR);
		result.setSonarTaskId(event.getTaskId());
		if (sonarEventIsRelevant(event)) {
			Long gitLabProjectId = toGitLabProjectId(event);
			Long gitLabMergeRequestIid = toGitLabMergeRequestIid(event);
			result.setGitlabProjectId(gitLabProjectId);
			result.setGitlabMergeRequestIid(gitLabMergeRequestIid);
			MergeRequest mr = getGitLabMr(event, result, gitLabProjectId, gitLabMergeRequestIid);
			if (mr != null) {
				Param p = new Param();
				p.logPrefix = "SonarEvent: '" + event.getSonarEventUUID() + "', taskId '" + event.getTaskId() + "'";
				p.gitLabProjectId = gitLabProjectId;
				p.gitLabMergeRequestIid = gitLabMergeRequestIid;
				p.gitLabMergeRequestSha = mr.getSha();
				p.sonarQualityGateStatus = event.getQualityGateStatus();
				p.sonarRevision = event.getRevision();
				if (reportModeApproval) {
					setMrApprovalIfPossible(event, result, p);
				} else {
					setExternalCheckStatusIfPossible(result, p);
				}
			}
		}
		LOG.infof("SonarEvent: '%s', taskId '%s' | Finished handling Sonar event with result %s",
				event.getSonarEventUUID(), event.getTaskId(), result);
		return result;
	}

	private void setMrApprovalIfPossible(SonarEventSimple event, USonarStatusResult result, Param param) {
		String revision = param.sonarRevision;
		if (Objects.equals(param.gitLabMergeRequestSha, revision)) {
			Status status = toGitLabExternalCheckStatus(param.sonarQualityGateStatus);
			if (status != null) {
				setMrApproval(event, result, param, status);
			} else {
				LOG.warnf("%s | Skipping event because of unexpected quality gate status: '%s'",
						param.logPrefix, param.sonarQualityGateStatus);
			}
		} else {
			LOG.infof("%s | Skipping event because head of merge request '%s' in project '%s' is '%s' and does not match the revision in Sonar '%s'",
					param.logPrefix, param.gitLabMergeRequestIid, param.gitLabProjectId, param.gitLabMergeRequestSha, revision);
		}
	}

	private void setExternalCheckStatusIfPossible(USonarStatusResult result, Param param) {
		String revision = param.sonarRevision;
		if (Objects.equals(param.gitLabMergeRequestSha, revision)) {
			Status gitLabExternalCheckStatus = toGitLabExternalCheckStatus(param.sonarQualityGateStatus);
			if (gitLabExternalCheckStatus != null) {
				setExternalCheckStatus(result, param, gitLabExternalCheckStatus);
			} else {
				LOG.warnf("%s | Skipping event because of unexpected quality gate status: '%s'",
						param.logPrefix, param.sonarQualityGateStatus);
			}
		} else {
			LOG.infof("%s | Skipping event because head of merge request '%s' in project '%s' is '%s' and does not match the revision in Sonar '%s'",
					param.logPrefix, param.gitLabMergeRequestIid, param.gitLabProjectId, param.gitLabMergeRequestSha, revision);
		}
	}

	private void setExternalCheckStatus(USonarStatusResult result, Param param, Status gitLabExternalCheckStatus) {
		result.setGitlabExternalStatusCheckStatus(gitLabExternalCheckStatus);
		Long externalStatusCheckId = getOrCreateGitLabExternalCheckId(result, param);
		result.setGitlabExternalStatusCheckId(externalStatusCheckId);
		if (externalStatusCheckId != null) {
			ExternalStatusCheckResult response = setGitLabExternalCheck(param, result, param.gitLabProjectId, param.gitLabMergeRequestIid, param.gitLabMergeRequestSha, gitLabExternalCheckStatus, externalStatusCheckId);
			if (response != null) {
				mapFromResponse(result, response);
			}
		}
	}

	private void setMrApproval(SonarEventSimple event, USonarStatusResult result, Param param, Status approveStatus) {
		MergeRequest mrWithApprovals = getMrApprovals(event, result, param.gitLabProjectId, param.gitLabMergeRequestIid);
		if (mrWithApprovals == null) {
			return;
		}

		MergeRequest mergeRequest = null;
		boolean approved = mrWithApprovals.getApprovedBy().stream().anyMatch(user -> user.getName().contentEquals(reportModeApproverName));
		if (approveStatus == Status.PASSED && !approved) {
			mergeRequest = approveMr(event, result, param.gitLabProjectId, param.gitLabMergeRequestIid);
		} else if (approveStatus == Status.FAILED && approved) {
			mergeRequest = unapproveMr(event, result, param.gitLabProjectId, param.gitLabMergeRequestIid);
		}

		if (mergeRequest != null) {
			result.setGitlabMergeRequestIid(mergeRequest.getIid());
			result.setGitlabProjectId(mergeRequest.getProjectId());
		}
	}

	private static class Param {
		String logPrefix;

		public Long gitLabProjectId;
		public Long gitLabMergeRequestIid;
		public String gitLabMergeRequestSha;
		public Long gitLabExternalStatusCheckId;

		public String sonarQualityGateStatus;
		public String sonarRevision;

	}

	static void mapFromResponse(USonarStatusResult result, ExternalStatusCheckResult response) {
		result.setGitlabExternalStatusCheckStatusId(response.getId());
		MergeRequest mergeRequest = response.getMergeRequest();
		if (mergeRequest != null) {
			result.setGitlabMergeRequestIid(mergeRequest.getIid());
			result.setGitlabProjectId(mergeRequest.getProjectId());
		}
		ExternalStatusCheck externalStatusCheck = response.getExternalStatusCheck();
		if (externalStatusCheck != null) {
			result.setGitlabProjectId(externalStatusCheck.getProjectId());
			result.setGitlabExternalStatusCheckId(externalStatusCheck.getId());
		}
	}

	static boolean sonarEventIsRelevant(SonarEventSimple event) {
		if (!"PULL_REQUEST".equals(event.getBranchType())) {
			LOG.infof("SonarEvent: '%s', taskId '%s' | Skipping event because of wrong branch type value: '%s'",
					event.getSonarEventUUID(), event.getTaskId(), event.getBranchType());
			return false;
		}
		String branchName = event.getBranchName();
		if (branchName == null || !branchName.matches("[0-9]+")) {
			LOG.infof("SonarEvent: '%s', taskId '%s' | Skipping event because of unexpected branch name value: '%s'",
					event.getSonarEventUUID(), event.getTaskId(), branchName);
			return false;
		}
		String projectKey = event.getProjectKey();
		if (projectKey == null || !projectKey.matches(PROJECT_PREFIX + "[0-9]+")) {
			LOG.infof("SonarEvent: '%s', taskId '%s' | Skipping event because of unexpected project key value: '%s'",
					event.getSonarEventUUID(), event.getTaskId(), projectKey);
			return false;
		}
		return true;
	}

	static Long toGitLabProjectId(SonarEventSimple event) {
		String value = event.getProjectKey().substring(PROJECT_PREFIX_LENGTH);
		return Long.valueOf(value);
	}

	static Long toGitLabMergeRequestIid(SonarEventSimple event) {
		return Long.valueOf(event.getBranchName());
	}

	static Status toGitLabExternalCheckStatus(String qualityGateStatus) {
		if ("OK".equals(qualityGateStatus)) {
			return Status.PASSED;
		} else if ("ERROR".equals(qualityGateStatus)) {
			return Status.FAILED;
		}
		return null;
	}

	private MergeRequest getGitLabMr(SonarEventSimple event, USonarStatusResult result, Long gitLabProjectId, Long gitLabMergeRequestIid) {
		try {
			return gitlab.getMergeRequestApi().getMergeRequest(gitLabProjectId, gitLabMergeRequestIid);
		} catch (GitLabApiException e) {
			LOG.warnf(e, "SonarEvent: '%s', taskId '%s' | Can not get Merge Request '%d' in GitLab project '%d'",
					event.getSonarEventUUID(), event.getTaskId(), gitLabMergeRequestIid, gitLabProjectId);
			result.setError(e.getMessage());
		}
		return null;
	}

	private MergeRequest approveMr(SonarEventSimple event, USonarStatusResult result, Long gitLabProjectId, Long gitLabMergeRequestIid) {
		try {
			return gitlab.getMergeRequestApi().approveMergeRequest(gitLabProjectId, gitLabMergeRequestIid, null);
		} catch (GitLabApiException e) {
			LOG.warnf(e, "SonarEvent: '%s', taskId '%s' | Cannot approve Merge Request '%d' in GitLab project '%d'",
					event.getSonarEventUUID(), event.getTaskId(), gitLabMergeRequestIid, gitLabProjectId);
			result.setError(e.getMessage());
		}
		return null;
	}

	private MergeRequest unapproveMr(SonarEventSimple event, USonarStatusResult result, Long gitLabProjectId, Long gitLabMergeRequestIid) {
		try {
			return gitlab.getMergeRequestApi().unapproveMergeRequest(gitLabProjectId, gitLabMergeRequestIid);
		} catch (GitLabApiException e) {
			LOG.warnf(e, "SonarEvent: '%s', taskId '%s' | Cannot unapprove Merge Request '%d' in GitLab project '%d'",
					event.getSonarEventUUID(), event.getTaskId(), gitLabMergeRequestIid, gitLabProjectId);
			result.setError(e.getMessage());
		}
		return null;
	}

	private MergeRequest getMrApprovals(SonarEventSimple event, USonarStatusResult result, Long gitLabProjectId, Long gitLabMergeRequestIid) {
		try {
			return gitlab.getMergeRequestApi().getMergeRequestApprovals(gitLabProjectId, gitLabMergeRequestIid);
		} catch (GitLabApiException e) {
			LOG.warnf(e, "SonarEvent: '%s', taskId '%s' | Cannot get approvals info for Merge Request '%d' in GitLab project '%d'",
					event.getSonarEventUUID(), event.getTaskId(), gitLabMergeRequestIid, gitLabProjectId);
			result.setError(e.getMessage());
		}
		return null;
	}

	private ExternalStatusCheckResult setGitLabExternalCheck(Param param, USonarStatusResult result, Long gitLabProjectId, Long gitLabMergeRequestIid, String revision, Status gitLabExternalCheckStatus, Long externalStatusCheckId) {
		try {
			return gitlab.getExternalStatusCheckApi().setStatusOfExternalStatusCheck(gitLabProjectId, gitLabMergeRequestIid, revision, externalStatusCheckId, gitLabExternalCheckStatus);
		} catch (GitLabApiException e) {
			LOG.warnf(e, "%s | Can not set external merge status to '%s' on merge request '%d' in GitLab project '%d' for revision '%s'",
					param.logPrefix, gitLabExternalCheckStatus, gitLabMergeRequestIid, gitLabProjectId, revision);
			result.setError(e.getMessage());
			return null;
		}
	}

	private Long getOrCreateGitLabExternalCheckId(USonarStatusResult result, Param param) {
		if (param.gitLabExternalStatusCheckId != null) {
			return param.gitLabExternalStatusCheckId;
		}
		List<ExternalStatusCheck> checks;
		try {
			checks = gitlab.getExternalStatusCheckApi().getExternalStatusChecks(param.gitLabProjectId);
		} catch (GitLabApiException e) {
			LOG.warnf(e, "%s | Can not read external status checks in GitLab project '%d'",
					param.logPrefix, param.gitLabProjectId);
			result.setError(e.getMessage());
			return null;
		}
		Optional<ExternalStatusCheck> find = checks.stream()
				.filter(c -> Objects.equals(externalStatusCheckName, c.getName()))
				.findAny();
		if (find.isPresent()) {
			ExternalStatusCheck check = find.get();
			if (Objects.equals(externalCheckUrl, check.getExternalUrl())) {
				return check.getId();
			} else {
				try {
					ExternalStatusCheck updatedCheck = gitlab.getExternalStatusCheckApi().updateExternalStatusCheck(param.gitLabProjectId, check.getId(), externalStatusCheckName, externalCheckUrl, null);
					return updatedCheck.getId();
				} catch (GitLabApiException e) {
					LOG.warnf(e, "%s | Can not update external status id '%d' checks in GitLab project '%d' with name '%s' and url '%s'",
							param.logPrefix, check.getId(), param.gitLabProjectId, externalStatusCheckName, externalCheckUrl);
					result.setError(e.getMessage());
					return null;
				}
			}
		}
		try {
			ExternalStatusCheck createdCheck = gitlab.getExternalStatusCheckApi().createExternalStatusCheck(param.gitLabProjectId, externalStatusCheckName, externalCheckUrl, null);
			return createdCheck.getId();
		} catch (GitLabApiException e) {
			LOG.warnf(e, "%s | Can not create external status checks in GitLab project '%d'",
					param.logPrefix, param.gitLabProjectId);
			result.setError(e.getMessage());
			return null;
		}
	}

	public USonarStatusResult createResult(String gitlabEventUUID, String sonarEventUUID, Source source) {
		USonarStatusResult result = new USonarStatusResult();
		result.setGitlabEventUUID(gitlabEventUUID);
		result.setSonarEventUUID(sonarEventUUID);
		result.setSource(source);
		result.setBuildCommit(gitInfo.latestCommitId().substring(7));
		result.setBuildTimestamp(buildInfo.time().toString());
		return result;
	}
}

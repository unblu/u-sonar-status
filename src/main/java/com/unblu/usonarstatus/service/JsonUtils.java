package com.unblu.usonarstatus.service;

import org.gitlab4j.api.webhook.EventCommit;
import org.gitlab4j.api.webhook.EventExternalStatusCheck;
import org.gitlab4j.api.webhook.EventProject;
import org.gitlab4j.api.webhook.ExternalStatusCheckEvent;
import org.gitlab4j.api.webhook.MergeRequestEvent.ObjectAttributes;

import com.unblu.usonarstatus.model.GitLabEventSimple;
import com.unblu.usonarstatus.model.SonarEventSimple;
import com.unblu.usonarstatus.sonar.Branch;
import com.unblu.usonarstatus.sonar.Project;
import com.unblu.usonarstatus.sonar.QualityGate;
import com.unblu.usonarstatus.sonar.SonarEvent;

public class JsonUtils {

	public static GitLabEventSimple toGitLabEventSimple(ExternalStatusCheckEvent externalStatusCheck, String gitlabEventUUID) {
		GitLabEventSimple result = new GitLabEventSimple();
		result.setGitlabEventUUID(gitlabEventUUID);

		ObjectAttributes objectAttributes = externalStatusCheck.getObjectAttributes();
		if (objectAttributes == null) {
			throw new IllegalStateException(String.format("GitlabEvent: '%s' | NoteEvent.ObjectAttributes is null. Possible cause: error in the deserializing process", gitlabEventUUID));
		}

		EventProject project = externalStatusCheck.getProject();
		if (project != null) {
			result.setProjectId(project.getId());
		}

		ObjectAttributes attributes = externalStatusCheck.getObjectAttributes();
		if (project != null) {
			result.setMergeRequestIid(attributes.getIid());
			result.setMergeRequestSourceBranch(attributes.getSourceBranch());
			EventCommit lastCommit = attributes.getLastCommit();
			if (lastCommit != null) {
				result.setMergeRequestLastCommitSha(lastCommit.getId());
			}
		}

		EventExternalStatusCheck externalApprovalRule = externalStatusCheck.getExternalApprovalRule();
		if (externalApprovalRule != null) {
			result.setExternalStatusCheckId(externalApprovalRule.getId());
			result.setExternalStatusCheckName(externalApprovalRule.getName());
			result.setExternalStatusCheckUrl(externalApprovalRule.getExternalUrl());
		}
		return result;
	}

	public static SonarEventSimple toSonarEventSimple(SonarEvent sonarEvent, String sonarEventUUID) {
		SonarEventSimple result = new SonarEventSimple();
		result.setSonarEventUUID(sonarEventUUID);
		result.setTaskId(sonarEvent.getTaskId());
		result.setRevision(sonarEvent.getRevision());

		Project project = sonarEvent.getProject();
		if (project != null) {
			result.setProjectKey(project.getKey());
		}

		Branch branch = sonarEvent.getBranch();
		if (branch != null) {
			result.setBranchName(branch.getName());
			result.setBranchType(branch.getType());
		}

		QualityGate qualityGate = sonarEvent.getQualityGate();
		if (qualityGate != null) {
			result.setQualityGateStatus(qualityGate.getStatus());
		}
		return result;
	}

	private JsonUtils() {
	}
}

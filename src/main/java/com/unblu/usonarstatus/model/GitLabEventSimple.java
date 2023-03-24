package com.unblu.usonarstatus.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitLabEventSimple {

	@JsonProperty("project_id")
	private Long projectId;
	@JsonProperty("merge_request_iid")
	private Long mergeRequestIid;
	@JsonProperty("merge_request_source_branch")
	private String mergeRequestSourceBranch;
	@JsonProperty("merge_request_last_commit_sha")
	private String mergeRequestLastCommitSha;
	@JsonProperty("external_status_check_id")
	private Long externalStatusCheckId;
	@JsonProperty("external_status_check_name")
	private String externalStatusCheckName;
	@JsonProperty("external_status_check_url")
	private String externalStatusCheckUrl;
	@JsonProperty("gitlab_event_uuid")
	private String gitlabEventUUID;

	public GitLabEventSimple() {
		super();
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getMergeRequestIid() {
		return mergeRequestIid;
	}

	public void setMergeRequestIid(Long mergeRequestIid) {
		this.mergeRequestIid = mergeRequestIid;
	}

	public String getMergeRequestSourceBranch() {
		return mergeRequestSourceBranch;
	}

	public void setMergeRequestSourceBranch(String mergeRequestSourceBranch) {
		this.mergeRequestSourceBranch = mergeRequestSourceBranch;
	}

	public String getMergeRequestLastCommitSha() {
		return mergeRequestLastCommitSha;
	}

	public void setMergeRequestLastCommitSha(String mergeRequestLastCommitSha) {
		this.mergeRequestLastCommitSha = mergeRequestLastCommitSha;
	}

	public Long getExternalStatusCheckId() {
		return externalStatusCheckId;
	}

	public void setExternalStatusCheckId(Long externalStatusCheckId) {
		this.externalStatusCheckId = externalStatusCheckId;
	}

	public String getExternalStatusCheckName() {
		return externalStatusCheckName;
	}

	public void setExternalStatusCheckName(String externalStatusCheckName) {
		this.externalStatusCheckName = externalStatusCheckName;
	}

	public String getExternalStatusCheckUrl() {
		return externalStatusCheckUrl;
	}

	public void setExternalStatusCheckUrl(String externalStatusCheckUrl) {
		this.externalStatusCheckUrl = externalStatusCheckUrl;
	}

	public String getGitlabEventUUID() {
		return gitlabEventUUID;
	}

	public void setGitlabEventUUID(String gitlabEventUUID) {
		this.gitlabEventUUID = gitlabEventUUID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(externalStatusCheckId, externalStatusCheckName, externalStatusCheckUrl, gitlabEventUUID, mergeRequestIid, mergeRequestLastCommitSha, mergeRequestSourceBranch, projectId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GitLabEventSimple other = (GitLabEventSimple) obj;
		return Objects.equals(externalStatusCheckId, other.externalStatusCheckId) && Objects.equals(externalStatusCheckName, other.externalStatusCheckName) && Objects.equals(externalStatusCheckUrl, other.externalStatusCheckUrl) && Objects.equals(gitlabEventUUID, other.gitlabEventUUID) && Objects.equals(mergeRequestIid, other.mergeRequestIid) && Objects.equals(mergeRequestLastCommitSha, other.mergeRequestLastCommitSha) && Objects.equals(mergeRequestSourceBranch, other.mergeRequestSourceBranch) && Objects.equals(projectId, other.projectId);
	}

	@Override
	public String toString() {
		return "GitLabEventSimple [projectId=" + projectId + ", mergeRequestIid=" + mergeRequestIid + ", mergeRequestSourceBranch=" + mergeRequestSourceBranch + ", mergeRequestLastCommitSha=" + mergeRequestLastCommitSha + ", externalStatusCheckId=" + externalStatusCheckId + ", externalStatusCheckName=" + externalStatusCheckName + ", externalStatusCheckUrl=" + externalStatusCheckUrl + ", gitlabEventUUID=" + gitlabEventUUID + "]";
	}
}

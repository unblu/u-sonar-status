package com.unblu.usonarstatus.model;

import java.util.Objects;

import org.gitlab4j.api.models.ExternalStatusCheckStatus.Status;

import com.fasterxml.jackson.annotation.JsonProperty;

public class USonarStatusResult {

	@JsonProperty("build_commit")
	private String buildCommit;

	@JsonProperty("build_timestamp")
	private String buildTimestamp;

	@JsonProperty("gitlab_event_uuid")
	private String gitlabEventUUID;

	@JsonProperty("sonar_event_uuid")
	private String sonarEventUUID;

	@JsonProperty("sonar_task_id")
	private String sonarTaskId;

	@JsonProperty("source")
	private Source source;

	@JsonProperty("gitlab_project_id")
	private Long gitlabProjectId;

	@JsonProperty("gitlab_merge_request_iid")
	private Long gitlabMergeRequestIid;

	@JsonProperty("gitlab_external_status_check_id")
	private Long gitlabExternalStatusCheckId;

	@JsonProperty("gitlab_external_status_check_status")
	private Status gitlabExternalStatusCheckStatus;

	@JsonProperty("gitlab_external_status_check_status_id")
	private Long gitlabExternalStatusCheckStatusId;

	@JsonProperty("error")
	private String error;

	public enum Source {
		GITLAB,
		SONAR
	}

	public String getBuildCommit() {
		return buildCommit;
	}

	public void setBuildCommit(String buildCommit) {
		this.buildCommit = buildCommit;
	}

	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	public void setBuildTimestamp(String buildTimestamp) {
		this.buildTimestamp = buildTimestamp;
	}

	public String getGitlabEventUUID() {
		return gitlabEventUUID;
	}

	public void setGitlabEventUUID(String gitlabEventUUID) {
		this.gitlabEventUUID = gitlabEventUUID;
	}

	public String getSonarEventUUID() {
		return sonarEventUUID;
	}

	public void setSonarEventUUID(String sonarEventUUID) {
		this.sonarEventUUID = sonarEventUUID;
	}

	public String getSonarTaskId() {
		return sonarTaskId;
	}

	public void setSonarTaskId(String sonarTaskId) {
		this.sonarTaskId = sonarTaskId;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Long getGitlabProjectId() {
		return gitlabProjectId;
	}

	public void setGitlabProjectId(Long gitlabProjectId) {
		this.gitlabProjectId = gitlabProjectId;
	}

	public Long getGitlabMergeRequestIid() {
		return gitlabMergeRequestIid;
	}

	public void setGitlabMergeRequestIid(Long gitlabMergeRequestIid) {
		this.gitlabMergeRequestIid = gitlabMergeRequestIid;
	}

	public Long getGitlabExternalStatusCheckId() {
		return gitlabExternalStatusCheckId;
	}

	public void setGitlabExternalStatusCheckId(Long gitlabExternalStatusCheckId) {
		this.gitlabExternalStatusCheckId = gitlabExternalStatusCheckId;
	}

	public Status getGitlabExternalStatusCheckStatus() {
		return gitlabExternalStatusCheckStatus;
	}

	public void setGitlabExternalStatusCheckStatus(Status gitlabExternalStatusCheckStatus) {
		this.gitlabExternalStatusCheckStatus = gitlabExternalStatusCheckStatus;
	}

	public Long getGitlabExternalStatusCheckStatusId() {
		return gitlabExternalStatusCheckStatusId;
	}

	public void setGitlabExternalStatusCheckStatusId(Long gitlabExternalStatusCheckStatusId) {
		this.gitlabExternalStatusCheckStatusId = gitlabExternalStatusCheckStatusId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public int hashCode() {
		return Objects.hash(buildCommit, buildTimestamp, error, gitlabEventUUID, gitlabExternalStatusCheckId, gitlabExternalStatusCheckStatus, gitlabExternalStatusCheckStatusId, gitlabMergeRequestIid, gitlabProjectId, sonarEventUUID, sonarTaskId, source);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		USonarStatusResult other = (USonarStatusResult) obj;
		return Objects.equals(buildCommit, other.buildCommit) && Objects.equals(buildTimestamp, other.buildTimestamp) && Objects.equals(error, other.error) && Objects.equals(gitlabEventUUID, other.gitlabEventUUID) && Objects.equals(gitlabExternalStatusCheckId, other.gitlabExternalStatusCheckId) && gitlabExternalStatusCheckStatus == other.gitlabExternalStatusCheckStatus && Objects.equals(gitlabExternalStatusCheckStatusId, other.gitlabExternalStatusCheckStatusId) && Objects.equals(gitlabMergeRequestIid, other.gitlabMergeRequestIid) && Objects.equals(gitlabProjectId, other.gitlabProjectId) && Objects.equals(sonarEventUUID, other.sonarEventUUID) && Objects.equals(sonarTaskId, other.sonarTaskId) && source == other.source;
	}

	@Override
	public String toString() {
		return "USonarStatusResult [buildCommit=" + buildCommit + ", buildTimestamp=" + buildTimestamp + ", gitlabEventUUID=" + gitlabEventUUID + ", sonarEventUUID=" + sonarEventUUID + ", sonarTaskId=" + sonarTaskId + ", source=" + source + ", gitlabProjectId=" + gitlabProjectId + ", gitlabMergeRequestIid=" + gitlabMergeRequestIid + ", gitlabExternalStatusCheckId=" + gitlabExternalStatusCheckId + ", gitlabExternalStatusCheckStatus=" + gitlabExternalStatusCheckStatus + ", gitlabExternalStatusCheckStatusId=" + gitlabExternalStatusCheckStatusId + ", error=" + error + "]";
	}

}

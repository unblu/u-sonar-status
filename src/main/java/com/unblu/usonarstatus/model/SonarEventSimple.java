package com.unblu.usonarstatus.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SonarEventSimple {

	@JsonProperty("sonar_event_uuid")
	private String sonarEventUUID;

	@JsonProperty("task_id")
	private String taskId;

	@JsonProperty("revision")
	private String revision;

	@JsonProperty("project_key")
	private String projectKey;

	@JsonProperty("branch_name")
	private String branchName;

	@JsonProperty("branch_type")
	private String branchType;

	@JsonProperty("quality_gate_status")
	private String qualityGateStatus;

	public SonarEventSimple() {
		super();
	}

	public String getSonarEventUUID() {
		return sonarEventUUID;
	}

	public void setSonarEventUUID(String sonarEventUUID) {
		this.sonarEventUUID = sonarEventUUID;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getBranchType() {
		return branchType;
	}

	public void setBranchType(String branchType) {
		this.branchType = branchType;
	}

	public String getQualityGateStatus() {
		return qualityGateStatus;
	}

	public void setQualityGateStatus(String qualityGateStatus) {
		this.qualityGateStatus = qualityGateStatus;
	}

	@Override
	public int hashCode() {
		return Objects.hash(branchName, branchType, projectKey, qualityGateStatus, revision, sonarEventUUID, taskId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SonarEventSimple other = (SonarEventSimple) obj;
		return Objects.equals(branchName, other.branchName) && Objects.equals(branchType, other.branchType) && Objects.equals(projectKey, other.projectKey) && Objects.equals(qualityGateStatus, other.qualityGateStatus) && Objects.equals(revision, other.revision) && Objects.equals(sonarEventUUID, other.sonarEventUUID) && Objects.equals(taskId, other.taskId);
	}

	@Override
	public String toString() {
		return "SonarEventSimple [sonarEventUUID=" + sonarEventUUID + ", taskId=" + taskId + ", revision=" + revision + ", projectKey=" + projectKey + ", branchName=" + branchName + ", branchType=" + branchType + ", qualityGateStatus=" + qualityGateStatus + "]";
	}

}

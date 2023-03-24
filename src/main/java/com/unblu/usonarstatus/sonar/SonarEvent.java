package com.unblu.usonarstatus.sonar;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"serverUrl",
	"taskId",
	"status",
	"analysedAt",
	"revision",
	"changedAt",
	"project",
	"branch",
	"qualityGate",
	"properties"
})
public class SonarEvent {

	@JsonProperty("serverUrl")
	private String serverUrl;
	@JsonProperty("taskId")
	private String taskId;
	@JsonProperty("status")
	private String status;
	@JsonProperty("analysedAt")
	private String analysedAt;
	@JsonProperty("revision")
	private String revision;
	@JsonProperty("changedAt")
	private String changedAt;
	@JsonProperty("project")
	private Project project;
	@JsonProperty("branch")
	private Branch branch;
	@JsonProperty("qualityGate")
	private QualityGate qualityGate;
	@JsonProperty("properties")
	private Map<String, String> properties;

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAnalysedAt() {
		return analysedAt;
	}

	public void setAnalysedAt(String analysedAt) {
		this.analysedAt = analysedAt;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(String changedAt) {
		this.changedAt = changedAt;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public QualityGate getQualityGate() {
		return qualityGate;
	}

	public void setQualityGate(QualityGate qualityGate) {
		this.qualityGate = qualityGate;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public int hashCode() {
		return Objects.hash(analysedAt, branch, changedAt, project, properties, qualityGate, revision, serverUrl, status, taskId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SonarEvent other = (SonarEvent) obj;
		return Objects.equals(analysedAt, other.analysedAt) && Objects.equals(branch, other.branch) && Objects.equals(changedAt, other.changedAt) && Objects.equals(project, other.project) && Objects.equals(properties, other.properties) && Objects.equals(qualityGate, other.qualityGate) && Objects.equals(revision, other.revision) && Objects.equals(serverUrl, other.serverUrl) && Objects.equals(status, other.status) && Objects.equals(taskId, other.taskId);
	}

}
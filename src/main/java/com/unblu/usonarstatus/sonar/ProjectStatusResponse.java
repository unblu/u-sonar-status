package com.unblu.usonarstatus.sonar;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"projectStatus"
})
public class ProjectStatusResponse {

	@JsonProperty("projectStatus")
	private QualityGate projectStatus;

	public QualityGate getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(QualityGate projectStatus) {
		this.projectStatus = projectStatus;
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectStatus);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectStatusResponse other = (ProjectStatusResponse) obj;
		return Objects.equals(projectStatus, other.projectStatus);
	}

	@Override
	public String toString() {
		return "ProjectStatusResponse [projectStatus=" + projectStatus + "]";
	}
}
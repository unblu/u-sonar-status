package com.unblu.usonarstatus.sonar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"qualityGateStatus",
	"bugs",
	"vulnerabilities",
	"codeSmells"
})
public class Status {

	@JsonProperty("qualityGateStatus")
	private String qualityGateStatus;
	@JsonProperty("bugs")
	private Integer bugs;
	@JsonProperty("vulnerabilities")
	private Integer vulnerabilities;
	@JsonProperty("codeSmells")
	private Integer codeSmells;

	@JsonProperty("qualityGateStatus")
	public String getQualityGateStatus() {
		return qualityGateStatus;
	}

	@JsonProperty("qualityGateStatus")
	public void setQualityGateStatus(String qualityGateStatus) {
		this.qualityGateStatus = qualityGateStatus;
	}

	@JsonProperty("bugs")
	public Integer getBugs() {
		return bugs;
	}

	@JsonProperty("bugs")
	public void setBugs(Integer bugs) {
		this.bugs = bugs;
	}

	@JsonProperty("vulnerabilities")
	public Integer getVulnerabilities() {
		return vulnerabilities;
	}

	@JsonProperty("vulnerabilities")
	public void setVulnerabilities(Integer vulnerabilities) {
		this.vulnerabilities = vulnerabilities;
	}

	@JsonProperty("codeSmells")
	public Integer getCodeSmells() {
		return codeSmells;
	}

	@JsonProperty("codeSmells")
	public void setCodeSmells(Integer codeSmells) {
		this.codeSmells = codeSmells;
	}

	@Override
	public String toString() {
		return "Status [qualityGateStatus=" + qualityGateStatus + ", bugs=" + bugs + ", vulnerabilities=" + vulnerabilities + ", codeSmells=" + codeSmells + "]";
	}

}

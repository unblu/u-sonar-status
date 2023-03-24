package com.unblu.usonarstatus.sonar;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"key",
	"title",
	"branch",
	"base",
	"status",
	"analysisDate",
	"url",
	"target",
	"commit",
	"contributors"
})
public class PullRequest {

	@JsonProperty("key")
	private String key;
	@JsonProperty("title")
	private String title;
	@JsonProperty("branch")
	private String branch;
	@JsonProperty("base")
	private String base;
	@JsonProperty("status")
	private Status status;
	@JsonProperty("analysisDate")
	private String analysisDate;
	@JsonProperty("url")
	private String url;
	@JsonProperty("target")
	private String target;
	@JsonProperty("commit")
	private Commit commit;
	@JsonProperty("contributors")
	private List<Contributor> contributors;

	@JsonProperty("key")
	public String getKey() {
		return key;
	}

	@JsonProperty("key")
	public void setKey(String key) {
		this.key = key;
	}

	@JsonProperty("title")
	public String getTitle() {
		return title;
	}

	@JsonProperty("title")
	public void setTitle(String title) {
		this.title = title;
	}

	@JsonProperty("branch")
	public String getBranch() {
		return branch;
	}

	@JsonProperty("branch")
	public void setBranch(String branch) {
		this.branch = branch;
	}

	@JsonProperty("base")
	public String getBase() {
		return base;
	}

	@JsonProperty("base")
	public void setBase(String base) {
		this.base = base;
	}

	@JsonProperty("status")
	public Status getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(Status status) {
		this.status = status;
	}

	@JsonProperty("analysisDate")
	public String getAnalysisDate() {
		return analysisDate;
	}

	@JsonProperty("analysisDate")
	public void setAnalysisDate(String analysisDate) {
		this.analysisDate = analysisDate;
	}

	@JsonProperty("url")
	public String getUrl() {
		return url;
	}

	@JsonProperty("url")
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty("target")
	public String getTarget() {
		return target;
	}

	@JsonProperty("target")
	public void setTarget(String target) {
		this.target = target;
	}

	@JsonProperty("commit")
	public Commit getCommit() {
		return commit;
	}

	@JsonProperty("commit")
	public void setCommit(Commit commit) {
		this.commit = commit;
	}

	@JsonProperty("contributors")
	public List<Contributor> getContributors() {
		return contributors;
	}

	@JsonProperty("contributors")
	public void setContributors(List<Contributor> contributors) {
		this.contributors = contributors;
	}

	@Override
	public String toString() {
		return "PullRequest [key=" + key + ", title=" + title + ", branch=" + branch + ", base=" + base + ", status=" + status + ", analysisDate=" + analysisDate + ", url=" + url + ", target=" + target + ", commit=" + commit + ", contributors=" + contributors + "]";
	}

}
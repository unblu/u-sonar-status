package com.unblu.usonarstatus.sonar;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"pullRequests"
})
public class PullRequestsResponse {

	@JsonProperty("pullRequests")
	private List<PullRequest> pullRequests;

	@JsonProperty("pullRequests")
	public List<PullRequest> getPullRequests() {
		return pullRequests;
	}

	@JsonProperty("pullRequests")
	public void setPullRequests(List<PullRequest> pullRequests) {
		this.pullRequests = pullRequests;
	}

	@Override
	public String toString() {
		return "PullRequestsResponse [pullRequests=" + pullRequests + "]";
	}

}
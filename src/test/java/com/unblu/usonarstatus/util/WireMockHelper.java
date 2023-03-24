package com.unblu.usonarstatus.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.unblu.usonarstatus.util.MockUtil.JsonStub;

public class WireMockHelper {

	final static String GITLAB_API_PREFIX = "/api/v4";
	final static String SONAR_API_PREFIX = "/api";
	final static String API_AUTH_KEY_NAME = "PRIVATE-TOKEN";

	private WireMockServer wireMockServer;
	private String gitLabApiToken;
	private String sonarApiToken;

	public WireMockHelper(WireMockServer wireMockServer, String gitLabApiToken, String sonarApiToken) {
		this.wireMockServer = wireMockServer;
		this.gitLabApiToken = gitLabApiToken;
		this.sonarApiToken = sonarApiToken;
	}

	public void verifyRequests(int expectedRequestNumber) throws InterruptedException {
		List<ServeEvent> allServeEvents = waitForRequests(expectedRequestNumber);
		Assertions.assertEquals(expectedRequestNumber, allServeEvents.size(), "Number of requests to GitLab");

		List<StubMapping> usedStubs = allServeEvents.stream().map(e -> e.getStubMapping()).toList();
		List<StubMapping> stubMappings = wireMockServer.getStubMappings();
		List<String> unused = stubMappings.stream()
				.filter(s -> !usedStubs.contains(s))
				.map(e -> e.getRequest().toString())
				.toList();
		if (!unused.isEmpty()) {
			Assertions.fail("Some defined stubs were not called by the GitLab client:\n" + unused);
		}
	}

	private List<ServeEvent> waitForRequests(int minimalNumberOfRequestsToWaitFor) throws InterruptedException {
		int countDown = 30;
		List<ServeEvent> allServeEvents = wireMockServer.getAllServeEvents();
		while (allServeEvents.size() < minimalNumberOfRequestsToWaitFor && countDown-- > 0) {
			TimeUnit.SECONDS.sleep(1);
			allServeEvents = wireMockServer.getAllServeEvents();
		}
		return allServeEvents;
	}

	public void setupDefaultStubsForSonar() {
		setupGetMr();
		setupGetExternalStatusChecks();
		setupSetStatusOfExternalStatusCheck();
	}

	public void setupDefaultStubsForGitLab() {
		setupGetSonarPullRequests();
		setupGetExternalStatusChecks();
		setupSetStatusOfExternalStatusCheck();
	}

	public void setupGetSonarPullRequests() {
		String authorizationValue = "Basic " + Base64.getEncoder().encodeToString((sonarApiToken + ":").getBytes());
		wireMockServer.stubFor(
				get(urlPathEqualTo(SONAR_API_PREFIX + "/project_pull_requests/list"))
						.withHeader("Authorization", WireMock.equalTo(authorizationValue))
						.withQueryParam("project", WireMock.equalTo("project_56"))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(MockUtil.get(JsonStub.SONAR_GET_PULLREQUEST))));
	}

	public void setupGetMr() {
		wireMockServer.stubFor(
				get(urlPathEqualTo(GITLAB_API_PREFIX + "/projects/" + MockUtil.GITLAB_PROJECT_ID + "/merge_requests/" + MockUtil.SONAR_BRANCH_NAME))
						.withHeader(API_AUTH_KEY_NAME, WireMock.equalTo(gitLabApiToken))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(MockUtil.get(JsonStub.GITLAB_GET_MERGE_REQUEST))));
	}

	public void setupGetExternalStatusChecks() {
		wireMockServer.stubFor(
				get(urlPathEqualTo(GITLAB_API_PREFIX + "/projects/" + MockUtil.GITLAB_PROJECT_ID + "/external_status_checks"))
						.withHeader(API_AUTH_KEY_NAME, WireMock.equalTo(gitLabApiToken))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(MockUtil.get(JsonStub.GITLAB_GET_EXTERNAL_STATUS_CHECKS))));
	}

	public void setupGetExternalStatusChecksEmpty() {
		wireMockServer.stubFor(
				get(urlPathEqualTo(GITLAB_API_PREFIX + "/projects/" + MockUtil.GITLAB_PROJECT_ID + "/external_status_checks"))
						.withHeader(API_AUTH_KEY_NAME, WireMock.equalTo(gitLabApiToken))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody("[]")));
	}

	public void setupCreateExternalStatusCheck() {
		wireMockServer.stubFor(
				post(urlPathEqualTo(GITLAB_API_PREFIX + "/projects/" + MockUtil.GITLAB_PROJECT_ID + "/external_status_checks"))
						.withHeader(API_AUTH_KEY_NAME, WireMock.equalTo(gitLabApiToken))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(MockUtil.get(JsonStub.GITLAB_CREATE_EXTERNAL_STATUS_CHECK))));
	}

	public void setupSetStatusOfExternalStatusCheck() {
		wireMockServer.stubFor(
				post(urlPathEqualTo(GITLAB_API_PREFIX + "/projects/" + MockUtil.GITLAB_PROJECT_ID + "/merge_requests/" + MockUtil.SONAR_BRANCH_NAME + "/status_check_responses"))
						.withHeader(API_AUTH_KEY_NAME, WireMock.equalTo(gitLabApiToken))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(MockUtil.get(JsonStub.GITLAB_SET_STATUS_OF_EXTERNAL_STATUS_CHECK))));
	}

}

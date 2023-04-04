package com.unblu.usonarstatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.unblu.usonarstatus.model.GitLabEventSimple;
import com.unblu.usonarstatus.model.SonarEventSimple;
import com.unblu.usonarstatus.util.MockUtil;
import com.unblu.usonarstatus.util.MockUtil.JsonStub;
import com.unblu.usonarstatus.util.WireMockHelper;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@QuarkusTestResource(WireMockGitlabProxy.class)
@TestProfile(SonarSecretDefinedProfile.class)
class USonarStatusTest {

	private static final String HMAC_FOR_SONAR_EVENT = "5a857ed6a1d38ec984b8b825d4bebbcf32deb4a5b99d6b2473f8f5f56b05e707";
	private static final String HMAC_FOR_SONAR_EVENT_WRONG_PROJECT_KEY = "081ea2d5306dd79ef527b08f046c321f68f99c7c02317517bc585411bffaa19d";

	@InjectWireMock
	WireMockServer wireMockServer;

	@ConfigProperty(name = "gitlab.api.token")
	String gitLabApiToken;

	@ConfigProperty(name = "sonarqube.api.token")
	String sonarApiToken;

	private WireMockHelper wireMockHelper;

	@BeforeEach
	void init() throws IOException {
		wireMockServer.resetAll();
		wireMockHelper = new WireMockHelper(wireMockServer, gitLabApiToken, sonarApiToken);
	}

	@Test
	void testSonarEndpointRapidReturn() throws Exception {
		wireMockHelper.setupDefaultStubsForSonar();

		String body = MockUtil.get(JsonStub.SONAR_EVENT);
		given().when()
				.header("Content-Type", "application/json")
				.header("x-sonar-webhook-hmac-sha256", HMAC_FOR_SONAR_EVENT)
				.body(body)
				.post("/u-sonar-status/sonar")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("SONAR"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", notNullValue())
				.body("gitlab_project_id", nullValue())
				.body("gitlab_merge_request_iid", nullValue())
				.body("gitlab_external_status_check_id", nullValue())
				.body("gitlab_external_status_check_status", nullValue())
				.body("gitlab_external_status_check_status_id", nullValue())
				.body("error", nullValue());

		wireMockHelper.verifyRequests(3);
	}

	@Test
	void testSonarEndpointBlocking() throws Exception {
		wireMockHelper.setupDefaultStubsForSonar();

		String body = MockUtil.get(JsonStub.SONAR_EVENT);
		given().when()
				.header("Content-Type", "application/json")
				.header("x-sonar-webhook-hmac-sha256", HMAC_FOR_SONAR_EVENT)
				.body(body)
				.post("/u-sonar-status/sonar-blocking")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("SONAR"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", notNullValue())
				.body("gitlab_project_id", equalTo(56))
				.body("gitlab_merge_request_iid", equalTo(19))
				.body("gitlab_external_status_check_id", equalTo(3))
				.body("gitlab_external_status_check_status", equalTo("passed"))
				.body("gitlab_external_status_check_status_id", equalTo(4))
				.body("error", nullValue());

		wireMockHelper.verifyRequests(3);
	}

	@Test
	void testSonarSuccessCase() throws Exception {
		wireMockHelper.setupDefaultStubsForSonar();
		SonarEventSimple simpleSonarEvent = MockUtil.createDefaultSonarEventSimple();

		given().when()
				.header("Content-Type", "application/json")
				.body(simpleSonarEvent)
				.post("/u-sonar-status/sonar-replay")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("SONAR"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", notNullValue())
				.body("gitlab_project_id", equalTo(56))
				.body("gitlab_merge_request_iid", equalTo(19))
				.body("gitlab_external_status_check_id", equalTo(3))
				.body("gitlab_external_status_check_status", equalTo("passed"))
				.body("gitlab_external_status_check_status_id", equalTo(4))
				.body("error", nullValue());

		wireMockHelper.verifyRequests(3);
	}

	@Test
	void testSonarSuccessCaseNoExistingExternalCheck() throws Exception {
		wireMockHelper.setupGetMr();
		wireMockHelper.setupGetExternalStatusChecksEmpty();
		wireMockHelper.setupCreateExternalStatusCheck();
		wireMockHelper.setupSetStatusOfExternalStatusCheck();

		SonarEventSimple simpleSonarEvent = MockUtil.createDefaultSonarEventSimple();

		given().when()
				.header("Content-Type", "application/json")
				.body(simpleSonarEvent)
				.post("/u-sonar-status/sonar-replay")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("SONAR"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", notNullValue())
				.body("gitlab_project_id", equalTo(56))
				.body("gitlab_merge_request_iid", equalTo(19))
				.body("gitlab_external_status_check_id", equalTo(3))
				.body("gitlab_external_status_check_status", equalTo("passed"))
				.body("gitlab_external_status_check_status_id", equalTo(4))
				.body("error", nullValue());

		wireMockHelper.verifyRequests(4);
	}

	@Test
	void testSonarEndpointRapidReturnMalformedRequest() throws Exception {
		String json = """
				{
				    "foo" : "bar"
				    "baz" : 43
				}
				""";
		given().when()
				.header("Content-Type", "application/json")
				.body(json)
				.post("/u-sonar-status/sonar")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("error", startsWith("Failed to decode:Unexpected character ('\"' (code 34)): was expecting comma to separate Object entries"));

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testSonarEndpointBlockingInvalidHmac() throws Exception {
		String body = MockUtil.get(JsonStub.SONAR_EVENT);
		given().when()
				.header("Content-Type", "application/json")
				.header("x-sonar-webhook-hmac-sha256", "invalidHmacValue")
				.body(body)
				.post("/u-sonar-status/sonar-blocking")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("SONAR"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", notNullValue())
				.body("gitlab_project_id", nullValue())
				.body("gitlab_merge_request_iid", nullValue())
				.body("gitlab_external_status_check_id", nullValue())
				.body("gitlab_external_status_check_status", nullValue())
				.body("gitlab_external_status_check_status_id", nullValue())
				.body("error", equalTo("Event skipped"));

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testSonarEndpointBlockingMissingHmac() throws Exception {
		String body = MockUtil.get(JsonStub.SONAR_EVENT);
		given().when()
				.header("Content-Type", "application/json")
				.body(body)
				.post("/u-sonar-status/sonar-blocking")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("SONAR"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", notNullValue())
				.body("gitlab_project_id", nullValue())
				.body("gitlab_merge_request_iid", nullValue())
				.body("gitlab_external_status_check_id", nullValue())
				.body("gitlab_external_status_check_status", nullValue())
				.body("gitlab_external_status_check_status_id", nullValue())
				.body("error", equalTo("Event skipped"));

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testSonarEndpointBlockingWrongProjectKey() throws Exception {
		String body = MockUtil.get(JsonStub.SONAR_EVENT_WRONG_PROJECT_KEY);
		given().when()
				.header("Content-Type", "application/json")
				.header("x-sonar-webhook-hmac-sha256", HMAC_FOR_SONAR_EVENT_WRONG_PROJECT_KEY)
				.body(body)
				.post("/u-sonar-status/sonar-blocking")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("SONAR"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", notNullValue())
				.body("gitlab_project_id", nullValue())
				.body("gitlab_merge_request_iid", nullValue())
				.body("gitlab_external_status_check_id", nullValue())
				.body("gitlab_external_status_check_status", nullValue())
				.body("gitlab_external_status_check_status_id", nullValue())
				.body("error", nullValue());

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testGitLabEndpointRapidReturn() throws Exception {
		wireMockHelper.setupDefaultStubsForGitLab();

		String body = MockUtil.get(JsonStub.GITLAB_EXTERNAL_CHECK_EVENT);
		given().when()
				.header("Content-Type", "application/json")
				.body(body)
				.post("/u-sonar-status/gitlab")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("GITLAB"))
				.body("sonar_event_uuid", nullValue())
				.body("gitlab_event_uuid", notNullValue())
				.body("gitlab_project_id", nullValue())
				.body("gitlab_merge_request_iid", nullValue())
				.body("gitlab_external_status_check_id", nullValue())
				.body("gitlab_external_status_check_status", nullValue())
				.body("gitlab_external_status_check_status_id", nullValue())
				.body("error", nullValue());

		wireMockHelper.verifyRequests(2);
	}

	@Test
	void testGitLabEndpointBlocking() throws Exception {
		wireMockHelper.setupDefaultStubsForGitLab();

		String body = MockUtil.get(JsonStub.GITLAB_EXTERNAL_CHECK_EVENT);
		given().when()
				.header("Content-Type", "application/json")
				.body(body)
				.post("/u-sonar-status/gitlab-blocking")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("GITLAB"))
				.body("sonar_event_uuid", nullValue())
				.body("gitlab_event_uuid", notNullValue())
				.body("gitlab_project_id", equalTo(56))
				.body("gitlab_merge_request_iid", equalTo(19))
				.body("gitlab_external_status_check_id", equalTo(3))
				.body("gitlab_external_status_check_status", equalTo("passed"))
				.body("gitlab_external_status_check_status_id", equalTo(4))
				.body("error", nullValue());

		wireMockHelper.verifyRequests(2);
	}

	@Test
	void testGitLabSuccessCase() throws Exception {
		wireMockHelper.setupDefaultStubsForGitLab();
		GitLabEventSimple simpleGitLabEvent = MockUtil.createDefaultGitLabEventSimple();

		given().when()
				.header("Content-Type", "application/json")
				.body(simpleGitLabEvent)
				.post("/u-sonar-status/gitlab-replay")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("GITLAB"))
				.body("sonar_event_uuid", nullValue())
				.body("gitlab_event_uuid", notNullValue())
				.body("gitlab_project_id", equalTo(56))
				.body("gitlab_merge_request_iid", equalTo(19))
				.body("gitlab_external_status_check_id", equalTo(3))
				.body("gitlab_external_status_check_status", equalTo("passed"))
				.body("gitlab_external_status_check_status_id", equalTo(4))
				.body("error", nullValue());

		wireMockHelper.verifyRequests(2);
	}

	@Test
	void testGitLabBypassCase() throws Exception {
		wireMockHelper.setupSetStatusOfExternalStatusCheck();
		GitLabEventSimple simpleGitLabEvent = MockUtil.createDefaultGitLabEventSimple();
		simpleGitLabEvent.setMergeRequestSourceBranch("mr123_release/6.x.x");

		given().when()
				.header("Content-Type", "application/json")
				.body(simpleGitLabEvent)
				.post("/u-sonar-status/gitlab-replay")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("source", equalTo("GITLAB"))
				.body("sonar_event_uuid", nullValue())
				.body("gitlab_event_uuid", notNullValue())
				.body("gitlab_project_id", equalTo(56))
				.body("gitlab_merge_request_iid", equalTo(19))
				.body("gitlab_external_status_check_id", equalTo(3))
				.body("gitlab_external_status_check_status", equalTo("passed"))
				.body("gitlab_external_status_check_status_id", equalTo(4))
				.body("error", nullValue());

		wireMockHelper.verifyRequests(1);
	}

	@Test
	void testGitlabEndpointRapidReturnMalformedRequest() throws Exception {
		String json = """
				{
				    "foo" : "bar"
				    "baz" : 43
				}
				""";
		given().when()
				.header("Content-Type", "application/json")
				.body(json)
				.post("/u-sonar-status/gitlab")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("error", startsWith("Failed to decode:Unexpected character ('\"' (code 34)): was expecting comma to separate Object entries"));

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testInvalidEndpoint() throws Exception {
		String json = """
				{
				    "foo" : "bar",
				    "baz" : 43
				}
				""";
		given().when()
				.header("Content-Type", "application/json")
				.body(json)
				.post("/foo")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", nullValue())
				.body("sonar_event_uuid", nullValue())
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("error", equalTo("Invalid path: /foo"));

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testHealthLive() throws Exception {
		//make sure we get an answer from the 'quarkus-smallrye-health' module
		given().when()
				.get("/q/health/live")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body("status", notNullValue())
				.body("checks", notNullValue())
				.body("gitlab_event_uuid", nullValue())
				.body("build_commit", nullValue())
				.body("build_timestamp", nullValue())
				.body("error", nullValue());

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testHealthReady() throws Exception {
		//make sure we get an answer from the 'quarkus-smallrye-health' module
		given().when()
				.post("/q/health/ready")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body("status", notNullValue())
				.body("checks", notNullValue())
				.body("gitlab_event_uuid", nullValue())
				.body("build_commit", nullValue())
				.body("build_timestamp", nullValue())
				.body("error", nullValue());

		wireMockHelper.verifyRequests(0);
	}
}

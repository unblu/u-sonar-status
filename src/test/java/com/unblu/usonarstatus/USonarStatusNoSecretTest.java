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
import com.unblu.usonarstatus.util.MockUtil;
import com.unblu.usonarstatus.util.MockUtil.JsonStub;
import com.unblu.usonarstatus.util.WireMockHelper;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@QuarkusTestResource(WireMockGitlabProxy.class)
@TestProfile(SonarSecretEmptyProfile.class)
class USonarStatusNoSecretTest {

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
	void testSonarEndpointBlockingNoSecret() throws Exception {
		wireMockHelper.setupDefaultStubsForSonar();

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
				.body("gitlab_project_id", equalTo(56))
				.body("gitlab_merge_request_iid", equalTo(19))
				.body("gitlab_external_status_check_id", equalTo(3))
				.body("gitlab_external_status_check_status", equalTo("passed"))
				.body("gitlab_external_status_check_status_id", equalTo(4))
				.body("error", nullValue());

		wireMockHelper.verifyRequests(3);
	}

	@Test
	void testSonarEndpointBlockingWrongProjectKeyNoSecret() throws Exception {
		String body = MockUtil.get(JsonStub.SONAR_EVENT_WRONG_PROJECT_KEY);
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
				.body("error", nullValue());

		wireMockHelper.verifyRequests(0);
	}

	@Test
	void testSonarEndpointBlockingWithHMacNoSecret() throws Exception {
		String body = MockUtil.get(JsonStub.SONAR_EVENT);
		given().when()
				.header("Content-Type", "application/json")
				.header("x-sonar-webhook-hmac-sha256", "someHmacValue")
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

}

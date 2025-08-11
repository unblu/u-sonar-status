package com.unblu.usonarstatus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.gitlab4j.api.models.ExternalStatusCheckResult;
import org.gitlab4j.api.utils.JacksonJson;
import org.gitlab4j.api.webhook.ExternalStatusCheckEvent;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.unblu.usonarstatus.model.GitLabEventSimple;
import com.unblu.usonarstatus.model.SonarEventSimple;
import com.unblu.usonarstatus.model.USonarStatusResult;
import com.unblu.usonarstatus.model.USonarStatusResult.Source;
import com.unblu.usonarstatus.sonar.Branch;
import com.unblu.usonarstatus.sonar.Condition;
import com.unblu.usonarstatus.sonar.Project;
import com.unblu.usonarstatus.sonar.QualityGate;
import com.unblu.usonarstatus.sonar.SonarEvent;
import com.unblu.usonarstatus.util.MockUtil;
import com.unblu.usonarstatus.util.MockUtil.JsonStub;

class JsonUtilsTest {

	@Test
	void testParseGitlabReplayInput() throws Exception {
		GitLabEventSimple expected = MockUtil.createDefaultGitLabEventSimple();
		checkJsonFile(expected, GitLabEventSimple.class, "_documentation/src/docs/documentation/gitlab-replay.json");
	}

	@Test
	void testSonarNonBlockingResponseFile() throws Exception {
		USonarStatusResult expected = createUSonarStatusResult(null, MockUtil.SONAR_EVENT_UUID, Source.SONAR);
		checkJsonFile(expected, USonarStatusResult.class, "_documentation/src/docs/documentation/sonar-non-blocking-response.json");
	}

	@Test
	void testSonarBlockingResponseFile() throws Exception {
		ExternalStatusCheckResult response = createExternalStatusCheckResult();

		USonarStatusResult expected = createUSonarStatusResult(null, MockUtil.SONAR_EVENT_UUID, Source.SONAR);
		expected.setSonarTaskId(MockUtil.SONAR_TASK_ID);
		Service.mapFromResponse(expected, response);
		checkJsonFile(expected, USonarStatusResult.class, "_documentation/src/docs/documentation/sonar-blocking-response.json");
	}

	@Test
	void testGitlabNonBlockingResponseFile() throws Exception {
		USonarStatusResult expected = createUSonarStatusResult(MockUtil.GITLAB_EVENT_UUID, null, Source.GITLAB);
		checkJsonFile(expected, USonarStatusResult.class, "_documentation/src/docs/documentation/gitlab-non-blocking-response.json");
	}

	@Test
	void testGitlabBlockingResponseFile() throws Exception {
		ExternalStatusCheckResult response = createExternalStatusCheckResult();

		USonarStatusResult expected = createUSonarStatusResult(MockUtil.GITLAB_EVENT_UUID, null, Source.GITLAB);
		Service.mapFromResponse(expected, response);

		checkJsonFile(expected, USonarStatusResult.class, "_documentation/src/docs/documentation/gitlab-blocking-response.json");
	}

	private ExternalStatusCheckResult createExternalStatusCheckResult() throws JsonParseException, JsonMappingException, IOException {
		JacksonJson jacksonJson = new JacksonJson();
		ExternalStatusCheckResult response = jacksonJson.unmarshal(ExternalStatusCheckResult.class, MockUtil.get(JsonStub.GITLAB_SET_STATUS_OF_EXTERNAL_STATUS_CHECK));
		return response;
	}

	@Test
	void testParseSonarReplayInput() throws Exception {
		SonarEventSimple expected = MockUtil.createDefaultSonarEventSimple();
		checkJsonFile(expected, SonarEventSimple.class, "_documentation/src/docs/documentation/sonar-replay.json");
	}

	private USonarStatusResult createUSonarStatusResult(String gitlabEventUuid, String sonarEventUuid, Source source) {
		USonarStatusResult result = new USonarStatusResult();
		result.setBuildCommit("6af21ad");
		result.setBuildTimestamp("2022-01-01T07:21:58.378413Z");
		result.setGitlabEventUUID("62940263-b495-4f7e-b0e8-578c7307f13d");
		result.setGitlabEventUUID(gitlabEventUuid);
		result.setSonarEventUUID(sonarEventUuid);
		result.setSource(source);
		return result;
	}

	@Test
	void testSonarEvent() throws Exception {
		SonarEvent event = createSonarEvent();
		checkJsonFile(event, SonarEvent.class, "src/test/resources/sonar_template_json/webhook/sonarEvent.json");
	}

	private <T> void checkJsonFile(T expected, Class<T> cls, String filePath) throws IOException, JsonProcessingException, JsonMappingException {
		// Due to https://github.com/docToolchain/docToolchain/issues/898 we need a copy inside the _documentation project because it can't access the java project
		Path file = Path.of(filePath);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);

		String content;
		T actual;
		// Read the current content to see if the parser works:
		if (Files.exists(file)) {
			content = Files.readString(file);

			try {
				actual = mapper.readValue(content, cls);
			} catch (Exception e) {
				actual = null;
			}
		} else {
			content = null;
			actual = null;
		}

		// Update the file derived from the Java model, so that we are sure it stays up-to-date:
		String expectedContent = mapper.writeValueAsString(expected);
		Files.writeString(file, expectedContent);

		Assertions.assertThat(actual).isEqualTo(expected);
		Assertions.assertThat(content).isEqualTo(expectedContent);
	}

	@Test
	void testToGitLabEventSimple() throws Exception {
		String content = MockUtil.get(JsonStub.GITLAB_EXTERNAL_CHECK_EVENT);
		ExternalStatusCheckEvent event = new JacksonJson().getObjectMapper().readValue(content, ExternalStatusCheckEvent.class);
		GitLabEventSimple result = JsonUtils.toGitLabEventSimple(event, MockUtil.GITLAB_EVENT_UUID);

		GitLabEventSimple expected = MockUtil.createDefaultGitLabEventSimple();
		Assertions.assertThat(result).isEqualTo(expected);
	}

	@Test
	void testToSonarSimple() throws Exception {
		SonarEvent event = createSonarEvent();
		SonarEventSimple result = JsonUtils.toSonarEventSimple(event, MockUtil.SONAR_EVENT_UUID);

		SonarEventSimple expected = MockUtil.createDefaultSonarEventSimple();
		Assertions.assertThat(result).isEqualTo(expected);
	}

	private SonarEvent createSonarEvent() {
		Project project = new Project();
		project.setKey(MockUtil.SONAR_PROJECT_KEY);
		project.setName("a_project (scanned by Jenkins)");
		project.setUrl("https://sonar.example.com/dashboard?id=project_56");

		Branch branch = new Branch();
		branch.setName(MockUtil.SONAR_BRANCH_NAME);
		branch.setType(MockUtil.SONAR_BRANCH_TYPE);
		branch.setIsMain(false);
		branch.setUrl("https://sonar.example.com/dashboard?id=project_56&pullRequest=100");

		Condition c1 = new Condition();
		c1.setMetric("new_reliability_rating");
		c1.setOperator("GREATER_THAN");
		c1.setValue("1");
		c1.setStatus(MockUtil.SONAR_QUALITY_GATE_STATUS_OK);
		c1.setErrorThreshold("1");
		Condition c2 = new Condition();
		c2.setMetric("new_security_rating");
		c2.setOperator("GREATER_THAN");
		c2.setValue("1");
		c2.setStatus(MockUtil.SONAR_QUALITY_GATE_STATUS_OK);
		c2.setErrorThreshold("1");
		Condition c3 = new Condition();
		c3.setMetric("new_maintainability_rating");
		c3.setOperator("GREATER_THAN");
		c3.setValue("1");
		c3.setStatus(MockUtil.SONAR_QUALITY_GATE_STATUS_OK);
		c3.setErrorThreshold("1");
		Condition c4 = new Condition();
		c4.setMetric("new_duplicated_lines_density");
		c4.setOperator("GREATER_THAN");
		c4.setValue("0.0");
		c4.setStatus(MockUtil.SONAR_QUALITY_GATE_STATUS_OK);
		c4.setErrorThreshold("3");
		Condition c5 = new Condition();
		c5.setMetric("new_security_hotspots_reviewed");
		c5.setOperator("LESS_THAN");
		c5.setStatus("NO_VALUE");
		c5.setErrorThreshold(MockUtil.SONAR_BRANCH_NAME);
		Condition c6 = new Condition();
		c6.setMetric("new_security_review_rating");
		c6.setOperator("GREATER_THAN");
		c6.setValue("1");
		c6.setStatus(MockUtil.SONAR_QUALITY_GATE_STATUS_OK);
		c6.setErrorThreshold("1");

		QualityGate qualityGate = new QualityGate();
		qualityGate.setName("Test Gate");
		qualityGate.setStatus(MockUtil.SONAR_QUALITY_GATE_STATUS_OK);
		qualityGate.setConditions(List.of(c1, c2, c3, c4, c5, c6));

		SonarEvent event = new SonarEvent();
		event.setServerUrl("https://sonar.example.com");
		event.setTaskId(MockUtil.SONAR_TASK_ID);
		event.setStatus("SUCCESS");
		event.setAnalysedAt("2023-03-10T16:40:28+0000");
		event.setRevision(MockUtil.SONAR_REVISION);
		event.setChangedAt("2023-03-10T16:40:28+0000");
		event.setProject(project);
		event.setBranch(branch);
		event.setQualityGate(qualityGate);
		event.setProperties(Map.of("sonar.analysis.detectedscm", "git"));
		return event;
	}

}

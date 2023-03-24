package com.unblu.usonarstatus.service;

import org.assertj.core.api.Assertions;
import org.gitlab4j.api.models.ExternalStatusCheckStatus.Status;
import org.junit.jupiter.api.Test;

import com.unblu.usonarstatus.model.SonarEventSimple;
import com.unblu.usonarstatus.util.MockUtil;

class ServiceTest {

	@Test
	void testPrefix() throws Exception {
		Assertions.assertThat(Service.PROJECT_PREFIX.length()).isEqualTo(Service.PROJECT_PREFIX_LENGTH);
	}

	@Test
	void testSonarEventIsRelevant() throws Exception {
		SonarEventSimple e1 = MockUtil.createDefaultSonarEventSimple();
		Assertions.assertThat(Service.sonarEventIsRelevant(e1)).isTrue();

		SonarEventSimple e2 = MockUtil.createDefaultSonarEventSimple();
		e2.setBranchType("BRANCH");
		Assertions.assertThat(Service.sonarEventIsRelevant(e2)).isFalse();

		SonarEventSimple e3 = MockUtil.createDefaultSonarEventSimple();
		e3.setBranchName("null");
		Assertions.assertThat(Service.sonarEventIsRelevant(e3)).isFalse();

		SonarEventSimple e4 = MockUtil.createDefaultSonarEventSimple();
		e4.setProjectKey("a_project");
		Assertions.assertThat(Service.sonarEventIsRelevant(e4)).isFalse();
	}

	@Test
	void testToGitLabProjectId() throws Exception {
		SonarEventSimple e = MockUtil.createDefaultSonarEventSimple();
		Assertions.assertThat(Service.toGitLabProjectId(e)).isEqualTo(56L);
	}

	@Test
	void testToGitLabMergeRequestIid() throws Exception {
		SonarEventSimple e = MockUtil.createDefaultSonarEventSimple();
		Assertions.assertThat(Service.toGitLabMergeRequestIid(e)).isEqualTo(100L);
	}

	@Test
	void testToGitLabExternalCheckStatus() throws Exception {
		Assertions.assertThat(Service.toGitLabExternalCheckStatus("OK")).isEqualTo(Status.PASSED);
		Assertions.assertThat(Service.toGitLabExternalCheckStatus("ERROR")).isEqualTo(Status.FAILED);
		Assertions.assertThat(Service.toGitLabExternalCheckStatus("unknown")).isNull();
		;
	}
}

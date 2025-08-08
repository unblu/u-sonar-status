package com.unblu.usonarstatus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.stream.Collectors;

import com.unblu.usonarstatus.model.GitLabEventSimple;
import com.unblu.usonarstatus.model.SonarEventSimple;

public class MockUtil {

	public static final String GITLAB_EVENT_UUID = "gitlab-c289ee78-ab41-4656-ad7a-3d8ea464fb25";
	public static final Long GITLAB_PROJECT_ID = 56L;
	public static final Long GITLAB_MERGE_REQUEST_IID = 100L;
	public static final String GITLAB_MERGE_REQUEST_LAST_COMMIT_SHA = "ba22a8ce46acee878adec5ffee5a5621c9d35ac1";
	public static final String GITLAB_MERGE_REQUEST_SOURCE_BRANCH = "patch-2";
	public static final Long GITLAB_EXTERNAL_STATUS_CHECK_ID = 3L;
	public static final String GITLAB_EXTERNAL_STATUS_CHECK_NAME = "SONAR";
	public static final String GITLAB_EXTERNAL_STATUS_CHECK_URL = "https://company.example.com/u-sonar-status/gitlab";

	public static final String SONAR_EVENT_UUID = "sonar-c9961b21-8554-4df8-8485-1aacd05b6ac4";
	public static final String SONAR_TASK_ID = "AYbMZT8ngxz2HxwOGdhr";
	public static final String SONAR_REVISION = "ba22a8ce46acee878adec5ffee5a5621c9d35ac1";
	public static final String SONAR_PROJECT_KEY = "project_" + GITLAB_PROJECT_ID;
	public static final String SONAR_BRANCH_NAME = "" + GITLAB_MERGE_REQUEST_IID;
	public static final String SONAR_BRANCH_TYPE = "PULL_REQUEST";
	public static final String SONAR_QUALITY_GATE_STATUS_OK = "OK";
	public static final String SONAR_QUALITY_GATE_STATUS_ERROR = "ERROR";

	public static enum JsonStub {
		SONAR_EVENT,
		SONAR_EVENT_WRONG_PROJECT_KEY,
		SONAR_GET_PULLREQUEST,
		GITLAB_APPROVE_MERGE_REQUEST,
		GITLAB_UNAPPROVE_MERGE_REQUEST,
		GITLAB_GET_MERGE_REQUEST_APPROVALS_APPROVED,
		GITLAB_GET_MERGE_REQUEST_APPROVALS_NOT_APPROVED,
		GITLAB_GET_MERGE_REQUEST,
		GITLAB_GET_EXTERNAL_STATUS_CHECKS,
		GITLAB_CREATE_EXTERNAL_STATUS_CHECK,
		GITLAB_SET_STATUS_OF_EXTERNAL_STATUS_CHECK,
		GITLAB_EXTERNAL_CHECK_EVENT;
	}

	private static final EnumMap<JsonStub, String> jsonStubsLocation = initJsonTemplatesLocationMap();

	/**
	 * Loads the JSON file content for a given action, possibly customizing some properties.
	 *
	 * @param stub The stub for which the corresponding JSON is required.
	 * @return String containing the contents of the JSON file for the given stub.
	 */
	public static String get(JsonStub stub) {
		return readFromResources(jsonStubsLocation.get(stub));
	}

	private static String readFromResources(String name) {
		try (InputStream is = MockUtil.class.getResourceAsStream(name)) {
			return new BufferedReader(new InputStreamReader(is))
					.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			throw new RuntimeException("Could not read resource " + name, e);
		}
	}

	private static EnumMap<JsonStub, String> initJsonTemplatesLocationMap() {
		EnumMap<JsonStub, String> templates = new EnumMap<>(JsonStub.class);
		templates.put(JsonStub.SONAR_EVENT, "/sonar_template_json/webhook/sonarEvent.json");
		templates.put(JsonStub.SONAR_EVENT_WRONG_PROJECT_KEY, "/sonar_template_json/webhook/sonarEventWrongProjectKey.json");
		templates.put(JsonStub.SONAR_GET_PULLREQUEST, "/sonar_template_json/api/pullRequestList.json");
		templates.put(JsonStub.GITLAB_APPROVE_MERGE_REQUEST, "/gitlab_template_json/api/approveMr.json");
		templates.put(JsonStub.GITLAB_UNAPPROVE_MERGE_REQUEST, "/gitlab_template_json/api/unapproveMr.json");
		templates.put(JsonStub.GITLAB_GET_MERGE_REQUEST_APPROVALS_APPROVED, "/gitlab_template_json/api/getMrApprovalsApproved.json");
		templates.put(JsonStub.GITLAB_GET_MERGE_REQUEST_APPROVALS_NOT_APPROVED, "/gitlab_template_json/api/getMrApprovalsNotApproved.json");
		templates.put(JsonStub.GITLAB_GET_MERGE_REQUEST, "/gitlab_template_json/api/getMr.json");
		templates.put(JsonStub.GITLAB_GET_EXTERNAL_STATUS_CHECKS, "/gitlab_template_json/api/getExternalStatusChecks.json");
		templates.put(JsonStub.GITLAB_CREATE_EXTERNAL_STATUS_CHECK, "/gitlab_template_json/api/createExternalStatusChecksResponse.json");
		templates.put(JsonStub.GITLAB_SET_STATUS_OF_EXTERNAL_STATUS_CHECK, "/gitlab_template_json/api/setStatusOfExternalStatusCheckResponse.json");
		templates.put(JsonStub.GITLAB_EXTERNAL_CHECK_EVENT, "/gitlab_template_json/webhook/externalCheckEvent.json");
		return templates;
	}

	public static GitLabEventSimple createDefaultGitLabEventSimple() {
		GitLabEventSimple event = new GitLabEventSimple();
		event.setGitlabEventUUID(GITLAB_EVENT_UUID);
		event.setProjectId(GITLAB_PROJECT_ID);
		event.setMergeRequestIid(GITLAB_MERGE_REQUEST_IID);
		event.setMergeRequestLastCommitSha(GITLAB_MERGE_REQUEST_LAST_COMMIT_SHA);
		event.setMergeRequestSourceBranch(GITLAB_MERGE_REQUEST_SOURCE_BRANCH);
		event.setExternalStatusCheckId(GITLAB_EXTERNAL_STATUS_CHECK_ID);
		event.setExternalStatusCheckName(GITLAB_EXTERNAL_STATUS_CHECK_NAME);
		event.setExternalStatusCheckUrl(GITLAB_EXTERNAL_STATUS_CHECK_URL);
		return event;
	}

	public static SonarEventSimple createDefaultSonarEventSimple() {
		SonarEventSimple event = new SonarEventSimple();
		event.setSonarEventUUID(SONAR_EVENT_UUID);
		event.setTaskId(SONAR_TASK_ID);
		event.setRevision(SONAR_REVISION);
		event.setProjectKey(SONAR_PROJECT_KEY);
		event.setBranchName(SONAR_BRANCH_NAME);
		event.setBranchType(SONAR_BRANCH_TYPE);
		event.setQualityGateStatus(SONAR_QUALITY_GATE_STATUS_OK);
		return event;
	}

	public static SonarEventSimple createErrorSonarEventSimple() {
		SonarEventSimple event = new SonarEventSimple();
		event.setSonarEventUUID(SONAR_EVENT_UUID);
		event.setTaskId(SONAR_TASK_ID);
		event.setRevision(SONAR_REVISION);
		event.setProjectKey(SONAR_PROJECT_KEY);
		event.setBranchName(SONAR_BRANCH_NAME);
		event.setBranchType(SONAR_BRANCH_TYPE);
		event.setQualityGateStatus(SONAR_QUALITY_GATE_STATUS_ERROR);
		return event;
	}
}

package com.unblu.usonarstatus;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class SonarSecretDefinedProfile implements QuarkusTestProfile {

	private static final String SECRET = "_a:secret-for!tests_";

	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of("sonarqube.webhook.secret", SECRET);
	}
}

package com.unblu.usonarstatus.sonar;

import java.util.Base64;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

import io.quarkus.logging.Log;

@Path("/api")
@ClientHeaderParam(name = "Authorization", value = "{lookupAuth}")
public interface SonarClient {

	default String lookupAuth() {
		String token = ConfigProvider.getConfig().getValue("sonarqube.api.token", String.class);
		if (token == null) {
			Log.error("Sonar token is not defined");
			return null;
		}
		return "Basic " + Base64.getEncoder().encodeToString((token + ":").getBytes());
	}

	@GET
	@Path("/qualitygates/project_status")
	ProjectStatusResponse getProjectStatus(@QueryParam("projectKey") String projectKey, @QueryParam("pullRequest") String pullRequest);

	@GET
	@Path("/project_pull_requests/list")
	PullRequestsResponse listPullRequests(@QueryParam("project") String projectKey);

}
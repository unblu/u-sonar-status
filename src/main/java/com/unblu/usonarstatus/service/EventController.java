package com.unblu.usonarstatus.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.gitlab4j.api.webhook.ExternalStatusCheckEvent;

import com.unblu.usonarstatus.model.GitLabEventSimple;
import com.unblu.usonarstatus.model.SonarEventSimple;
import com.unblu.usonarstatus.model.USonarStatusResult;
import com.unblu.usonarstatus.model.USonarStatusResult.Source;
import com.unblu.usonarstatus.sonar.SonarEvent;

import io.quarkus.logging.Log;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Header;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.eventbus.EventBus;

@RouteBase(produces = "application/json")
public class EventController {

	@Inject
	Service service;

	@Inject
	EventBus eventsBus;

	@ConfigProperty(name = "sonarqube.webhook.secret")
	Optional<String> sonarWebhookSecret;

	@Route(path = "/u-sonar-status/sonar", order = 1, methods = HttpMethod.POST, type = HandlerType.NORMAL)
	public USonarStatusResult handleSonarEvent(@Header("x-sonar-webhook-hmac-sha256") String hmacValue, @Body SonarEvent sonarEvent, RoutingContext rc) {
		String sonarEventUUID = createSonarUUID();
		Log.infof("SonarEvent: '%s' | Received hmac: '%s'", sonarEventUUID, hmacValue);
		if (isSonarHmacValid(hmacValue, rc.body().buffer().getBytes(), sonarEventUUID)) {
			SonarEventSimple simpleEvent = JsonUtils.toSonarEventSimple(sonarEvent, sonarEventUUID);
			// consumed by Service class
			eventsBus.send(Service.SONAR_EVENT, simpleEvent);
		}
		rc.response().setStatusCode(202);
		return service.createResult(null, sonarEventUUID, Source.SONAR);
	}

	@Route(path = "/u-sonar-status/sonar-blocking", order = 1, methods = HttpMethod.POST, type = HandlerType.BLOCKING)
	public USonarStatusResult handleSonarEventBlocking(@Header("x-sonar-webhook-hmac-sha256") String hmacValue, @Body SonarEvent sonarEvent, RoutingContext rc) {
		String sonarEventUUID = createSonarUUID();
		Log.infof("SonarEvent: '%s' | Received (blocking) hmac: '%s'", sonarEventUUID, hmacValue);
		System.out.println("XXXX");
		System.out.println(rc.body().asString());
		System.out.println("XXXX");
		if (isSonarHmacValid(hmacValue, rc.body().buffer().getBytes(), sonarEventUUID)) {
			SonarEventSimple simpleEvent = JsonUtils.toSonarEventSimple(sonarEvent, sonarEventUUID);
			return service.handleSonarEvent(simpleEvent);
		}
		USonarStatusResult result = service.createResult(null, sonarEventUUID, Source.SONAR);
		result.setError("Event skipped");
		return result;
	}

	private boolean isSonarHmacValid(String hmacValue, byte[] body, String sonarEventUUID) {
		if (hmacValue != null) {
			if (sonarWebhookSecret.isEmpty()) {
				Log.errorf("SonarEvent: '%s' | Got an value hmac value, but no secret is configured with the 'sonarqube.webhook.secret' configuration", sonarEventUUID);
				return false;
			} else {
				String expected = calculateHmac(body, sonarWebhookSecret.get(), sonarEventUUID);
				if (Objects.equals(expected, hmacValue)) {
					Log.debugf("SonarEvent: '%s' | Hmac value is correct", sonarEventUUID);
					return true;
				} else {
					Log.infof("SonarEvent: '%s' | Hmac value from sonar '%s' does not match with the expected one '%s'. Check the 'sonarqube.webhook.secret' configuration", sonarEventUUID, hmacValue, expected);
					return false;
				}
			}
		} else {
			if (sonarWebhookSecret.isEmpty()) {
				Log.debugf("SonarEvent: '%s' | No hmac value configured, no secret configured", sonarEventUUID);
				return true;
			} else {
				Log.warnf("SonarEvent: '%s' | No hmac value send (is a secret configured in Sonar?), but a secret is configuredwith the 'sonarqube.webhook.secret' configuration", sonarEventUUID);
				return false;
			}
		}
	}

	private static String calculateHmac(byte data[], String secret, String sonarEventUUID) {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKeySpec);
			return HexFormat.of().formatHex(mac.doFinal(data));
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			Log.errorf(e, "SonarEvent: '%s' | can not compute expected hmac value", sonarEventUUID);
			return "";
		}
	}

	@Route(path = "/u-sonar-status/sonar-replay", order = 1, methods = HttpMethod.POST, type = HandlerType.BLOCKING)
	public USonarStatusResult handleSonarEventReplay(@Body SonarEventSimple sonarSimple) {
		String sonarEventUUID = sonarSimple.getSonarEventUUID();
		if (sonarEventUUID == null) {
			sonarEventUUID = createUUID("replay-sonar-");
		}
		Log.infof("SonarEvent: '%s' | Replay", sonarEventUUID);
		return service.handleSonarEvent(sonarSimple);
	}

	@Route(path = "/u-sonar-status/gitlab", order = 1, methods = HttpMethod.POST, type = HandlerType.NORMAL)
	public USonarStatusResult handleGitLabEvent(@Body ExternalStatusCheckEvent externalStatusCheck, HttpServerResponse response) {
		String gitlabEventUUID = createGitLabUUID();
		Log.infof("GitlabEvent: '%s' | Received", gitlabEventUUID);
		GitLabEventSimple simpleEvent = JsonUtils.toGitLabEventSimple(externalStatusCheck, gitlabEventUUID);
		// consumed by Service class
		eventsBus.send(Service.GITLAB_EVENT, simpleEvent);
		response.setStatusCode(202);
		return service.createResult(gitlabEventUUID, null, Source.GITLAB);
	}

	@Route(path = "/u-sonar-status/gitlab-blocking", order = 1, methods = HttpMethod.POST, type = HandlerType.BLOCKING)
	public USonarStatusResult handleGitLabEventBlocking(@Body ExternalStatusCheckEvent externalStatusCheck) {
		String gitlabEventUUID = createGitLabUUID();
		Log.infof("GitlabEvent: '%s' | Received (blocking)", gitlabEventUUID);
		GitLabEventSimple simpleEvent = JsonUtils.toGitLabEventSimple(externalStatusCheck, gitlabEventUUID);
		return service.handleGitLabEvent(simpleEvent);
	}

	@Route(path = "/u-sonar-status/gitlab-replay", order = 1, methods = HttpMethod.POST, type = HandlerType.BLOCKING)
	public USonarStatusResult handleGitLabEventReplay(@Body GitLabEventSimple gitlabSimple) {
		String gitlabEventUUID = gitlabSimple.getGitlabEventUUID();
		if (gitlabEventUUID == null) {
			gitlabEventUUID = createUUID("replay-gitlab-");
		}
		Log.infof("GitlabEvent: '%s' | Replay", gitlabEventUUID);
		return service.handleGitLabEvent(gitlabSimple);
	}

	@Route(path = "/*", order = 2)
	public void other(RoutingContext rc) {
		String path = rc.request().path();
		if (path.startsWith("/q/health")) {
			// the module 'quarkus-smallrye-health' will answer:
			rc.next();
		} else {
			Log.infof("Invalid path '%s' ", path);

			USonarStatusResult result = service.createResult(null, null, null);
			result.setError("Invalid path: " + path);
			String body = Json.encode(result);
			rc.response()
					.setStatusCode(202)
					.end(body);
		}
	}

	@Route(path = "/*", order = 3, type = HandlerType.FAILURE)
	public USonarStatusResult error(RoutingContext rc) {
		Throwable t = rc.failure();
		Log.warnf(t, "Failed to handle request on path '%s' ", null, rc.request().path());

		USonarStatusResult result = service.createResult(null, null, null);
		if (t != null) {
			result.setError(t.getMessage());
		} else {
			result.setError("Unknown error");
		}
		rc.response().setStatusCode(202);
		return result;
	}

	private String createSonarUUID() {
		return createUUID("sonar-");
	}

	private String createGitLabUUID() {
		return createUUID("gitlab-");
	}

	private String createUUID(String prefix) {
		UUID uuid = UUID.randomUUID();
		return prefix + uuid;
	}
}

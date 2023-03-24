package com.unblu.usonarstatus.sonar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"name",
	"login",
	"avatar"
})
public class Author {

	@JsonProperty("name")
	private String name;
	@JsonProperty("login")
	private String login;
	@JsonProperty("avatar")
	private String avatar;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("login")
	public String getLogin() {
		return login;
	}

	@JsonProperty("login")
	public void setLogin(String login) {
		this.login = login;
	}

	@JsonProperty("avatar")
	public String getAvatar() {
		return avatar;
	}

	@JsonProperty("avatar")
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Override
	public String toString() {
		return "Author [name=" + name + ", login=" + login + ", avatar=" + avatar + "]";
	}

}
package com.unblu.usonarstatus.sonar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"sha",
	"author",
	"date",
	"message"
})
public class Commit {

	@JsonProperty("sha")
	private String sha;
	@JsonProperty("author")
	private Author author;
	@JsonProperty("date")
	private String date;
	@JsonProperty("message")
	private String message;

	@JsonProperty("sha")
	public String getSha() {
		return sha;
	}

	@JsonProperty("sha")
	public void setSha(String sha) {
		this.sha = sha;
	}

	@JsonProperty("author")
	public Author getAuthor() {
		return author;
	}

	@JsonProperty("author")
	public void setAuthor(Author author) {
		this.author = author;
	}

	@JsonProperty("date")
	public String getDate() {
		return date;
	}

	@JsonProperty("date")
	public void setDate(String date) {
		this.date = date;
	}

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Commit [sha=" + sha + ", author=" + author + ", date=" + date + ", message=" + message + "]";
	}
}
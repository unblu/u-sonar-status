package com.unblu.usonarstatus.sonar;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"name",
	"status",
	"conditions"
})
public class QualityGate {

	@JsonProperty("name")
	private String name;
	@JsonProperty("status")
	private String status;
	@JsonProperty("conditions")
	private List<Condition> conditions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public int hashCode() {
		return Objects.hash(conditions, name, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QualityGate other = (QualityGate) obj;
		return Objects.equals(conditions, other.conditions) && Objects.equals(name, other.name) && Objects.equals(status, other.status);
	}

	@Override
	public String toString() {
		return "QualityGate [name=" + name + ", status=" + status + ", conditions=" + conditions + "]";
	}

}
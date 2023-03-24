
package com.unblu.usonarstatus.sonar;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"metric",
	"operator",
	"value",
	"status",
	"errorThreshold"
})
public class Condition {

	@JsonProperty("metric")
	private String metric;
	@JsonProperty("operator")
	private String operator;
	@JsonProperty("value")
	private String value;
	@JsonProperty("status")
	private String status;
	@JsonProperty("errorThreshold")
	private String errorThreshold;

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorThreshold() {
		return errorThreshold;
	}

	public void setErrorThreshold(String errorThreshold) {
		this.errorThreshold = errorThreshold;
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorThreshold, metric, operator, status, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Condition other = (Condition) obj;
		return Objects.equals(errorThreshold, other.errorThreshold) && Objects.equals(metric, other.metric) && Objects.equals(operator, other.operator) && Objects.equals(status, other.status) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Condition [metric=" + metric + ", operator=" + operator + ", value=" + value + ", status=" + status + ", errorThreshold=" + errorThreshold + "]";
	}

}
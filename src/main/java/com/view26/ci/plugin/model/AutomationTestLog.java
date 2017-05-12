package com.view26.ci.plugin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.tasks.junit.CaseResult;

/**
 * @author aneeshia
 */
public class AutomationTestLog {
  @JsonProperty("description")
  private String description;
  @JsonProperty("expected_result")
  private String expectedResult;
  private Integer order;
  private String status;

  public AutomationTestLog() {
  }

  public AutomationTestLog(String description, String expectedResult, Integer order, String status) {
    this.description = description;
    this.expectedResult = expectedResult;
    this.order = order;
    this.status = status;
  }

  public AutomationTestLog(CaseResult caseResult) {
    this.setDescription(caseResult.getName());
    this.setExpectedResult(caseResult.getName());
    this.setStatus(caseResult.getStatus().toString());
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getExpectedResult() {
    return expectedResult;
  }

  public void setExpectedResult(String expectedResult) {
    this.expectedResult = expectedResult;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AutomationTestLog that = (AutomationTestLog) o;

    if (description != null ? !description.equals(that.description) : that.description != null)
      return false;
    if (expectedResult != null ? !expectedResult.equals(that.expectedResult) : that.expectedResult != null)
      return false;
    return !(status != null ? !status.equals(that.status) : that.status != null);

  }

  @Override public int hashCode() {
    int result = description != null ? description.hashCode() : 0;
    result = 31 * result + (expectedResult != null ? expectedResult.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }
}

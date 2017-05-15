package com.view26.ci.plugin.model.view26;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author aneeshia
 * @version 12/24/2015 12:25 AM aneeshia $
 * @since 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmittedTask {
  private long id;
  private String type;
  private String state;

  public long getId() {
    return id;
  }

  public SubmittedTask setId(long id) {
    this.id = id;
    return this;
  }

  public String getType() {
    return type;
  }

  public SubmittedTask setType(String type) {
    this.type = type;
    return this;
  }

  public String getState() {
    return state;
  }

  public SubmittedTask setState(String state) {
    this.state = state;
    return this;
  }
}

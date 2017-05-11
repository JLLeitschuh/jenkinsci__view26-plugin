package com.view26.ci.plugin.parse;

import com.view26.ci.plugin.model.Configuration;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * @author trongle
 * @version 12/14/2015 10:10 AM trongle $
 * @since 1.0
 */
public class ParseRequest {
  private AbstractBuild build;
  private Configuration configuration;
  private Launcher launcher;
  private BuildListener listener;
  private Boolean isMavenProject;

  public AbstractBuild getBuild() {
    return build;
  }

  public ParseRequest setBuild(AbstractBuild build) {
    this.build = build;
    isMavenProject = null == build ? false : build.getProject().getClass().getName().toLowerCase().contains("maven");
    return this;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public ParseRequest setConfiguration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  public Launcher getLauncher() {
    return launcher;
  }

  public ParseRequest setLauncher(Launcher launcher) {
    this.launcher = launcher;
    return this;
  }

  public BuildListener getListener() {
    return listener;
  }

  public ParseRequest setListener(BuildListener listener) {
    this.listener = listener;
    return this;
  }

  public Boolean isMavenProject() {
    return isMavenProject;
  }
  
}

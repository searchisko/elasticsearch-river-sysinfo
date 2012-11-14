/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.action.admin.cluster.health.RestClusterHealthAction;
import org.elasticsearch.rest.action.admin.cluster.state.RestClusterStateAction;
import org.jboss.elasticsearch.river.sysinfo.SourceClient;
import org.jboss.elasticsearch.river.sysinfo.SourceClientBase;

/**
 * {@link SourceClient} implementation using {@link Client} instance.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientESClient extends SourceClientBase {

  protected Client client;

  private RestClusterHealthAction healthAction;
  private RestClusterStateAction stateAction;

  /**
   * @param client ES cluster to be used for calls
   */
  public SourceClientESClient(Client client) {
    this.client = client;
    Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
    RestController controller = new RestController(settings);
    healthAction = new RestClusterHealthAction(settings, client, controller);
    stateAction = new RestClusterStateAction(settings, client, controller, null);
  }

  @Override
  protected String readClusterStateInfo(Map<String, String> params) throws IOException, InterruptedException {
    return performRestRequestLocally(stateAction, params);
  }

  @Override
  protected String readClusterHealthInfo(Map<String, String> params) throws IOException, InterruptedException {
    return performRestRequestLocally(healthAction, params);
  }

  private String performRestRequestLocally(RestHandler handler, Map<String, String> params) throws IOException,
      InterruptedException {
    LocalRestChannel channel = new LocalRestChannel();
    handler.handleRequest(new LocalRestRequest(params), channel);
    return channel.getResponseContent();
  }

}

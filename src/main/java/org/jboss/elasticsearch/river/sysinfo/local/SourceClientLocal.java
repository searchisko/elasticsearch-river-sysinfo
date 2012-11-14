/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.local;

import java.io.IOException;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.admin.cluster.health.RestClusterHealthAction;
import org.elasticsearch.rest.action.admin.cluster.state.RestClusterStateAction;
import org.jboss.elasticsearch.river.sysinfo.SourceClient;
import org.jboss.elasticsearch.river.sysinfo.SourceClientBase;

/**
 * {@link SourceClient} implementation using {@link Client} instance.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientLocal extends SourceClientBase {

  protected Client client;

  private RestClusterHealthAction healthAction;
  private RestClusterStateAction stateAction;

  /**
   * @param client ES cluster to be used for calls
   */
  public SourceClientLocal(Client client) {
    this.client = client;
    Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
    RestController controller = new RestController(settings);
    healthAction = new RestClusterHealthAction(settings, client, controller);
    stateAction = new RestClusterStateAction(settings, client, controller, null);
  }

  @Override
  protected String readStateInfo() throws IOException, InterruptedException {
    return performRestRequestLocally(stateAction, null);
  }

  @Override
  protected String readHealthInfo() throws IOException, InterruptedException {
    LocalRestRequest request = new LocalRestRequest();
    request.addParam("level", "shards");
    return performRestRequestLocally(healthAction, request);
  }

  private String performRestRequestLocally(RestHandler handler, RestRequest request) throws IOException,
      InterruptedException {
    if (request == null)
      request = new LocalRestRequest();
    LocalRestChannel channel = new LocalRestChannel();
    handler.handleRequest(request, channel);
    return channel.getResponseContent();
  }

}

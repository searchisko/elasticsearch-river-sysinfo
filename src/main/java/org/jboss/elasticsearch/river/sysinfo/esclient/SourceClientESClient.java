/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.action.admin.cluster.health.RestClusterHealthAction;
import org.elasticsearch.rest.action.admin.cluster.node.info.RestNodesInfoAction;
import org.elasticsearch.rest.action.admin.cluster.state.RestClusterStateAction;
import org.jboss.elasticsearch.river.sysinfo.SourceClient;
import org.jboss.elasticsearch.river.sysinfo.SourceClientBase;

/**
 * {@link SourceClient} implementation using passed in {@link Client} instance.
 * <p>
 * Use next section in river configuration if you want to process informations from local ES cluster:
 * 
 * <pre>
 * "es_connection" : {
 *   "type" : "local"
 * }
 * </pre>
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientESClient extends SourceClientBase {

  private static final ESLogger logger = Loggers.getLogger(SourceClientESClient.class);

  protected Client client;

  private RestClusterHealthAction healthAction;
  private RestClusterStateAction stateAction;
  private RestNodesInfoAction nodesInfoAction;

  /**
   * @param client ES cluster to be used for calls
   */
  public SourceClientESClient(Client client) {
    this.client = client;
    Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
    SettingsFilter settingsFilter = new SettingsFilter(settings);
    RestController controller = new RestController(settings);
    healthAction = new RestClusterHealthAction(settings, client, controller);
    stateAction = new RestClusterStateAction(settings, client, controller, settingsFilter);
    nodesInfoAction = new RestNodesInfoAction(settings, client, controller, settingsFilter);
  }

  @Override
  protected String readClusterStateInfo(Map<String, String> params) throws IOException, InterruptedException {
    logger.debug("readClusterStateInfo with params {}", params);
    return performRestRequestLocally(stateAction, params);
  }

  @Override
  protected String readClusterHealthInfo(Map<String, String> params) throws IOException, InterruptedException {
    logger.debug("readClusterHealthInfo with params {}", params);
    return performRestRequestLocally(healthAction, params);
  }

  @Override
  protected String readClusterNodesInfoInfo(Map<String, String> params) throws IOException, InterruptedException {
    logger.debug("readClusterNodesInfoInfo with params {}", params);
    return performRestRequestLocally(nodesInfoAction, params);
  }

  private String performRestRequestLocally(RestHandler handler, Map<String, String> params) throws IOException,
      InterruptedException {
    LocalRestChannel channel = new LocalRestChannel();
    handler.handleRequest(new LocalRestRequest(params), channel);
    String res = channel.getResponseContent();
    logger.debug("performRestRequestLocally response {}", res);
    return res;
  }

  @Override
  public void start() {
  }

  @Override
  public void close() {
  }

}

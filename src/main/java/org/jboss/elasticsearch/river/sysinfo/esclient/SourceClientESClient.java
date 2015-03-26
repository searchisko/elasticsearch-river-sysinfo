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
import org.elasticsearch.rest.action.admin.cluster.node.stats.RestNodesStatsAction;
import org.elasticsearch.rest.action.admin.cluster.state.RestClusterStateAction;
import org.elasticsearch.rest.action.admin.cluster.stats.RestClusterStatsAction;
import org.elasticsearch.rest.action.admin.indices.recovery.RestRecoveryAction;
import org.elasticsearch.rest.action.admin.indices.segments.RestIndicesSegmentsAction;
import org.elasticsearch.rest.action.admin.indices.stats.RestIndicesStatsAction;
import org.elasticsearch.rest.action.admin.indices.status.RestIndicesStatusAction;
import org.jboss.elasticsearch.river.sysinfo.SourceClient;
import org.jboss.elasticsearch.river.sysinfo.SourceClientBase;

/**
 * {@link SourceClient} implementation using passed in {@link Client} instance.
 * <p>
 * Use next section in river configuration if you want to process information from local ES cluster:
 * 
 * <pre>
 * "es_connection" : {
 *   "type" : "local"
 * }
 * </pre>
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 * @author Lukas Vlcek
 */
public class SourceClientESClient extends SourceClientBase {

	private static final ESLogger logger = Loggers.getLogger(SourceClientESClient.class);

	protected Client client;

	private RestClusterHealthAction clusterHealthAction;
	private RestClusterStateAction clusterStateAction;
	private RestClusterStatsAction clusterStatsAction;
	private RestNodesInfoAction nodesInfoAction;
	private RestNodesStatsAction nodesStatsAction;
	private RestIndicesStatusAction indicesStatusAction;
	private RestIndicesStatsAction indicesStatsAction;
	private RestIndicesSegmentsAction indicesSegmentsAction;
	private RestRecoveryAction indicesRecoveryAction;

	/**
	 * @param client ES cluster to be used for calls
	 */
	public SourceClientESClient(Client client) {
		this.client = client;
		Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
		SettingsFilter settingsFilter = new SettingsFilter(settings);
		RestController controller = new RestController(settings);
		clusterHealthAction = new RestClusterHealthAction(settings, controller, client);
		clusterStateAction = new RestClusterStateAction(settings, controller, client, settingsFilter);
		clusterStatsAction = new RestClusterStatsAction(settings, controller, client);
		nodesInfoAction = new RestNodesInfoAction(settings, controller, client, settingsFilter);
		nodesStatsAction = new RestNodesStatsAction(settings, controller, client);
		indicesStatusAction = new RestIndicesStatusAction(settings, controller, client, settingsFilter);
		indicesStatsAction = new RestIndicesStatsAction(settings, controller, client);
		indicesSegmentsAction = new RestIndicesSegmentsAction(settings, controller, client);
		indicesRecoveryAction = new RestRecoveryAction(settings, controller, client);
	}

	@Override
	protected String readClusterStateInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readClusterStateInfo with params {}", params);
		return performRestRequestLocally(clusterStateAction, params);
	}

	@Override
	protected String readClusterHealthInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readClusterHealthInfo with params {}", params);
		return performRestRequestLocally(clusterHealthAction, params);
	}

	@Override
	protected String readClusterStatsInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readClusterStatsInfo with params {}", params);
		return performRestRequestLocally(clusterStatsAction, params);
	}

	@Override
	protected String readClusterNodesInfoInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readClusterNodesInfoInfo with params {}", params);
		return performRestRequestLocally(nodesInfoAction, params);
	}

	@Override
	protected String readClusterNodesStatsInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readClusterNodesStatsInfo with params {}", params);
		return performRestRequestLocally(nodesStatsAction, params);
	}

	@Override
	protected String readIndicesStatusInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readIndicesStatusInfo with params {}", params);
		return performRestRequestLocally(indicesStatusAction, params);
	}

	@Override
	protected String readIndicesStatsInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readIndicesStatsInfo with params {}", params);
		return performRestRequestLocally(indicesStatsAction, params);
	}

	@Override
	protected String readIndicesSegmentsInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readIndicesSegmentsInfo with params {}", params);
		return performRestRequestLocally(indicesSegmentsAction, params);
	}

	@Override
	protected String readIndicesRecoveryInfo(Map<String, String> params) throws IOException, InterruptedException {
		logger.debug("readIndicesRecoveryInfo with params {}", params);
		return performRestRequestLocally(indicesRecoveryAction, params);
	}

	private String performRestRequestLocally(RestHandler handler, Map<String, String> params) throws IOException,
			InterruptedException {
		LocalRestRequest request = new LocalRestRequest(params);
		LocalRestChannel channel = new LocalRestChannel(request);
		try {
			handler.handleRequest(request, channel);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e.getCause());
		}
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

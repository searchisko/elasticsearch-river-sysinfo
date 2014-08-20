/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.jboss.elasticsearch.river.sysinfo.IRiverMgm;
import org.jboss.elasticsearch.river.sysinfo.mgm.TransportJRMgmBaseAction;

/**
 * SysinfoRiver period change method transport action.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class TransportJRPeriodAction extends
		TransportJRMgmBaseAction<JRPeriodRequest, JRPeriodResponse, NodeJRPeriodRequest, NodeJRPeriodResponse> {

	@Inject
	public TransportJRPeriodAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService) {
		super(settings, JRPeriodAction.NAME, clusterName, threadPool, clusterService, transportService);
	}

	@Override
	protected NodeJRPeriodResponse performOperationOnRiver(IRiverMgm river, JRPeriodRequest req, DiscoveryNode node)
			throws Exception {
		logger.debug("Go to perform period change to {} for indexers {} on river '{}'", req.period, req.indexerNames,
				req.getRiverName());
		boolean ret = river.changeIndexerPeriod(req.indexerNames, req.period);
		return new NodeJRPeriodResponse(node, true, ret);
	}

	@Override
	protected JRPeriodRequest newRequest() {
		return new JRPeriodRequest();
	}

	@Override
	protected NodeJRPeriodRequest newNodeRequest() {
		return new NodeJRPeriodRequest();
	}

	@Override
	protected NodeJRPeriodRequest newNodeRequest(String nodeId, JRPeriodRequest request) {
		return new NodeJRPeriodRequest(nodeId, request);
	}

	@Override
	protected NodeJRPeriodResponse newNodeResponse() {
		return new NodeJRPeriodResponse(clusterService.localNode());
	}

	@Override
	protected NodeJRPeriodResponse[] newNodeResponseArray(int len) {
		return new NodeJRPeriodResponse[len];
	}

	@Override
	protected JRPeriodResponse newResponse(ClusterName clusterName, NodeJRPeriodResponse[] array) {
		return new JRPeriodResponse(clusterName, array);
	}

}

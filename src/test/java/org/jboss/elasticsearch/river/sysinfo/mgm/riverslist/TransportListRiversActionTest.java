/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.riverslist;

import junit.framework.Assert;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.DummyTransportAddress;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.TransportService;
import org.jboss.elasticsearch.river.sysinfo.IRiverMgm;
import org.jboss.elasticsearch.river.sysinfo.SysinfoRiver;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link TransportListRiversAction}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class TransportListRiversActionTest {

	private static final String RIVER_NAME_2 = "myRiver2";
	private static final String RIVER_NAME_1 = "myRiver";
	public static final ClusterName clusterName = new ClusterName("myCluster");

	@Test
	public void newRequest() {
		TransportListRiversAction tested = prepareTestedInstance(clusterName);
		Assert.assertNotNull(tested.newRequest());
	}

	@SuppressWarnings("unused")
	@Test
	public void newNodeRequest() {
		TransportListRiversAction tested = prepareTestedInstance(clusterName);

		{
			Assert.assertNotNull(tested.newNodeRequest());
		}

		{
			ListRiversRequest request = new ListRiversRequest();
			NodeListRiversRequest nodeReq = tested.newNodeRequest("myNodeId", request);
		}
	}

	@Test
	public void newNodeResponse() {
		TransportListRiversAction tested = prepareTestedInstance(clusterName);
		Mockito.when(clusterService.localNode()).thenReturn(dn);

		NodeListRiversResponse resp = tested.newNodeResponse();
		Assert.assertNotNull(resp);
		Assert.assertEquals(dn, resp.getNode());
	}

	@Test
	public void nodeOperation() throws Exception {

		SysinfoRiver.clearRunningInstances();

		TransportListRiversAction tested = prepareTestedInstance(clusterName);
		try {
			{
				NodeListRiversRequest req = Mockito.mock(NodeListRiversRequest.class);
				NodeListRiversResponse resp = tested.nodeOperation(req);
				Assert.assertNotNull(resp);
				Assert.assertNotNull(resp.riverNames);
				Assert.assertEquals(0, resp.riverNames.size());
			}

			{
				IRiverMgm riverMock = Mockito.mock(IRiverMgm.class);
				RiverName riverName = new RiverName("sysinfo", RIVER_NAME_1);
				Mockito.when(riverMock.riverName()).thenReturn(riverName);
				SysinfoRiver.addRunningInstance(riverMock);
				NodeListRiversRequest req = Mockito.mock(NodeListRiversRequest.class);
				NodeListRiversResponse resp = tested.nodeOperation(req);
				Assert.assertNotNull(resp);
				Assert.assertNotNull(resp.riverNames);
				Assert.assertEquals(1, resp.riverNames.size());
				Assert.assertTrue(resp.riverNames.contains(RIVER_NAME_1));
			}

			{
				IRiverMgm jiraRiverMock = Mockito.mock(IRiverMgm.class);
				RiverName riverName = new RiverName("sysinfo", RIVER_NAME_2);
				Mockito.when(jiraRiverMock.riverName()).thenReturn(riverName);
				SysinfoRiver.addRunningInstance(jiraRiverMock);
				NodeListRiversRequest req = Mockito.mock(NodeListRiversRequest.class);
				NodeListRiversResponse resp = tested.nodeOperation(req);
				Assert.assertNotNull(resp);
				Assert.assertNotNull(resp.riverNames);
				Assert.assertEquals(2, resp.riverNames.size());
				Assert.assertTrue(resp.riverNames.contains(RIVER_NAME_1));
				Assert.assertTrue(resp.riverNames.contains(RIVER_NAME_2));
			}
		} finally {
			SysinfoRiver.clearRunningInstances();
		}
	}

	private static DiscoveryNode dn = new DiscoveryNode("aa", DummyTransportAddress.INSTANCE, Version.CURRENT);
	private static ClusterService clusterService = Mockito.mock(ClusterService.class);

	public static TransportListRiversAction prepareTestedInstance(ClusterName clusterName) {
		Settings settings = Mockito.mock(Settings.class);
		ThreadPool threadPool = new ThreadPool("tp");
		TransportService transportService = new TransportService(Mockito.mock(Transport.class), threadPool);
		TransportListRiversAction tested = new TransportListRiversAction(settings, clusterName, threadPool, clusterService,
				transportService);
		return tested;
	}
}

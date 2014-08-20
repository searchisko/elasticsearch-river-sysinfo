/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.DummyTransportAddress;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.TransportService;
import org.jboss.elasticsearch.river.sysinfo.IRiverMgm;
import org.jboss.elasticsearch.river.sysinfo.mgm.TransportJRMgmBaseAction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link TransportJRPeriodAction} and {@link TransportJRMgmBaseAction}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class TransportJRPeriodActionTest {

	public static final ClusterName clusterName = new ClusterName("myCluster");

	@Test
	public void newRequest() {
		TransportJRPeriodAction tested = prepareTestedInstance(clusterName);
		Assert.assertNotNull(tested.newRequest());
	}

	@Test
	public void newNodeRequest() {
		TransportJRPeriodAction tested = prepareTestedInstance(clusterName);

		{
			Assert.assertNotNull(tested.newNodeRequest());
		}

		{
			JRPeriodRequest request = new JRPeriodRequest();
			NodeJRPeriodRequest nodeReq = tested.newNodeRequest("myNodeId", request);
			Assert.assertEquals(request, nodeReq.getRequest());
		}
	}

	@Test
	public void newNodeResponse() {
		TransportJRPeriodAction tested = prepareTestedInstance(clusterName);
		Mockito.when(clusterService.localNode()).thenReturn(dn);

		NodeJRPeriodResponse resp = tested.newNodeResponse();
		Assert.assertNotNull(resp);
		Assert.assertEquals(dn, resp.getNode());
	}

	@Test
	public void newNodeResponseArray() {
		TransportJRPeriodAction tested = prepareTestedInstance(clusterName);
		NodeJRPeriodResponse[] array = tested.newNodeResponseArray(2);
		Assert.assertNotNull(array);
		Assert.assertEquals(2, array.length);
	}

	@Test
	public void newResponse() {
		TransportJRPeriodAction tested = prepareTestedInstance(clusterName);

		NodeJRPeriodResponse[] array = new NodeJRPeriodResponse[0];
		JRPeriodResponse resp = tested.newResponse(clusterName, array);
		Assert.assertNotNull(resp);
		Assert.assertEquals(resp.getClusterName(), clusterName);
		Assert.assertArrayEquals(resp.getNodes(), array);

	}

	@Test
	public void performOperationOnJiraRiver() throws Exception {

		TransportJRPeriodAction tested = prepareTestedInstance(clusterName);

		IRiverMgm river = Mockito.mock(IRiverMgm.class);

		{
			JRPeriodRequest req = new JRPeriodRequest("myriver", new String[] { "idxr1" }, 1300);
			Mockito.when(river.changeIndexerPeriod(new String[] { "idxr1" }, 1300)).thenReturn(true);
			NodeJRPeriodResponse resp = tested.performOperationOnRiver(river, req, dn);
			Assert.assertNotNull(resp);
			Assert.assertTrue(resp.isRiverFound());
			Assert.assertTrue(resp.isIndexerFound());
			Assert.assertEquals(dn, resp.getNode());
			Mockito.verify(river).changeIndexerPeriod(new String[] { "idxr1" }, 1300);
			Mockito.verifyNoMoreInteractions(river);
		}

		Mockito.reset(river);
		{
			JRPeriodRequest req = new JRPeriodRequest("myriver", new String[] { "idxr1" }, 1300);
			Mockito.when(river.changeIndexerPeriod(new String[] { "idxr1" }, 1300)).thenReturn(false);
			NodeJRPeriodResponse resp = tested.performOperationOnRiver(river, req, dn);
			Assert.assertNotNull(resp);
			Assert.assertTrue(resp.isRiverFound());
			Assert.assertFalse(resp.isIndexerFound());
			Assert.assertEquals(dn, resp.getNode());
			Mockito.verify(river).changeIndexerPeriod(new String[] { "idxr1" }, 1300);
			Mockito.verifyNoMoreInteractions(river);
		}

	}

	private static DiscoveryNode dn = new DiscoveryNode("aa", DummyTransportAddress.INSTANCE, Version.CURRENT);
	private static ClusterService clusterService = Mockito.mock(ClusterService.class);

	public static TransportJRPeriodAction prepareTestedInstance(ClusterName clusterName) {
		Settings settings = Mockito.mock(Settings.class);
		ThreadPool threadPool = new ThreadPool("tp");
		TransportService transportService = new TransportService(Mockito.mock(Transport.class), threadPool);
		TransportJRPeriodAction tested = new TransportJRPeriodAction(settings, clusterName, threadPool, clusterService,
				transportService);
		return tested;
	}
}

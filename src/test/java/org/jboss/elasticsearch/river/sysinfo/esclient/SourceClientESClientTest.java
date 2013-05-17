/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.elasticsearch.client.Client;
import org.jboss.elasticsearch.river.sysinfo.testtools.ESRealClientTestBase;
import org.junit.Test;

/**
 * Unit test for {@link SourceClientESClient}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientESClientTest extends ESRealClientTestBase {

	@Test
	public synchronized void readClusterStateInfo() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			String info = tested.readClusterStateInfo(null);
			assertStartsWith("{\"cluster_name\":\"elasticsearch\",\"master_node\":\"", info);

		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readClusterHealthInfo() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			String info = tested.readClusterHealthInfo(null);
			assertStartsWith("{\"cluster_name\":\"elasticsearch\",\"status\":", info);

		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readClusterNodesInfoInfo() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			String info = tested.readClusterNodesInfoInfo(null);
			assertStartsWith("{\"ok\":true,\"cluster_name\":\"elasticsearch\",\"nodes\":{", info);

		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readClusterNodesStatsInfo() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			String info = tested.readClusterNodesStatsInfo(null);
			assertStartsWith("{\"cluster_name\":\"elasticsearch\",\"nodes\":{", info);

		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readIndicesStatusInfo() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			String info = tested.readIndicesStatusInfo(null);
			assertStartsWith("{\"ok\":true,\"_shards\":{\"total\":0,\"successful\":0,\"failed\":0},\"indices\":{}}", info);

			Map<String, String> params = new HashMap<String, String>();
			params.put("index", "test");
			info = tested.readIndicesStatusInfo(params);
			Assert.fail("IOException must be thrown due missing index");
		} catch (IOException e) {
			Assert
					.assertEquals(
							"response status is NOT_FOUND with content {\"error\":\"IndexMissingException[[test] missing]\",\"status\":404}",
							e.getMessage());
		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readIndicesStatsInfo() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			String info = tested.readIndicesStatsInfo(null);
			assertStartsWith(
					"{\"ok\":true,\"_shards\":{\"total\":0,\"successful\":0,\"failed\":0},\"_all\":{\"primaries\":{},\"total\":{}},\"indices\":{}}",
					info);

			Map<String, String> params = new HashMap<String, String>();
			params.put("index", "test");
			info = tested.readIndicesStatsInfo(params);
			Assert.fail("IOException must be thrown due missing index");
		} catch (IOException e) {
			Assert
					.assertEquals(
							"response status is NOT_FOUND with content {\"error\":\"IndexMissingException[[test] missing]\",\"status\":404}",
							e.getMessage());
		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readIndicesSegmentsInfo() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			String info = tested.readIndicesSegmentsInfo(null);
			assertStartsWith("{\"ok\":true,\"_shards\":{\"total\":0,\"successful\":0,\"failed\":0},\"indices\":{}}", info);

			Map<String, String> params = new HashMap<String, String>();
			params.put("index", "test");
			info = tested.readIndicesSegmentsInfo(params);
			Assert.fail("IOException must be thrown due missing index");
		} catch (IOException e) {
			Assert
					.assertEquals(
							"response status is NOT_FOUND with content {\"error\":\"IndexMissingException[[test] missing]\",\"status\":404}",
							e.getMessage());
		} finally {
			finalizeESClientForUnitTest();
		}
	}

	protected void assertStartsWith(String expected, String actual) {
		if (expected == null && actual == null)
			return;

		if (actual != null && expected != null && actual.length() >= expected.length()) {
			actual = actual.substring(0, expected.length());
		}
		Assert.assertEquals("Expected start with failed: ", expected, actual);
	}

}

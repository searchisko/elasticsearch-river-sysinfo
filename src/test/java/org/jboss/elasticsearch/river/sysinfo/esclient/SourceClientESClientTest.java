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
			System.out.println(info);
			assertStartsWith("{\"cluster_name\":\"elasticsearch\",\"version\":2,\"master_node\":\"", info);

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
			System.out.println(info);
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
			System.out.println(info);
			assertStartsWith("{\"cluster_name\":\"elasticsearch\",\"nodes\":{", info);

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
			System.out.println(info);
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
			System.out.println(info);
			assertStartsWith("{\"_shards\":{\"total\":0,\"successful\":0,\"failed\":0},\"indices\":{}}", info);

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
			System.out.println(info);
			assertStartsWith(
					"{\"_shards\":{\"total\":0,\"successful\":0,\"failed\":0},\"_all\":{\"primaries\":{},\"total\":{}},\"indices\":{}}",
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
	public synchronized void readIndicesSegmentsInfo_allIndices() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			indexCreate("test_index");

			String info = tested.readIndicesSegmentsInfo(null);
			System.out.println(info);
			assertStartsWith("{\"_shards\":{\"total\":10,\"successful\":5,\"failed\":0},\"indices\":{\"test_index\":{\"",
					info);

		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readIndicesSegmentsInfo_missingAllIndices() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			tested.readIndicesSegmentsInfo(null);
			Assert.fail("IOException must be thrown due missing index");
		} catch (IOException e) {
			Assert
					.assertEquals(
							"response status is NOT_FOUND with content {\"error\":\"IndexMissingException[[_all] missing]\",\"status\":404}",
							e.getMessage());
		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public synchronized void readIndicesSegmentsInfo_missingOneIndex() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			Map<String, String> params = new HashMap<String, String>();
			params.put("index", "test");
			tested.readIndicesSegmentsInfo(params);
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
	public synchronized void readIndicesRecoveryInfo_allIndices() throws Exception {
		try {
			Client client = prepareESClientForUnitTest();

			SourceClientESClient tested = new SourceClientESClient(client);

			indexCreate("test_index");

			String info = tested.readIndicesRecoveryInfo(null);
			System.out.println(info);
			assertStartsWith("{\"test_index\":{\"shards\":[{\"id\":0,\"type\":\"GATEWAY\"", info);

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

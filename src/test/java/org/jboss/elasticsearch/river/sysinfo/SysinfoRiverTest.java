/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.jboss.elasticsearch.river.sysinfo.esclient.SourceClientESClient;
import org.jboss.elasticsearch.river.sysinfo.esclient.SourceClientESTransportClient;
import org.jboss.elasticsearch.river.sysinfo.testtools.ESRealClientTestBase;
import org.jboss.elasticsearch.river.sysinfo.testtools.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link SysinfoRiver}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoRiverTest extends ESRealClientTestBase {

	private static final String RIVER_NAME = "my_sysinfo_river";

	@SuppressWarnings("unchecked")
	@Test
	public void configure_sourceClient() throws Exception {

		// case - local es_connection
		{
			Map<String, Object> settings = Utils.loadJSONFromJarPackagedFile("/river_configuration_test_conn_local.json");
			SysinfoRiver tested = prepareRiverInstanceForTest(null);
			tested.configure(settings);
			Assert.assertEquals(SourceClientESClient.class, tested.sourceClient.getClass());
		}

		// case - remote es_connection
		{
			Map<String, Object> settings = Utils.loadJSONFromJarPackagedFile("/river_configuration_test_conn_remote.json");
			SysinfoRiver tested = prepareRiverInstanceForTest(null);
			tested.configure(settings);
			Assert.assertEquals(SourceClientESTransportClient.class, tested.sourceClient.getClass());
			Assert.assertEquals(2, ((SourceClientESTransportClient) tested.sourceClient).getTransportAddresses().length);
		}

		// case - REST es_connection
		{
			Map<String, Object> settings = Utils.loadJSONFromJarPackagedFile("/river_configuration_test_conn_rest.json");
			SysinfoRiver tested = prepareRiverInstanceForTest(null);
			tested.configure(settings);
			Assert.assertEquals(SourceClientREST.class, tested.sourceClient.getClass());
			Assert.assertEquals("http://localhost:9200/", ((SourceClientREST) tested.sourceClient).restAPIUrlBase);
		}

		// case - invalid es_connection
		{
			try {
				Map<String, Object> settings = Utils.loadJSONFromJarPackagedFile("/river_configuration_test_conn_local.json");
				((Map<String, Object>) settings.get("es_connection")).put("type", "nonsense");
				SysinfoRiver tested = prepareRiverInstanceForTest(null);
				tested.configure(settings);
				Assert.fail("SettingsException must be thrown");
			} catch (SettingsException e) {
				// OK
			}
		}
	}

	@Test
	public void configure_indexers() throws Exception {

		// case - missing indexers
		{
			try {
				Map<String, Object> settings = Utils.loadJSONFromJarPackagedFile("/river_configuration_test_conn_local.json");
				settings.remove("indexers");
				SysinfoRiver tested = prepareRiverInstanceForTest(null);
				tested.configure(settings);
				Assert.fail("SettingsException must be thrown");
			} catch (SettingsException e) {
				// OK
			}
		}

		// case - empty indexers
		{
			try {
				Map<String, Object> settings = Utils.loadJSONFromJarPackagedFile("/river_configuration_test_conn_local.json");
				((Map<?, ?>) settings.get("indexers")).clear();
				SysinfoRiver tested = prepareRiverInstanceForTest(null);
				tested.configure(settings);
				Assert.fail("SettingsException must be thrown");
			} catch (SettingsException e) {
				// OK
			}
		}

		// case - OK read
		{
			Map<String, Object> settings = Utils.loadJSONFromJarPackagedFile("/river_configuration_test_conn_local.json");
			SysinfoRiver tested = prepareRiverInstanceForTest(null);
			tested.configure(settings);
			Assert.assertEquals(8, tested.indexers.size());
			SysinfoIndexer idxr = tested.indexers.get("cluster_health");
			Assert.assertEquals("cluster_health", idxr.name);
			Assert.assertEquals(tested.sourceClient, idxr.sourceClient);
			Assert.assertEquals(tested.client, idxr.targetClient);
			Assert.assertEquals(SysinfoType.CLUSTER_HEALTH, idxr.infoType);
			Assert.assertEquals("my_sysinfo_index", idxr.indexName);
			Assert.assertEquals("cluster_health", idxr.typeName);
			Assert.assertEquals(30 * 1000, idxr.indexingPeriod);
			Assert.assertNotNull(idxr.params);
		}

	}

	@Test
	public void start() throws Exception {
		SysinfoRiver tested = prepareRiverInstanceForTest(null);
		SourceClient scMock = tested.sourceClient;
		SysinfoRiver.clearRunningInstances();

		// case - exception if started already
		{
			tested.closed = false;
			try {
				tested.start();
				Assert.fail("IllegalStateException must be thrown");
			} catch (IllegalStateException e) {
				// OK
				Assert.assertFalse(tested.closed);
				Assert.assertEquals(0, tested.indexerThreads.size());
				Mockito.verifyZeroInteractions(scMock);
				Assert.assertEquals(0, SysinfoRiver.getRunningInstances().size());
			}
		}

		// case - no exception if indexers list is empty
		{
			Mockito.reset(scMock);
			tested.closed = true;
			tested.start();
			Assert.assertFalse(tested.closed);
			Assert.assertEquals(0, tested.indexerThreads.size());
			Mockito.verify(scMock).start();
			Assert.assertEquals(1, SysinfoRiver.getRunningInstances().size());
		}

		// case - check threads created and started for indexers
		{
			Mockito.reset(scMock);
			tested.closed = true;
			tested.indexers.put("ch", indexerMock("ch", SysinfoType.CLUSTER_HEALTH));
			tested.indexers.put("cs", indexerMock("cs", SysinfoType.CLUSTER_STATE));
			tested.start();
			Assert.assertFalse(tested.closed);
			Assert.assertEquals(2, tested.indexerThreads.size());
			Mockito.verify(scMock).start();
			// give threads chance to run so we can do next assertion on this method
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// nothing to do
			}
			for (SysinfoIndexer i : tested.indexers.values()) {
				Mockito.verify(i).run();
			}
		}
		SysinfoRiver.clearRunningInstances();
	}

	@Test
	public void close() throws Exception {
		SysinfoRiver tested = prepareRiverInstanceForTest(null);
		SourceClient scMock = tested.sourceClient;
		SysinfoRiver.clearRunningInstances();
		SysinfoRiver.addRunningInstance(tested);
		try {
			// case - no errors on empty indexers list and unstarted river
			{
				tested.close();
				Assert.assertTrue(tested.closed);
				Mockito.verify(scMock).close();
				Assert.assertEquals(0, SysinfoRiver.getRunningInstances().size());
			}

			// case - successful close
			{
				SysinfoRiver.addRunningInstance(tested);
				Mockito.reset(scMock);
				tested.closed = false;
				tested.indexers.put("ch", indexerMock("ch", SysinfoType.CLUSTER_HEALTH));
				tested.indexers.put("cs", indexerMock("cs", SysinfoType.CLUSTER_STATE));
				List<Thread> t = new ArrayList<Thread>();
				t.add(Mockito.mock(Thread.class));
				t.add(Mockito.mock(Thread.class));
				tested.indexerThreads.put("ch", t.get(0));
				tested.indexerThreads.put("cs", t.get(1));
				tested.close();
				Assert.assertTrue(tested.closed);
				Assert.assertEquals(0, tested.indexerThreads.size());
				Mockito.verify(scMock).close();
				for (SysinfoIndexer i : tested.indexers.values()) {
					Mockito.verify(i).close();
				}
				for (Thread i : t) {
					Mockito.verify(i).interrupt();
				}
				Assert.assertEquals(0, SysinfoRiver.getRunningInstances().size());
			}
		} finally {
			SysinfoRiver.clearRunningInstances();
		}
	}

	@Test
	public void stop() throws Exception {
		SysinfoRiver tested = prepareRiverInstanceForTest(null);
		SourceClient scMock = tested.sourceClient;
		SysinfoRiver.clearRunningInstances();
		SysinfoRiver.addRunningInstance(tested);
		try {
			// case - no errors on empty indexers list and unstarted river
			{
				tested.stop();
				Assert.assertTrue(tested.closed);
				Mockito.verify(scMock).close();
			}

			// case - successful stop
			{
				Mockito.reset(scMock);
				tested.closed = false;
				tested.indexers.put("ch", indexerMock("ch", SysinfoType.CLUSTER_HEALTH));
				tested.indexers.put("cs", indexerMock("cs", SysinfoType.CLUSTER_STATE));
				List<Thread> t = new ArrayList<Thread>();
				t.add(Mockito.mock(Thread.class));
				t.add(Mockito.mock(Thread.class));
				tested.indexerThreads.put("ch", t.get(0));
				tested.indexerThreads.put("cs", t.get(1));

				tested.stop();
				Assert.assertTrue(tested.closed);
				Assert.assertEquals(0, tested.indexerThreads.size());
				Mockito.verify(scMock).close();
				for (SysinfoIndexer i : tested.indexers.values()) {
					Mockito.verify(i).close();
				}
				for (Thread i : t) {
					Mockito.verify(i).interrupt();
				}
				Assert.assertEquals(1, SysinfoRiver.getRunningInstances().size());
			}
		} finally {
			SysinfoRiver.clearRunningInstances();
		}
	}

	@Test
	public void reconfigure() throws Exception {

		// case - exception when not stopped
		{
			SysinfoRiver tested = prepareRiverInstanceForTest(null);
			try {
				tested.closed = false;
				tested.reconfigure();
				Assert.fail("IllegalStateException must be thrown");
			} catch (IllegalStateException e) {
				// OK
			}
		}

		// case - config reload error because no document
		{
			SysinfoRiver tested = prepareRiverInstanceForTest(null);
			try {
				tested.client = prepareESClientForUnitTest();
				tested.closed = true;
				indexCreate(tested.getRiverIndexName());
				tested.reconfigure();
				Assert.fail("IllegalStateException must be thrown");
			} catch (IllegalStateException e) {
				// OK
			} finally {
				finalizeESClientForUnitTest();
			}
		}

		// case - config reload performed
		{
			SysinfoRiver tested = prepareRiverInstanceForTest(null);
			try {
				tested.client = prepareESClientForUnitTest();
				tested.closed = true;
				tested.client.prepareIndex(tested.getRiverIndexName(), tested.riverName().getName(), "_meta")
						.setSource(TestUtils.readStringFromClasspathFile("/river_configuration_test_conn_local.json")).execute()
						.actionGet();

				tested.reconfigure();
				Assert.assertEquals(8, tested.indexers.size());
				Assert.assertTrue(tested.sourceClient instanceof SourceClientESClient);
			} finally {
				finalizeESClientForUnitTest();
			}
		}
	}

	@Test
	public void changeIndexerPeriod() throws Exception {
		SysinfoRiver tested = prepareRiverInstanceForTest(null);

		// case - no indexer to change send, no NPE
		Assert.assertTrue(tested.changeIndexerPeriod(null, 1000));

		// case - no indexers
		Assert.assertFalse(tested.changeIndexerPeriod(new String[] { "myIndexer" }, 1000));

		// case - no indexer found if some exists
		SysinfoIndexer chi = indexerMock("ch", SysinfoType.CLUSTER_HEALTH);
		tested.indexers.put("ch", chi);
		SysinfoIndexer csi = indexerMock("cs", SysinfoType.CLUSTER_HEALTH);
		tested.indexers.put("cs", csi);
		Thread cht = Mockito.mock(Thread.class);
		Thread cst = Mockito.mock(Thread.class);
		tested.indexerThreads.put("ch", cht);
		tested.indexerThreads.put("cs", cst);
		Assert.assertFalse(tested.changeIndexerPeriod(new String[] { "myIndexer" }, 1000));
		Assert.assertFalse(tested.changeIndexerPeriod(new String[] { "myIndexer", "myIndexer2" }, 1000));
		Mockito.verifyZeroInteractions(chi);
		Mockito.verifyZeroInteractions(csi);
		Mockito.verifyZeroInteractions(cht);
		Mockito.verifyZeroInteractions(cst);

		// case - one indexer found from two, increase, no thread restarted
		Mockito.reset(chi, csi, cht, cst);
		Assert.assertTrue(tested.changeIndexerPeriod(new String[] { "myIndexer", "ch" }, 4000));
		Assert.assertEquals(4000, chi.indexingPeriod);
		Mockito.verifyZeroInteractions(chi);
		Mockito.verifyZeroInteractions(csi);
		Mockito.verifyZeroInteractions(cht);
		Mockito.verifyZeroInteractions(cst);

		// case - two indexers found, increase, no threads restarted
		Mockito.reset(chi, csi, cht, cst);
		Assert.assertTrue(tested.changeIndexerPeriod(new String[] { "cs", "ch" }, 5000));
		Assert.assertEquals(5000, chi.indexingPeriod);
		Assert.assertEquals(5000, csi.indexingPeriod);
		Mockito.verifyZeroInteractions(chi);
		Mockito.verifyZeroInteractions(csi);
		Mockito.verifyZeroInteractions(cht);
		Mockito.verifyZeroInteractions(cst);

		// case - one indexer, decrease, thread restarted if old period is long
		Mockito.reset(chi, csi, cht, cst);
		Mockito.when(cht.getState()).thenReturn(State.TERMINATED);
		Assert.assertTrue(tested.changeIndexerPeriod(new String[] { "ch" }, 2000));
		Assert.assertEquals(2000, chi.indexingPeriod);
		Assert.assertNotSame(cht, tested.indexerThreads.get("ch"));
		Mockito.verify(chi).close();
		Mockito.verify(cht).interrupt();
		Mockito.verifyZeroInteractions(csi);
		Mockito.verifyZeroInteractions(cst);
		tested.indexerThreads.get("ch").interrupt();

		// case - one indexer, decrease, thread restarted if old period is long
		Mockito.reset(chi, csi, cht, cst);
		Mockito.when(cht.getState()).thenReturn(State.TERMINATED);
		Assert.assertTrue(tested.changeIndexerPeriod(new String[] { "ch" }, 1000));
		Assert.assertEquals(1000, chi.indexingPeriod);
		Mockito.verifyZeroInteractions(chi);
		Mockito.verifyZeroInteractions(csi);
		Mockito.verifyZeroInteractions(cht);
		Mockito.verifyZeroInteractions(cst);

	}

	/**
	 * Prepare mock indexer of given type
	 * 
	 * @return
	 */
	protected SysinfoIndexer indexerMock(String name, SysinfoType type) {
		SysinfoIndexer i = Mockito.mock(SysinfoIndexer.class);
		i.infoType = type;
		i.name = name;
		return i;
	}

	/**
	 * Prepare {@link JiraRiver} instance for unit test, with Mockito mocked elasticSearchClient.
	 * 
	 * @param toplevelSettings settings to be passed to the river configuring constructor. If null then river is
	 *          constructed without configuration.
	 * @return instance for tests
	 * @throws Exception from constructor
	 */
	public static SysinfoRiver prepareRiverInstanceForTest(Map<String, Object> toplevelSettings) throws Exception {
		Map<String, Object> settings = new HashMap<String, Object>();
		if (toplevelSettings != null)
			settings.putAll(toplevelSettings);

		Settings gs = mock(Settings.class);
		RiverSettings rs = new RiverSettings(gs, settings);
		Client clientMock = mock(Client.class);
		SysinfoRiver ret;
		if (toplevelSettings != null) {
			ret = new SysinfoRiver(new RiverName("sysinfo", RIVER_NAME), rs, clientMock);
		} else {
			ret = new SysinfoRiver(new RiverName("sysinfo", RIVER_NAME), rs);
			ret.client = clientMock;
		}
		ret.sourceClient = Mockito.mock(SourceClient.class);
		return ret;

	}

}

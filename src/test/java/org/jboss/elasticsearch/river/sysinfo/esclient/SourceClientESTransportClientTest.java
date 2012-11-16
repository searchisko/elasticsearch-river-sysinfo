/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link SourceClientESTransportClient}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientESTransportClientTest {

  @Test
  public void constructor() {

    Map<String, Object> sourceClientSettings = new HashMap<String, Object>();
    assertSettingsExceptionThrown(sourceClientSettings,
        "SettingsException must be thrown if adresses setting part is not defined");

    List<Map<String, Object>> adr = new ArrayList<Map<String, Object>>();
    sourceClientSettings.put("addresses", adr);
    assertSettingsExceptionThrown(sourceClientSettings,
        "SettingsException must be thrown if adresses setting part is empty");

    Map<String, Object> adr1 = new HashMap<String, Object>();
    adr.add(adr1);
    assertSettingsExceptionThrown(sourceClientSettings, "SettingsException must be thrown if address is empty");

    adr1.put("host", "host1");
    assertSettingsExceptionThrown(sourceClientSettings, "SettingsException must be thrown if port in address is empty");

    adr1.remove("host");
    adr1.put("port", "9320");
    assertSettingsExceptionThrown(sourceClientSettings, "SettingsException must be thrown if host in address is empty");

    // case - OK, one address, no settings
    {
      adr1.put("host", "host1");
      adr1.put("port", "9320");
      SourceClientESTransportClient tested = new SourceClientESTransportClient(sourceClientSettings);
      Assert.assertEquals(1, tested.transportAddresses.length);
      assertInetSocketTransportAddress(tested.transportAddresses[0], "host1", 9320);
      Assert.assertNotNull(tested.settingsConf);
      Assert.assertTrue(tested.settingsConf.isEmpty());
    }

    // case - OK, more addresses and settings
    {
      Map<String, Object> adr2 = new HashMap<String, Object>();
      adr2.put("host", "host2");
      adr2.put("port", "9321");
      adr.add(adr2);
      Map<String, String> settings = new HashMap<String, String>();
      sourceClientSettings.put("settings", settings);
      settings.put("param1", "value1");
      settings.put("param2", "value2");

      SourceClientESTransportClient tested = new SourceClientESTransportClient(sourceClientSettings);
      Assert.assertEquals(2, tested.transportAddresses.length);
      assertInetSocketTransportAddress(tested.transportAddresses[0], "host1", 9320);
      assertInetSocketTransportAddress(tested.transportAddresses[1], "host2", 9321);
      Assert.assertNotNull(tested.settingsConf);
      Assert.assertEquals("value1", tested.settingsConf.get("param1"));
      Assert.assertEquals("value2", tested.settingsConf.get("param2"));
    }

  }

  private void assertSettingsExceptionThrown(Map<String, Object> sourceClientSettings, String message) {
    try {
      new SourceClientESTransportClient(sourceClientSettings);
      Assert.fail(message);
    } catch (SettingsException e) {
      // OK
    }
  }

  private void assertInetSocketTransportAddress(TransportAddress actual, String expectedHost, int expectedPort) {
    InetSocketAddress adr = ((InetSocketTransportAddress) actual).address();
    Assert.assertEquals(expectedHost, adr.getHostName());
    Assert.assertEquals(expectedPort, adr.getPort());
  }

  @Test
  public void start() {
    SourceClientESTransportClient tested = new SourceClientESTransportClient();

    // case - test not duplicate start
    Client clMock = Mockito.mock(Client.class);
    tested.client = clMock;
    tested.start();
    Assert.assertEquals(clMock, tested.client);

    // case - unit test of real start not performed because needs remote ES cluster which is not unit test!
  }

  @Test
  public void close() {
    SourceClientESTransportClient tested = new SourceClientESTransportClient();

    // case - no error with empty client
    tested.close();
    Assert.assertNull(tested.client);

    // case - client closed correctly
    Client clMock = Mockito.mock(Client.class);
    tested.client = clMock;
    tested.close();
    Mockito.verify(clMock).close();
    Assert.assertNull(tested.client);

  }

}

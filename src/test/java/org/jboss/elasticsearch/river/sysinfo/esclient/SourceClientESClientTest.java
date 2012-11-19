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
  public synchronized void readStateInfo() throws Exception {
    try {
      Client client = prepareESClientForUnitTest();

      SourceClientESClient tested = new SourceClientESClient(client);

      String info = tested.readClusterStateInfo(null);
      assertStartsWith("{\"cluster_name\":\"elasticsearch\",\"master_node\":\"1\",\"blocks\":", info);

    } finally {
      finalizeESClientForUnitTest();
    }
  }

  @Test
  public synchronized void readHealthInfo() throws Exception {
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

      Map<String, String> params = new HashMap<String, String>();
      params.put("index", "test");
      String info = tested.readIndicesStatusInfo(params);
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

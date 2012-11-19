/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

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
  public void readStateInfo() throws Exception {
    try {
      Client client = prepareESClientForUnitTest();

      SourceClientESClient tested = new SourceClientESClient(client);

      String info = tested.readClusterStateInfo(null);
      Assert.assertTrue(info.contains("{\"cluster_name\":\"elasticsearch\","));

    } finally {
      finalizeESClientForUnitTest();
    }
  }

  @Test
  public void readHealthInfo() throws Exception {
    try {
      Client client = prepareESClientForUnitTest();

      SourceClientESClient tested = new SourceClientESClient(client);

      String info = tested.readClusterHealthInfo(null);
      Assert.assertTrue(info.contains("{\"cluster_name\":\"elasticsearch\","));

    } finally {
      finalizeESClientForUnitTest();
    }
  }

  @Test
  public void readClusterNodesInfoInfo() throws Exception {
    try {
      Client client = prepareESClientForUnitTest();

      SourceClientESClient tested = new SourceClientESClient(client);

      String info = tested.readClusterHealthInfo(null);
      Assert.assertTrue(info.contains("{\"cluster_name\":\"elasticsearch\","));

    } finally {
      finalizeESClientForUnitTest();
    }
  }
}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.local;

import junit.framework.Assert;

import org.elasticsearch.client.Client;
import org.jboss.elasticsearch.river.sysinfo.testtools.ESRealClientTestBase;
import org.junit.Test;

/**
 * Unit test for {@link SourceClientLocal}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientLocalTest extends ESRealClientTestBase {

  @Test
  public void readStateInfo() throws Exception {
    try {
      Client client = prepareESClientForUnitTest();

      SourceClientLocal tested = new SourceClientLocal(client);

      String info = tested.readStateInfo();
      Assert.assertTrue(info.contains("{\"cluster_name\":\"elasticsearch\","));
      System.out.println(info);

    } finally {
      finalizeESClientForUnitTest();
    }
  }

  @Test
  public void readHealthInfo() throws Exception {
    try {
      Client client = prepareESClientForUnitTest();

      SourceClientLocal tested = new SourceClientLocal(client);

      String info = tested.readHealthInfo();
      Assert.assertTrue(info.contains("{\"cluster_name\":\"elasticsearch\","));
      System.out.println(info);

    } finally {
      finalizeESClientForUnitTest();
    }
  }
}

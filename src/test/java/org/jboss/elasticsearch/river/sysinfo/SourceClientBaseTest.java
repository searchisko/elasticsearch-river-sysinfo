/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit test for {@link SourceClientBase}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientBaseTest {

  @Test
  public void readSysinfoValue() throws IOException, InterruptedException {

    Map<String, String> params = new HashMap<String, String>();

    Tested tested = new Tested();
    Assert.assertEquals("SI", tested.readSysinfoValue(SysinfoType.CLUSTER_STATE, params));
    Assert.assertEquals("HI_noparams", tested.readSysinfoValue(SysinfoType.CLUSTER_HEALTH, null));

    try {
      tested.readSysinfoValue(null, null);
      Assert.fail("IllegalArgumentException must be thrown");
    } catch (IllegalArgumentException e) {
      // OK
    }

  }

  private class Tested extends SourceClientBase {

    @Override
    protected String readClusterStateInfo(Map<String, String> params) throws IOException {
      return ret("SI", params);
    }

    @Override
    protected String readClusterHealthInfo(Map<String, String> params) throws IOException {
      return ret("HI", params);
    }

    private String ret(String name, Map<String, String> params) {
      if (params == null) {
        name = name + "_noparams";
      }
      return name;
    }

    @Override
    public void start() {
    }

    @Override
    public void close() {
    }

  }

}

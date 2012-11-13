/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;

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

    Tested tested = new Tested();
    Assert.assertEquals("SI", tested.readSysinfoValue(SysinfoType.STATE));
    Assert.assertEquals("HI", tested.readSysinfoValue(SysinfoType.HEALTH));
  }

  private class Tested extends SourceClientBase {

    @Override
    protected String readStateInfo() throws IOException {
      return "SI";
    }

    @Override
    protected String readHealthInfo() throws IOException {
      return "HI";
    }

  }

}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.local;

import java.io.IOException;

import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.StringRestResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link LocalRestChannel}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class LocalRestChannelTest {

  @Test
  public void response_ok() throws IOException, InterruptedException {

    LocalRestChannel tested = new LocalRestChannel();

    tested.sendResponse(new StringRestResponse(RestStatus.OK, "result"));

    Assert.assertEquals("result", tested.getResponseContent());

  }

  @Test
  public void response_error() throws IOException, InterruptedException {

    LocalRestChannel tested = new LocalRestChannel();

    tested.sendResponse(new StringRestResponse(RestStatus.EXPECTATION_FAILED));

    try {
      tested.getResponseContent();
      Assert.fail("IOException must be thrown");
    } catch (IOException e) {
      // OK
    }
  }

  @Test
  public void response_timeout() throws IOException, InterruptedException {

    LocalRestChannel tested = new LocalRestChannel();
    tested.TIMEOUT = 50;

    try {
      tested.getResponseContent();
      Assert.fail("IOException must be thrown");
    } catch (IOException e) {
      // OK
    }
  }

}

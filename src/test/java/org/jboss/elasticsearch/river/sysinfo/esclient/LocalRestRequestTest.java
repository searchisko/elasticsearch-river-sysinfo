/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

import java.util.HashMap;
import java.util.Map;

import org.jboss.elasticsearch.river.sysinfo.esclient.LocalRestRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link LocalRestRequest}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class LocalRestRequestTest {

  @Test
  public void params() {
    LocalRestRequest tested = new LocalRestRequest(null);

    // case - initial params structure for null in constructor
    Assert.assertNotNull(tested.params());
    Assert.assertFalse(tested.hasParam("testparam"));
    Assert.assertNull(tested.param("testparam"));
    Assert.assertEquals("aa", tested.param("testparam", "aa"));

    // case - add param test
    tested.addParam("testparam", "bbb").addParam("param2", "ccc");
    Assert.assertEquals(2, tested.params().size());
    Assert.assertTrue(tested.hasParam("testparam"));
    Assert.assertTrue(tested.hasParam("param2"));
    Assert.assertEquals("bbb", tested.param("testparam"));
    Assert.assertEquals("ccc", tested.param("param2"));
    Assert.assertEquals("bbb", tested.param("testparam", "aa"));

    // case - filled by constructor
    Map<String, String> params = new HashMap<String, String>();
    params.put("tp", "a");
    tested = new LocalRestRequest(params);
    Assert.assertEquals("a", tested.param("tp"));
    Assert.assertEquals(1, tested.params().size());
  }

}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.local;

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
    LocalRestRequest tested = new LocalRestRequest();

    Assert.assertFalse(tested.hasParam("testparam"));
    Assert.assertNull(tested.param("testparam"));
    Assert.assertNotNull(tested.params());
    Assert.assertEquals("aa", tested.param("testparam", "aa"));

    tested.addParam("testparam", "bbb").addParam("param2", "ccc");
    Assert.assertTrue(tested.hasParam("testparam"));
    Assert.assertTrue(tested.hasParam("param2"));
    Assert.assertEquals("bbb", tested.param("testparam"));
    Assert.assertEquals("ccc", tested.param("param2"));
    Assert.assertEquals("bbb", tested.param("testparam", "aa"));
    Assert.assertEquals(2, tested.params().size());
  }

}

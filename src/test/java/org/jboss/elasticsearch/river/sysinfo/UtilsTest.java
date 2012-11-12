/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.ElasticSearchParseException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Utils}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class UtilsTest {

  @Test
  public void isEmpty() {
    Assert.assertTrue(Utils.isEmpty(null));
    Assert.assertTrue(Utils.isEmpty(""));
    Assert.assertTrue(Utils.isEmpty("     "));
    Assert.assertTrue(Utils.isEmpty(" "));
    Assert.assertFalse(Utils.isEmpty("a"));
    Assert.assertFalse(Utils.isEmpty(" a"));
    Assert.assertFalse(Utils.isEmpty("a "));
    Assert.assertFalse(Utils.isEmpty(" a "));
  }

  @Test
  public void trimToNull() {
    Assert.assertNull(Utils.trimToNull(null));
    Assert.assertNull(Utils.trimToNull(""));
    Assert.assertNull(Utils.trimToNull("     "));
    Assert.assertNull(Utils.trimToNull(" "));
    Assert.assertEquals("a", Utils.trimToNull("a"));
    Assert.assertEquals("a", Utils.trimToNull(" a"));
    Assert.assertEquals("a", Utils.trimToNull("a "));
    Assert.assertEquals("a", Utils.trimToNull(" a "));
  }

  @Test
  public void parseTimeValue() {
    Map<String, Object> jiraSettings = new HashMap<String, Object>();

    // test defaults
    Assert.assertEquals(0, Utils.parseTimeValue(jiraSettings, "nonexist", 1250, null));
    Assert.assertEquals(12, Utils.parseTimeValue(null, "nonexist", 12, TimeUnit.MILLISECONDS));
    Assert.assertEquals(1250, Utils.parseTimeValue(jiraSettings, "nonexist", 1250, TimeUnit.MILLISECONDS));

    // test correct values parsing
    jiraSettings.put("mstest", "250");
    jiraSettings.put("mstest2", "255ms");
    jiraSettings.put("secondtest", "250s");
    jiraSettings.put("minutetest", "50m");
    jiraSettings.put("hourtest", "2h");
    jiraSettings.put("daytest", "2d");
    jiraSettings.put("weektest", "2w");
    jiraSettings.put("zerotest", "0");
    jiraSettings.put("negativetest", "-1");
    Assert.assertEquals(250, Utils.parseTimeValue(jiraSettings, "mstest", 1250, TimeUnit.MILLISECONDS));
    Assert.assertEquals(255, Utils.parseTimeValue(jiraSettings, "mstest2", 1250, TimeUnit.MILLISECONDS));
    Assert.assertEquals(250 * 1000, Utils.parseTimeValue(jiraSettings, "secondtest", 1250, TimeUnit.MILLISECONDS));
    Assert.assertEquals(50 * 60 * 1000, Utils.parseTimeValue(jiraSettings, "minutetest", 1250, TimeUnit.MILLISECONDS));
    Assert.assertEquals(2 * 24 * 60 * 60 * 1000,
        Utils.parseTimeValue(jiraSettings, "daytest", 1250, TimeUnit.MILLISECONDS));
    Assert.assertEquals(2 * 7 * 24 * 60 * 60 * 1000,
        Utils.parseTimeValue(jiraSettings, "weektest", 1250, TimeUnit.MILLISECONDS));
    Assert.assertEquals(0, Utils.parseTimeValue(jiraSettings, "zerotest", 1250, TimeUnit.MILLISECONDS));
    Assert.assertEquals(-1, Utils.parseTimeValue(jiraSettings, "negativetest", 1250, TimeUnit.MILLISECONDS));

    // test error handling
    jiraSettings.put("errortest", "w");
    jiraSettings.put("errortest2", "ahojs");
    try {
      Utils.parseTimeValue(jiraSettings, "errortest", 1250, TimeUnit.MILLISECONDS);
      Assert.fail("ElasticSearchParseException must be thrown");
    } catch (ElasticSearchParseException e) {
      // ok
    }
    try {
      Utils.parseTimeValue(jiraSettings, "errortest2", 1250, TimeUnit.MILLISECONDS);
      Assert.fail("ElasticSearchParseException must be thrown");
    } catch (ElasticSearchParseException e) {
      // ok
    }
  }

  @Test
  public void nodeIntegerValue() {
    Assert.assertNull(Utils.nodeIntegerValue(null));
    Assert.assertEquals(new Integer(10), Utils.nodeIntegerValue(new Integer(10)));
    Assert.assertEquals(new Integer(10), Utils.nodeIntegerValue(new Short("10")));
    Assert.assertEquals(new Integer(10), Utils.nodeIntegerValue(new Long("10")));
    Assert.assertEquals(new Integer(10), Utils.nodeIntegerValue("10"));
    try {
      Utils.nodeIntegerValue("ahoj");
      Assert.fail("No NumberFormatException thrown.");
    } catch (NumberFormatException e) {
      // OK
    }
  }

}

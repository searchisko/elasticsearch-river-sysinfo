/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import org.elasticsearch.common.settings.SettingsException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link SysinfoType}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoTypeTest {

  @Test
  public void parseConfiguration() {

    for (SysinfoType t : SysinfoType.values()) {
      Assert.assertEquals(t, SysinfoType.parseConfiguration(t.getName()));
    }

    try {
      SysinfoType.parseConfiguration("nonsense");
      Assert.fail("SettingsException must be thrown");
    } catch (SettingsException e) {
      // OK
    }
    try {
      SysinfoType.parseConfiguration(null);
      Assert.fail("SettingsException must be thrown");
    } catch (SettingsException e) {
      // OK
    }
    try {
      SysinfoType.parseConfiguration("");
      Assert.fail("SettingsException must be thrown");
    } catch (SettingsException e) {
      // OK
    }

  }

}

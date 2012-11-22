/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import java.io.IOException;

import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link JRPeriodRequest}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodRequestTest {

  @Test
  public void constructor_empty() {
    {
      JRPeriodRequest tested = new JRPeriodRequest();

      tested.setRiverName("myriver");
      tested.setIndexerNames(new String[] { "idx1", "idx2" });
      tested.setPeriod(1300);
      Assert.assertEquals("myriver", tested.getRiverName());
      Assert.assertArrayEquals(new String[] { "idx1", "idx2" }, tested.getIndexerNames());
      Assert.assertEquals(1300, tested.getPeriod());
    }
  }

  @Test
  public void constructor_filling() {

    try {
      new JRPeriodRequest(null, new String[] { "idx1", "idx2" }, 1360);
      Assert.fail("IllegalArgumentException must be thrown");
    } catch (IllegalArgumentException e) {
      // OK
    }

    {
      JRPeriodRequest tested = new JRPeriodRequest("myriver", new String[] { "idx1", "idx2" }, 1450);
      Assert.assertEquals("myriver", tested.getRiverName());
      Assert.assertArrayEquals(new String[] { "idx1", "idx2" }, tested.getIndexerNames());
      Assert.assertEquals(1450, tested.getPeriod());
    }
  }

  @Test
  public void serialization() throws IOException {

    {
      JRPeriodRequest testedSrc = new JRPeriodRequest("myriver", new String[] { "idx1", "idx2" }, 1450);
      JRPeriodRequest testedTarget = performserialization(testedSrc);
      Assert.assertEquals("myriver", testedTarget.getRiverName());
      Assert.assertArrayEquals(new String[] { "idx1", "idx2" }, testedTarget.getIndexerNames());
      Assert.assertEquals(1450, testedTarget.getPeriod());
    }

    {
      JRPeriodRequest testedSrc = new JRPeriodRequest("myriver2", null, 1230);
      JRPeriodRequest testedTarget = performserialization(testedSrc);
      Assert.assertEquals("myriver2", testedTarget.getRiverName());
      Assert.assertNull(testedTarget.getIndexerNames());
      Assert.assertEquals(1230, testedTarget.getPeriod());
    }

  }

  /**
   * @param testedSrc
   * @return
   * @throws IOException
   */
  private JRPeriodRequest performserialization(JRPeriodRequest testedSrc) throws IOException {
    BytesStreamOutput out = new BytesStreamOutput();
    testedSrc.writeTo(out);
    JRPeriodRequest testedTarget = new JRPeriodRequest();
    testedTarget.readFrom(new BytesStreamInput(out.bytes()));
    return testedTarget;
  }

}

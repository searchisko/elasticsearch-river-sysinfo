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
 * Unit test for {@link NodeJRPeriodRequest}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class NodeJRPeriodRequestTest {

  @Test
  public void constructor() {

    // note nodeId cann't be asserted because private and no getter for it :-(

    {
      NodeJRPeriodRequest tested = new NodeJRPeriodRequest();
      Assert.assertNull(tested.getRequest());
    }

    {
      JRPeriodRequest request = new JRPeriodRequest();
      NodeJRPeriodRequest tested = new NodeJRPeriodRequest("myNode", request);
      Assert.assertEquals(request, tested.getRequest());
    }

  }

  @SuppressWarnings("unused")
  @Test
  public void serialization() throws IOException {

    {
      JRPeriodRequest request = new JRPeriodRequest("my river", null, 1500);
      NodeJRPeriodRequest testedSrc = new NodeJRPeriodRequest("myNode", request);
      NodeJRPeriodRequest testedTarget = performSerializationAndBasicAsserts(testedSrc);
    }
    {
      JRPeriodRequest request = new JRPeriodRequest("my river 2", new String[] { "aaa" }, 5602);
      NodeJRPeriodRequest testedSrc = new NodeJRPeriodRequest("myNode2", request);
      NodeJRPeriodRequest testedTarget = performSerializationAndBasicAsserts(testedSrc);
    }

  }

  private NodeJRPeriodRequest performSerializationAndBasicAsserts(NodeJRPeriodRequest testedSrc) throws IOException {
    BytesStreamOutput out = new BytesStreamOutput();
    testedSrc.writeTo(out);
    NodeJRPeriodRequest testedTarget = new NodeJRPeriodRequest();
    testedTarget.readFrom(new BytesStreamInput(out.bytes()));
    Assert.assertEquals(testedSrc.getRequest().getRiverName(), testedTarget.getRequest().getRiverName());
    Assert.assertArrayEquals(testedSrc.getRequest().getIndexerNames(), testedTarget.getRequest().getIndexerNames());
    Assert.assertEquals(testedSrc.getRequest().getPeriod(), testedTarget.getRequest().getPeriod());
    return testedTarget;
  }

}

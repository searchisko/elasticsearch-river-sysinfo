/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import java.io.IOException;

import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.transport.DummyTransportAddress;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link NodeJRPeriodResponse}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class NodeJRPeriodResponseTest {

  DiscoveryNode dn = new DiscoveryNode("aa", DummyTransportAddress.INSTANCE);

  @Test
  public void constructor() {
    {
      NodeJRPeriodResponse tested = new NodeJRPeriodResponse();
      Assert.assertNull(tested.getNode());
      Assert.assertFalse(tested.isRiverFound());
    }

    {
      NodeJRPeriodResponse tested = new NodeJRPeriodResponse(dn);
      Assert.assertEquals(dn, tested.getNode());
      Assert.assertFalse(tested.isRiverFound());
      Assert.assertFalse(tested.isIndexerFound());
    }

    {
      NodeJRPeriodResponse tested = new NodeJRPeriodResponse(dn, false, true);
      Assert.assertEquals(dn, tested.getNode());
      Assert.assertFalse(tested.isRiverFound());
      Assert.assertTrue(tested.isIndexerFound());
    }
    {
      NodeJRPeriodResponse tested = new NodeJRPeriodResponse(dn, true, false);
      Assert.assertEquals(dn, tested.getNode());
      Assert.assertTrue(tested.isRiverFound());
      Assert.assertFalse(tested.indexerFound);
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void serialization() throws IOException {

    {
      NodeJRPeriodResponse testedSrc = new NodeJRPeriodResponse(dn, false, true);
      NodeJRPeriodResponse testedTarget = performSerializationAndBasicAsserts(testedSrc);
    }
    {
      NodeJRPeriodResponse testedSrc = new NodeJRPeriodResponse(dn, true, false);
      NodeJRPeriodResponse testedTarget = performSerializationAndBasicAsserts(testedSrc);
    }

  }

  private NodeJRPeriodResponse performSerializationAndBasicAsserts(NodeJRPeriodResponse testedSrc) throws IOException {
    BytesStreamOutput out = new BytesStreamOutput();
    testedSrc.writeTo(out);
    NodeJRPeriodResponse testedTarget = new NodeJRPeriodResponse();
    testedTarget.readFrom(new BytesStreamInput(out.bytes()));
    Assert.assertEquals(testedSrc.getNode().getId(), testedTarget.getNode().getId());
    Assert.assertEquals(testedSrc.isRiverFound(), testedTarget.isRiverFound());
    Assert.assertEquals(testedSrc.isIndexerFound(), testedTarget.isIndexerFound());

    return testedTarget;
  }

}

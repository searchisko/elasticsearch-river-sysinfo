/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import java.io.IOException;

import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.jboss.elasticsearch.river.sysinfo.mgm.NodeJRMgmBaseResponse;

/**
 * SysinfoRiver period change command node response.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class NodeJRPeriodResponse extends NodeJRMgmBaseResponse {

  protected boolean indexerFound;

  protected NodeJRPeriodResponse() {
  }

  /**
   * Create response with values to be send back to requestor. <code>riverFound</code> is set to <code>false</code>.
   * 
   * @param node this response is for.
   */
  public NodeJRPeriodResponse(DiscoveryNode node) {
    super(node);
  }

  /**
   * Create response with values to be send back to requestor.
   * 
   * @param node this response is for.
   * @param riverFound set to true if you found river on this node
   * @param indexerFound set to true if at least one indexer was found and period changed for it
   */
  public NodeJRPeriodResponse(DiscoveryNode node, boolean riverFound, boolean indexerFound) {
    super(node, riverFound);
    this.indexerFound = indexerFound;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    super.readFrom(in);
    indexerFound = in.readBoolean();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    super.writeTo(out);
    out.writeBoolean(indexerFound);
  }

  public boolean isIndexerFound() {
    return indexerFound;
  }

}

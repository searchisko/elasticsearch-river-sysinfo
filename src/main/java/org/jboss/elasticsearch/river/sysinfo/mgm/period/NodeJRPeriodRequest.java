/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.jboss.elasticsearch.river.sysinfo.mgm.NodeJRMgmBaseRequest;

/**
 * Node request for SysinfoRiver period change command.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class NodeJRPeriodRequest extends NodeJRMgmBaseRequest<JRPeriodRequest> {

  NodeJRPeriodRequest() {
    super();
  }

  /**
   * Construct node request with data.
   * 
   * @param nodeId this request is for
   * @param request to be send to the node
   */
  public NodeJRPeriodRequest(String nodeId, JRPeriodRequest request) {
    super(nodeId, request);
  }

  @Override
  protected JRPeriodRequest newRequest() {
    return new JRPeriodRequest();
  }

}

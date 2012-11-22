/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.cluster.ClusterName;
import org.jboss.elasticsearch.river.sysinfo.mgm.JRMgmBaseResponse;

/**
 * Response SysinfoRiver period chage command. All node responses are aggregated here.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodResponse extends JRMgmBaseResponse<NodeJRPeriodResponse> {

  public JRPeriodResponse() {

  }

  public JRPeriodResponse(ClusterName clusterName, NodeJRPeriodResponse[] nodes) {
    super(clusterName, nodes);
  }

  @Override
  protected NodeJRPeriodResponse[] newNodeResponsesArray(int len) {
    return new NodeJRPeriodResponse[len];
  }

  @Override
  protected NodeJRPeriodResponse newNodeResponse() {
    return new NodeJRPeriodResponse();
  }

}

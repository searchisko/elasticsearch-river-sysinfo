/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.BaseRequestBuilder;
import org.elasticsearch.client.Client;

/**
 * Request builder to perform period change method of some sysinfo river.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodRequestBuilder extends BaseRequestBuilder<JRPeriodRequest, JRPeriodResponse> {

  public JRPeriodRequestBuilder(Client client) {
    super(client, new JRPeriodRequest());
  }

  /**
   * Set name of river to perform operation on
   * 
   * @param riverName name of river
   * @return builder for chaining
   */
  public JRPeriodRequestBuilder setRiverName(String riverName) {
    this.request.setRiverName(riverName);
    return this;
  }

  /**
   * Set names of indexers to change period for.
   * 
   * @param indexerNames to change period for.
   * @return builder for chaining
   */
  public JRPeriodRequestBuilder setIndexerNames(String[] indexerNames) {
    this.request.setIndexerNames(indexerNames);
    return this;
  }

  /**
   * Set new period.
   * 
   * @param period to set
   * @return builder for chaining
   */
  public JRPeriodRequestBuilder setPeriod(long period) {
    this.request.setPeriod(period);
    return this;
  }

  @Override
  protected void doExecute(ActionListener<JRPeriodResponse> listener) {
    if (request.getRiverName() == null)
      throw new IllegalArgumentException("riverName must be provided for request");
    client.execute(JRPeriodAction.INSTANCE, request, listener);
  }

  protected Client getClient() {
    return client;
  }

}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;

/**
 * Sysinfo River lifecycle action implementation.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodAction extends Action<JRPeriodRequest, JRPeriodResponse, JRPeriodRequestBuilder> {

  public static final JRPeriodAction INSTANCE = new JRPeriodAction();
  public static final String NAME = "sysinfo_river/period";

  protected JRPeriodAction() {
    super(NAME);
  }

  @Override
  public JRPeriodRequestBuilder newRequestBuilder(Client client) {
    return new JRPeriodRequestBuilder(client);
  }

  @Override
  public JRPeriodResponse newResponse() {
    return new JRPeriodResponse();
  }

}

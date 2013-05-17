/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.client.ClusterAdminClient;

/**
 * Sysinfo River lifecycle action implementation.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodAction extends ClusterAction<JRPeriodRequest, JRPeriodResponse, JRPeriodRequestBuilder> {

	public static final JRPeriodAction INSTANCE = new JRPeriodAction();
	public static final String NAME = "sysinfo_river/period";

	protected JRPeriodAction() {
		super(NAME);
	}

	@Override
	public JRPeriodRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new JRPeriodRequestBuilder(client);
	}

	@Override
	public JRPeriodResponse newResponse() {
		return new JRPeriodResponse();
	}

}

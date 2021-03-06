/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.client.ClusterAdminClient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link JRPeriodAction}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodActionTest {

	@Test
	public void constructor() {
		Assert.assertEquals(JRPeriodAction.NAME, JRPeriodAction.INSTANCE.name());
	}

	@Test
	public void newRequestBuilder() {
		ClusterAdminClient client = Mockito.mock(ClusterAdminClient.class);

		JRPeriodRequestBuilder rb = JRPeriodAction.INSTANCE.newRequestBuilder(client);
		Assert.assertNotNull(rb);
	}

	@Test
	public void newResponse() {
		JRPeriodResponse rb = JRPeriodAction.INSTANCE.newResponse();
		Assert.assertNotNull(rb);
	}
}

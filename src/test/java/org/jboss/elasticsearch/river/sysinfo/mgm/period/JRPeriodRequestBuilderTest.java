/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.internal.InternalClusterAdminClient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link JRPeriodRequestBuilder}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodRequestBuilderTest {

	@Test
	public void test() {

		InternalClusterAdminClient client = Mockito.mock(InternalClusterAdminClient.class);

		{
			JRPeriodRequestBuilder tested = new JRPeriodRequestBuilder(client);
			Assert.assertNull(tested.request().getRiverName());
			Assert.assertNull(tested.request().getIndexerNames());

			try {
				tested.doExecute(null);
				Assert.fail("IllegalArgumentException must be thrown");
			} catch (IllegalArgumentException e) {
				// OK
			}
		}

		{
			JRPeriodRequestBuilder tested = new JRPeriodRequestBuilder(client);
			Assert.assertEquals(tested, tested.setIndexerNames(new String[] { "idx1, idx2" }));
			Assert.assertEquals(tested, tested.setPeriod(1200));
			Assert.assertNull(tested.request().getRiverName());
			Assert.assertArrayEquals(new String[] { "idx1, idx2" }, tested.request().getIndexerNames());
			Assert.assertEquals(1200, tested.request().getPeriod());
			try {
				tested.doExecute(null);
				Assert.fail("IllegalArgumentException must be thrown");
			} catch (IllegalArgumentException e) {
				// OK
			}

			Assert.assertEquals(tested, tested.setRiverName("my river"));
			Assert.assertEquals("my river", tested.request().getRiverName());
			Assert.assertArrayEquals(new String[] { "idx1, idx2" }, tested.request().getIndexerNames());
			Assert.assertEquals(1200, tested.request().getPeriod());
			ActionListener<JRPeriodResponse> al = new ActionListener<JRPeriodResponse>() {

				@Override
				public void onResponse(JRPeriodResponse response) {
				}

				@Override
				public void onFailure(Throwable e) {
				}
			};
			tested.doExecute(al);
			Mockito.verify(client).execute(JRPeriodAction.INSTANCE, tested.request(), al);

		}
	}

}

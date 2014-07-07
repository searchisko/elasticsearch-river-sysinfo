/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import java.util.concurrent.TimeUnit;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.jboss.elasticsearch.river.sysinfo.Utils;
import org.jboss.elasticsearch.river.sysinfo.mgm.JRMgmBaseActionListener;
import org.jboss.elasticsearch.river.sysinfo.mgm.RestJRMgmBaseAction;

import static org.elasticsearch.rest.RestStatus.OK;

/**
 * REST action handler for Sysinfo river change period operation.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class RestJRPeriodAction extends RestJRMgmBaseAction {

	@Inject
	protected RestJRPeriodAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		String baseUrl = baseRestMgmUrl();
		controller.registerHandler(org.elasticsearch.rest.RestRequest.Method.POST, baseUrl
				+ "{indexerName}/period/{period}", this);
	}

	@Override
	public void handleRequest(final RestRequest restRequest, final RestChannel restChannel) {

		final String indexerNames = restRequest.param("indexerName");
		final long period = Utils.parseTimeValue(restRequest.params(), "period", 1, TimeUnit.MINUTES);
		JRPeriodRequest actionRequest = new JRPeriodRequest(restRequest.param("riverName"),
				splitIndexerNames(indexerNames), period);

		client
				.admin()
				.cluster()
				.execute(
						JRPeriodAction.INSTANCE,
						actionRequest,
						new JRMgmBaseActionListener<JRPeriodRequest, JRPeriodResponse, NodeJRPeriodResponse>(actionRequest,
								restRequest, restChannel) {

							@Override
							protected void handleRiverResponse(NodeJRPeriodResponse nodeInfo) throws Exception {
								if (nodeInfo.indexerFound) {
									restChannel.sendResponse(new BytesRestResponse(OK, buildMessageDocument(restRequest,
											"Period changed to " + period + "[ms] for at least one of defined indexers")));
								} else {
									restChannel.sendResponse(new BytesRestResponse(RestStatus.NOT_FOUND, buildMessageDocument(
											restRequest, "No any of defined indexers '" + indexerNames + "' found")));
								}
							}

						});
	}

	public static String[] splitIndexerNames(String namesParam) {
		if (Utils.isEmpty(namesParam)) {
			return Strings.EMPTY_ARRAY;
		}
		return Strings.splitStringByCommaToArray(namesParam);
	}
}

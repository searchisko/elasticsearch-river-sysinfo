/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.http.HttpException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link SourceClientREST}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientRESTTest {

	@Test
	public void constructor_configure() {

		// case - undefined urlBase
		try {
			Map<String, Object> settings = new HashMap<String, Object>();
			new SourceClientREST(settings);
			Assert.fail("SettingsException expected");
		} catch (SettingsException e) {
			// OK
		}

		try {
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("urlBase", "");
			new SourceClientREST(settings);
			Assert.fail("SettingsException expected");
		} catch (SettingsException e) {
			// OK
		}

		// case - malformed urlBase
		try {
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("urlBase", "ahoj");
			new SourceClientREST(settings);
			Assert.fail("SettingsException expected");
		} catch (SettingsException e) {
			// OK
		}

		// case - url ok, defaults for rest
		{
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("urlBase", "http://test.org");
			SourceClientREST tested = new SourceClientREST(settings);
			Assert.assertEquals("http://test.org/", tested.restAPIUrlBase);
			Assert.assertFalse(tested.isAuthConfigured);
		}

		// case - url ok, all configured
		{
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("urlBase", "http://test.org");
			settings.put("timeout", "8s");
			settings.put("username", "usr");
			settings.put("pwd", "pw");
			SourceClientREST tested = new SourceClientREST(settings);
			Assert.assertEquals("http://test.org/", tested.restAPIUrlBase);
			Assert.assertTrue(tested.isAuthConfigured);
		}

		// case - username and password settings
		{
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("urlBase", "http://test.org");
			settings.put("timeout", "8s");
			settings.put("username", "");
			settings.put("pwd", "pw");
			SourceClientREST tested = new SourceClientREST(settings);
			Assert.assertEquals("http://test.org/", tested.restAPIUrlBase);
			Assert.assertFalse(tested.isAuthConfigured);
		}
		{
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("urlBase", "http://test.org");
			settings.put("timeout", "8s");
			settings.put("username", "uname");
			SourceClientREST tested = new SourceClientREST(settings);
			Assert.assertEquals("http://test.org/", tested.restAPIUrlBase);
			Assert.assertFalse(tested.isAuthConfigured);
		}
		{
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("urlBase", "http://test.org");
			settings.put("timeout", "8s");
			settings.put("username", "uname");
			settings.put("pwd", "");
			SourceClientREST tested = new SourceClientREST(settings);
			Assert.assertEquals("http://test.org/", tested.restAPIUrlBase);
			Assert.assertTrue(tested.isAuthConfigured);
		}

	}

	@Test
	public void performRESTCall() throws IOException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		// case - ok response, no aut nor parameters
		{
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/testop", false));
			Assert.assertEquals(null, tested.performRESTCall("testop", null));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		// case - ok response, both auth and parameters
		{
			Mockito.reset(hcMock);
			tested.isAuthConfigured = true;
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/testop?param=pval", true));
			NameValuePair[] params = new NameValuePair[] { new BasicNameValuePair("param", "pval") };
			Assert.assertEquals(null, tested.performRESTCall("testop", params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		// case - http error response
		{
			Mockito.reset(hcMock);
			tested.isAuthConfigured = false;
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(new Answer<Integer>() {

						@Override
						public Integer answer(InvocationOnMock invocation) throws Throwable {
							HttpGet method = (HttpGet) invocation.getArguments()[0];
							Assert.assertEquals("http://test.org/testop?param=pval&param2=pval2", method.getURI().toString());
							return HttpStatus.SC_BAD_GATEWAY;
						}
					});

			try {
				NameValuePair[] params = new NameValuePair[] { new BasicNameValuePair("param", "pval"),
						new BasicNameValuePair("param2", "pval2") };
				Assert.assertEquals(null, tested.performRESTCall("testop", params));
				Assert.fail("IOException must be thrown");
			} catch (IOException e) {
				Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
						Mockito.any(HttpContext.class));
			}
		}

		// case - exception from http client call
		{
			Mockito.reset(hcMock);
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenThrow(new HttpException("test exception"));
			try {
				NameValuePair[] params = new NameValuePair[] { new BasicNameValuePair("param", "pval") };
				Assert.assertEquals(null, tested.performRESTCall("testop", params));
				Assert.fail("HttpException must be thrown");
			} catch (IOException e) {
				Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
						Mockito.any(HttpContext.class));
			}
		}
	}

	@Test
	public void readClusterStateInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_cluster/state?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterStateInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readClusterStatsInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
					prepareOKAnswerWithAssertions("http://test.org/_cluster/stats?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterStatsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("nodeId", "node1,node2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
					prepareOKAnswerWithAssertions("http://test.org/_cluster/stats/nodes/node1,node2?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterStatsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readClusterHealthInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_cluster/health?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterHealthInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("index", "idx1,idx2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_cluster/health/idx1,idx2?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterHealthInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readClusterNodesInfoInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_nodes?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterNodesInfoInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("nodeId", "idx1,idx2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_nodes/idx1,idx2?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterNodesInfoInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readClusterNodesStatsInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_nodes/stats?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterNodesStatsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("nodeId", "idx1,idx2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_nodes/idx1,idx2/stats?param=myparam", false));
			Assert.assertEquals(null, tested.readClusterNodesStatsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readIndicesStatusInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_status?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesStatusInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("index", "idx1,idx2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/idx1,idx2/_status?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesStatusInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readIndicesStatsInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_stats?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesStatsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("index", "idx1,idx2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/idx1,idx2/_stats?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesStatsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readIndicesSegmentsInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/_segments?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesSegmentsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("index", "idx1,idx2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
							prepareOKAnswerWithAssertions("http://test.org/idx1,idx2/_segments?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesSegmentsInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	@Test
	public void readIndicesRecoveryInfo() throws IOException, InterruptedException {
		SourceClientREST tested = prepareTestedInstance();
		HttpClient hcMock = tested.httpclient;

		{
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
					prepareOKAnswerWithAssertions("http://test.org/_recovery?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesRecoveryInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}

		{
			Mockito.reset(hcMock);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("param", "myparam");
			params.put("index", "idx1,idx2");
			Mockito
					.when(
							hcMock.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
									Mockito.any(HttpContext.class))).thenAnswer(
					prepareOKAnswerWithAssertions("http://test.org/idx1,idx2/_recovery?param=myparam", false));
			Assert.assertEquals(null, tested.readIndicesRecoveryInfo(params));
			Mockito.verify(hcMock).execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
					Mockito.any(HttpContext.class));
		}
	}

	protected SourceClientREST prepareTestedInstance() {
		SourceClientREST tested = new SourceClientREST();
		CloseableHttpClient hcMock = Mockito.mock(CloseableHttpClient.class);
		tested.httpclient = hcMock;
		tested.restAPIUrlBase = "http://test.org/";
		return tested;
	}

	protected Answer<HttpResponse> prepareOKAnswerWithAssertions(final String expectedUrl,
			final boolean expectedDoAuthentication) {
		return new Answer<HttpResponse>() {

			@Override
			public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
				HttpGet method = (HttpGet) invocation.getArguments()[1];
				Assert.assertEquals(expectedUrl, method.getURI().toString());
				CloseableHttpResponse ret = Mockito.mock(CloseableHttpResponse.class);
				Mockito.when(ret.getStatusLine()).thenReturn(
						new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "reason"));
				return ret;
			}
		};
	}

	@Test
	public void prepareAPIURLFromBaseURL() {
		Assert.assertNull(SourceClientREST.prepareAPIURLFromBaseURL(null));
		Assert.assertNull(SourceClientREST.prepareAPIURLFromBaseURL(""));
		Assert.assertNull(SourceClientREST.prepareAPIURLFromBaseURL("  "));
		Assert.assertEquals("http://test.org/", SourceClientREST.prepareAPIURLFromBaseURL("http://test.org"));
		Assert.assertEquals("http://test.org/", SourceClientREST.prepareAPIURLFromBaseURL("http://test.org/"));
		Assert.assertEquals("http://test.org:5655/", SourceClientREST.prepareAPIURLFromBaseURL("http://test.org:5655"));
		Assert.assertEquals("http://test.org:5655/", SourceClientREST.prepareAPIURLFromBaseURL("http://test.org:5655/"));
		Assert.assertEquals("http://test.org:5655/es/",
				SourceClientREST.prepareAPIURLFromBaseURL("http://test.org:5655/es"));
		Assert.assertEquals("http://test.org:5655/es/",
				SourceClientREST.prepareAPIURLFromBaseURL("http://test.org:5655/es/"));
	}

	@Test
	public void prepareRequestParams() {
		Assert.assertNull(SourceClientREST.prepareRequestParams(null, null));
		Assert.assertNull(SourceClientREST.prepareRequestParams(null, "index"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		Assert.assertNull(SourceClientREST.prepareRequestParams(params, null));
		Assert.assertNull(SourceClientREST.prepareRequestParams(params, "index"));
		params.put("index", "myindex");
		Assert.assertNull(SourceClientREST.prepareRequestParams(params, "index"));

		NameValuePair[] ret = SourceClientREST.prepareRequestParams(params, null);
		Assert.assertEquals(new BasicNameValuePair("index", "myindex"), ret[0]);

		params.put("param2", "myparam2");
		ret = SourceClientREST.prepareRequestParams(params, "index");
		Assert.assertEquals(new BasicNameValuePair("param2", "myparam2"), ret[0]);

		ret = SourceClientREST.prepareRequestParams(params, null);
		Assert.assertEquals(new BasicNameValuePair("index", "myindex"), ret[0]);
		Assert.assertEquals(new BasicNameValuePair("param2", "myparam2"), ret[1]);

		params.put("param3", "myparam3");
		ret = SourceClientREST.prepareRequestParams(params, "unknown");
		Assert.assertEquals(new BasicNameValuePair("index", "myindex"), ret[0]);
		Assert.assertEquals(new BasicNameValuePair("param2", "myparam2"), ret[1]);
		Assert.assertEquals(new BasicNameValuePair("param3", "myparam3"), ret[2]);
	}

}

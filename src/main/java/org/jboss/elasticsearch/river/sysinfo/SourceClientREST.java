/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.SettingsException;

/**
 * {@link SourceClient} implementation using remote ES HTTP REST API calls.
 * <p>
 * Use next section in river configuration if you want to process informations from local ES cluster:
 * 
 * <pre>
 * "es_connection" : {
 *   "type"     : "rest",
 *   "urlBase"  : "http://localhost:9200",
 *   "timeout"  : "1s",
 *   "username" : "aaaa",
 *   "pwd"      : "bbb"
 * }
 * </pre>
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientREST extends SourceClientBase {

	protected static final String PARAM_NODE_ID = "nodeId";

	protected static final String PARAM_INDEX = "index";

	private static final ESLogger logger = Loggers.getLogger(SourceClientREST.class);

	protected CloseableHttpClient httpclient;

	protected String restAPIUrlBase;

	protected boolean isAuthConfigured = false;

	/**
	 * Constructor performing configuration.
	 * 
	 * @param sourceClientSettings Map of configuration parameters
	 * @throws SettingsException in case of configuration problem
	 */
	public SourceClientREST(Map<String, Object> sourceClientSettings) throws SettingsException {

		restAPIUrlBase = prepareAPIURLFromBaseURL((String) sourceClientSettings.get("urlBase"));
		if (restAPIUrlBase == null) {
			throw new SettingsException("Parameter es_connection/urlBase must be set!");
		}

		URL url = null;
		try {
			url = new URL(restAPIUrlBase);
		} catch (MalformedURLException e) {
			throw new SettingsException("Parameter es_connection/urlBase is malformed: " + e.getMessage());
		}

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultMaxPerRoute(20);
		connManager.setMaxTotal(20);

		ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Consts.UTF_8).build();
		connManager.setDefaultConnectionConfig(connectionConfig);

		HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(connManager);

		int timeout = (int) Utils.parseTimeValue(sourceClientSettings, "timeout", 5, TimeUnit.SECONDS);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
		clientBuilder.setDefaultRequestConfig(requestConfig);

		String username = Utils.trimToNull((String) sourceClientSettings.get("username"));
		String password = (String) sourceClientSettings.get("pwd");
		if (!Utils.isEmpty(username)) {
			if (password != null) {
				String host = url.getHost();
				CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(new AuthScope(host, AuthScope.ANY_PORT), new UsernamePasswordCredentials(
						username, password));
				clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				isAuthConfigured = true;
			} else {
				logger.warn("Password not found so authentication is not used!");
				username = null;
			}
		}
		httpclient = clientBuilder.build();
	}

	/**
	 * Constructor for unit tests, nothing is configured here.
	 */
	protected SourceClientREST() {

	}

	/**
	 * Prepare ES API URL from configured base URL.
	 * 
	 * @param baseURL base ES URL, ie. http://search.acme.org
	 * @return URL with trailing <code>/</code> or null if not configured properly.
	 */
	protected static String prepareAPIURLFromBaseURL(String baseURL) {
		if (Utils.isEmpty(baseURL))
			return null;
		if (!baseURL.endsWith("/")) {
			baseURL = baseURL + "/";
		}
		return baseURL;
	}

	@Override
	public void start() {
	}

	@Override
	public void close() {
	}

	@Override
	protected String readClusterStateInfo(Map<String, String> params) throws IOException, InterruptedException {
		return performRESTCall("_cluster/state", prepareRequestParams(params, null));
	}

	@Override
	protected String readClusterHealthInfo(Map<String, String> params) throws IOException, InterruptedException {
		String op = "_cluster/health";
		if (params != null && !Utils.isEmpty(params.get(PARAM_INDEX))) {
			op = op + "/" + params.get(PARAM_INDEX);
		}
		return performRESTCall(op, prepareRequestParams(params, PARAM_INDEX));
	}

	@Override
	protected String readClusterNodesInfoInfo(Map<String, String> params) throws IOException, InterruptedException {
		String op = "_cluster/nodes";
		if (params != null && !Utils.isEmpty(params.get(PARAM_NODE_ID))) {
			op = op + "/" + params.get(PARAM_NODE_ID);
		}
		return performRESTCall(op, prepareRequestParams(params, PARAM_NODE_ID));
	}

	@Override
	protected String readClusterNodesStatsInfo(Map<String, String> params) throws IOException, InterruptedException {
		String op = "_nodes";
		if (params != null && !Utils.isEmpty(params.get(PARAM_NODE_ID))) {
			op = op + "/" + params.get(PARAM_NODE_ID);
		}
		op = op + "/stats";
		return performRESTCall(op, prepareRequestParams(params, PARAM_NODE_ID));
	}

	@Override
	protected String readIndicesStatusInfo(Map<String, String> params) throws IOException, InterruptedException {
		String op = "";
		if (params != null && !Utils.isEmpty(params.get(PARAM_INDEX))) {
			op = params.get(PARAM_INDEX) + "/";
		}
		op = op + "_status";
		return performRESTCall(op, prepareRequestParams(params, PARAM_INDEX));
	}

	@Override
	protected String readIndicesStatsInfo(Map<String, String> params) throws IOException, InterruptedException {
		String op = "";
		if (params != null && !Utils.isEmpty(params.get(PARAM_INDEX))) {
			op = params.get(PARAM_INDEX) + "/";
		}
		op = op + "_stats";
		return performRESTCall(op, prepareRequestParams(params, PARAM_INDEX));
	}

	@Override
	protected String readIndicesSegmentsInfo(Map<String, String> params) throws IOException, InterruptedException {
		String op = "";
		if (params != null && !Utils.isEmpty(params.get(PARAM_INDEX))) {
			op = params.get(PARAM_INDEX) + "/";
		}
		op = op + "_segments";
		return performRESTCall(op, prepareRequestParams(params, PARAM_INDEX));
	}

	/**
	 * Prepare request params to be used by {@link #performRESTCall(String, NameValuePair[])}
	 * 
	 * @param params map of params to be converted
	 * @param removeParamName parameter which must be removed from output parameters, can be null
	 * @return array of parameters for request with removed removeParamName if defined
	 */
	protected static NameValuePair[] prepareRequestParams(Map<String, String> params, String removeParamName) {
		if (params == null || params.isEmpty()
				|| (params.size() == 1 && removeParamName != null && params.containsKey(removeParamName)))
			return null;
		List<NameValuePair> paramsOut = new ArrayList<NameValuePair>();
		if (params != null) {
			for (String paramName : params.keySet()) {
				if (removeParamName != null && removeParamName.equals(paramName))
					continue;
				paramsOut.add(new BasicNameValuePair(paramName, params.get(paramName)));
			}
		}
		return paramsOut.toArray(new NameValuePair[paramsOut.size()]);
	}

	/**
	 * Perform defined REST call to remote ES REST API.
	 * 
	 * @param restOperation name of REST operation to call on ES API (eg. '_cluster/state' or 'index1,index2/_stats' ).
	 *          It's appended to configured {@link #restAPIUrlBase}.
	 * @param requestParams GET parameters used for call
	 * @return response from server if successful
	 * @throws IOException in case of unsuccessful call
	 */
	protected String performRESTCall(String restOperation, NameValuePair[] requestParams) throws IOException {

		try {
			String url = restAPIUrlBase + restOperation;
			logger.debug("Go to perform ES REST API call to the {} with parameters {}", url, requestParams);

			URIBuilder builder = new URIBuilder(url);
			if (requestParams != null) {
				for (NameValuePair param : requestParams) {
					builder.addParameter(param.getName(), param.getValue());
				}
			}
			HttpGet method = new HttpGet(builder.build());
			method.addHeader("Accept", "application/json");

			CloseableHttpResponse response = null;
			try {

				// Preemptive authentication enabled - see
				// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html#d5e1032
				HttpHost targetHost = new HttpHost(builder.getHost(), builder.getPort(), builder.getScheme());
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(targetHost, basicAuth);
				HttpClientContext localcontext = HttpClientContext.create();
				localcontext.setAuthCache(authCache);

				response = httpclient.execute(targetHost, method, localcontext);
				int statusCode = response.getStatusLine().getStatusCode();
				String responseContent = null;
				if (response.getEntity() != null) {
					responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
				}
				if (statusCode != HttpStatus.SC_OK) {
					throw new IOException("Failed ES REST API call. HTTP error code: " + statusCode + " Response body: "
							+ responseContent);
				}
				return responseContent;
			} finally {
				if (response != null)
					response.close();
				method.releaseConnection();
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Failed ES REST API call: " + e.getMessage(), e);
		}
	}

}

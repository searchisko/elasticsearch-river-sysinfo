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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
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

  protected HttpClient httpclient;

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

    HttpConnectionManagerParams params = new HttpConnectionManagerParams();
    params.setDefaultMaxConnectionsPerHost(20);
    params.setMaxTotalConnections(20);

    int timeout = (int) Utils.parseTimeValue(sourceClientSettings, "timeout", 5, TimeUnit.SECONDS);
    params.setSoTimeout(timeout);
    params.setConnectionTimeout(timeout);

    MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    connectionManager.setParams(params);

    httpclient = new HttpClient(connectionManager);
    httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");

    String username = (String) sourceClientSettings.get("username");
    String password = (String) sourceClientSettings.get("pwd");
    if (!Utils.isEmpty(username)) {
      httpclient.getParams().setAuthenticationPreemptive(true);
      String host = url.getHost();
      httpclient.getState().setCredentials(new AuthScope(host, -1, null),
          new UsernamePasswordCredentials(username, password));
      isAuthConfigured = true;
    }
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
        paramsOut.add(new NameValuePair(paramName, params.get(paramName)));
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

    String url = restAPIUrlBase + restOperation;
    logger.debug("Go to perform ES REST API call to the {} with parameters {}", url, requestParams);

    HttpMethod method = new GetMethod(url);
    method.setDoAuthentication(isAuthConfigured);
    method.setFollowRedirects(true);
    method.addRequestHeader("Accept", "application/json");
    if (requestParams != null) {
      method.setQueryString(requestParams);
    }
    try {
      int statusCode = httpclient.executeMethod(method);

      if (statusCode != HttpStatus.SC_OK) {
        throw new IOException("Failed ES REST API call. HTTP error code: " + statusCode + " Response body: "
            + method.getResponseBodyAsString());
      }
      return method.getResponseBodyAsString();
    } finally {
      method.releaseConnection();
    }
  }

}

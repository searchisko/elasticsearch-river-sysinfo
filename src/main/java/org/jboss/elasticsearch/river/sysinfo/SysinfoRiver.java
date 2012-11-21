/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.jboss.elasticsearch.river.sysinfo.esclient.SourceClientESClient;
import org.jboss.elasticsearch.river.sysinfo.esclient.SourceClientESTransportClient;

/**
 * System Info River implementation class.
 * <p>
 * Example of river configuration:
 * 
 * <pre>
 * {
 *     "type" : "sysinfo",
 *     "es_connection" : {
 *         "type" : "local"
 *     },
 *     "indexers" : {
 *       "cluster_health" : {
 *           "info_type"   : "cluster_health",
 *           "index_name"  : "my_index_1",
 *           "index_type"  : "my_type_1",
 *           "period"      : "1m",
 *           "params" : {
 *               "level" : "shards"
 *           }
 *       },
 *       "cluster_state" : {
 *           "info_type"   : "cluster_state",
 *           "index_name"  : "my_index_1",
 *           "index_type"  : "my_type_1",
 *           "period"      : "1m"
 *       }
 *     }
 * }
 * 
 * </pre>
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoRiver extends AbstractRiverComponent implements River, IRiverMgm {

  /**
   * Map of running river instances. Used for management operations dispatching. See {@link #getRunningInstance(String)}
   */
  protected static Map<String, IRiverMgm> riverInstances = new HashMap<String, IRiverMgm>();

  /**
   * Local ElasticSearch client to be used for indexing.
   */
  protected Client client;

  /**
   * Flag set to true if this river is stopped from ElasticSearch server.
   */
  protected volatile boolean closed = true;

  /**
   * Source client used by this river.
   */
  protected SourceClient sourceClient;

  /**
   * List of configured indexers.
   */
  protected Map<String, SysinfoIndexer> indexers = new LinkedHashMap<String, SysinfoIndexer>();

  /**
   * List of running indexer threads.
   */
  protected List<Thread> indexerThreads = new ArrayList<Thread>();

  /**
   * Public constructor used by ElasticSearch.
   * 
   * @param riverName
   * @param settings
   * @param client
   * @throws MalformedURLException
   */
  @Inject
  public SysinfoRiver(RiverName riverName, RiverSettings settings, Client client) throws MalformedURLException {
    super(riverName, settings);
    this.client = client;
    configure(settings.settings());
  }

  /**
   * Constructor for unit tests, nothing is initialized/configured in river.
   * 
   * @param riverName
   * @param settings
   */
  protected SysinfoRiver(RiverName riverName, RiverSettings settings) {
    super(riverName, settings);
  }

  /**
   * Configure river.
   * 
   * @param settings used for configuration.
   */
  @SuppressWarnings("unchecked")
  protected void configure(Map<String, Object> settings) {

    if (!closed)
      throw new IllegalStateException("Sysinfo River must be stopped to configure it!");
    String type = null;
    if (settings.containsKey("es_connection")) {
      Map<String, Object> sourceClientSettings = (Map<String, Object>) settings.get("es_connection");
      type = XContentMapValues.nodeStringValue(sourceClientSettings.get("type"), null);
      if (Utils.isEmpty(type)) {
        throw new SettingsException("es_connection/type element of configuration structure not found or empty");
      }
      if ("local".equalsIgnoreCase(type)) {
        sourceClient = new SourceClientESClient(client);
      } else if ("remote".equalsIgnoreCase(type)) {
        sourceClient = new SourceClientESTransportClient(sourceClientSettings);
      } else if ("rest".equalsIgnoreCase(type)) {
        sourceClient = new SourceClientREST(sourceClientSettings);
      } else {
        throw new SettingsException("es_connection/type value '" + type
            + "' is invalid. Use one of local, remote, rest");
      }
    } else {
      throw new SettingsException("'es_connection' element of river configuration structure not found");
    }

    Map<String, Map<String, Object>> indexersMap = (Map<String, Map<String, Object>>) settings.get("indexers");
    if (indexersMap != null && !indexersMap.isEmpty()) {
      for (String name : indexersMap.keySet()) {
        name = name.trim();
        if (indexers.containsKey(name)) {
          throw new SettingsException("Duplicate 'indexers/" + name + "' section");
        }
        Map<String, Object> ic = indexersMap.get(name);
        SysinfoType infoType = SysinfoType.parseConfiguration((String) ic.get("info_type"));
        String indexName = configMandatoryString(ic, "index_name", name);
        String typeName = configMandatoryString(ic, "index_type", name);
        long indexingPeriod = Utils.parseTimeValue(ic, "period", 30, TimeUnit.SECONDS);
        Map<String, String> params = (Map<String, String>) ic.get("params");
        indexers.put(name, new SysinfoIndexer(name, sourceClient, client, infoType, indexName, typeName,
            indexingPeriod, params));
      }
    } else {
      throw new SettingsException("'indexers' element of river configuration structure not found or is empty");
    }

    logger.info("Sysinfo River configured for connection type '{}' and {} indexers.", type, indexers.size());
  }

  private String configMandatoryString(Map<String, Object> settings, String key, String parentName) {
    String s = (String) settings.get(key);
    if (Utils.isEmpty(s)) {
      throw new SettingsException("'indexers/" + parentName + "/" + key
          + "' river configuration element not found or is empty");
    }
    return s;
  }

  @Override
  public synchronized void start() {
    if (!closed)
      throw new IllegalStateException("Can't start already running river");
    logger.info("starting Sysinfo River");
    synchronized (riverInstances) {
      addRunningInstance(this);
    }
    sourceClient.start();
    closed = false;
    for (SysinfoIndexer indexer : indexers.values()) {
      Thread t = acquireThread("sysinfo_river_" + indexer.name, indexer);
      indexerThreads.add(t);
      t.start();
    }
    logger.info("Sysinfo River started");
  }

  @Override
  public synchronized void close() {
    logger.info("closing Sysinfo River on this node");
    closed = true;
    try {
      for (SysinfoIndexer indexer : indexers.values()) {
        try {
          indexer.close();
        } catch (Throwable t) {
          logger.warn("Exception during {} indexer closing: {}", indexer.name, t.getMessage());
        }
      }
      // let threads some time to finish
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        // nothing to do
      }
      // and interrupt them if not finished yet
      for (Thread pi : indexerThreads) {
        pi.interrupt();
      }
      indexerThreads.clear();
    } finally {
      try {
        sourceClient.close();
      } finally {
        logger.info("Sysinfo River closed");
        synchronized (riverInstances) {
          riverInstances.remove(riverName().getName());
        }
      }
    }
  }

  /**
   * Get running instance of river for given name. Used for REST management operations handling.
   * 
   * @param riverName to get instance for
   * @return river instance or null if not found
   * @see #addRunningInstance(IRiverMgm)
   * @see #getRunningInstances()
   */
  public static IRiverMgm getRunningInstance(String riverName) {
    if (riverName == null)
      return null;
    return riverInstances.get(riverName);
  }

  /**
   * Put running instance of jira river into registry. Used for REST management operations handling.
   * 
   * @param riverName to get instance for
   * @see #getRunningInstances()
   * @see #getRunningInstance(String)
   */
  public static void addRunningInstance(IRiverMgm river) {
    riverInstances.put(river.riverName().getName(), river);
  }

  /**
   * Get running instances of all sysinfo rivers. Used for REST management operations handling.
   * 
   * @return Set with names of all river instances registered for management
   * @see #addRunningInstance(IRiverMgm)
   * @see #getRunningInstance(String)
   */
  public static Set<String> getRunningInstances() {
    return Collections.unmodifiableSet((riverInstances.keySet()));
  }

  protected Thread acquireThread(String threadName, Runnable runnable) {
    return EsExecutors.daemonThreadFactory(settings.globalSettings(), threadName).newThread(runnable);
  }

}

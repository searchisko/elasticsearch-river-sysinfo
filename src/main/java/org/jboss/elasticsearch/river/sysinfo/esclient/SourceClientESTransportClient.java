/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.jboss.elasticsearch.river.sysinfo.SourceClient;
import org.jboss.elasticsearch.river.sysinfo.Utils;

/**
 * {@link SourceClient} implementation using {@link TransportClient} instance pointing to remote ES cluster.
 * <p>
 * Use next section in river configuration if you want to obtain information from remote ES cluster using transport
 * protocol:
 * 
 * <pre>
 * "es_connection" : {
 *   "type" : "remote",
 *   "addresses" : [
 *     {"host": "host1", "port" : "9300"},
 *     {"host": "host2", "port" : "9300"}
 *   ],
 *   "settings" : {
 *     "cluster.name" : "myCluster",
 *     "client.transport.ping_timeout" : "10"
 *   }
 * }
 * </pre>
 * 
 * <code>settings</code> part is optional, for description of available settings see <code>Transport Client</code>
 * section of <a href="http://www.elasticsearch.org/guide/reference/java-api/client.html"
 * >http://www.elasticsearch.org/guide/reference/java-api/client.html</a>.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SourceClientESTransportClient extends SourceClientESClient {

  private static final ESLogger logger = Loggers.getLogger(SourceClientESTransportClient.class);

  protected TransportAddress[] transportAddresses;
  protected Map<String, String> settingsConf;

  /**
   * Create and configure client.
   * 
   * @param sourceClientSettings configurations to be used.
   * @throws SettingsException in case of configuration problem
   */
  @SuppressWarnings("unchecked")
  public SourceClientESTransportClient(Map<String, Object> sourceClientSettings) throws SettingsException {
    super(null);
    List<Map<String, Object>> adr = (List<Map<String, Object>>) sourceClientSettings.get("addresses");
    if (adr == null || adr.isEmpty()) {
      throw new SettingsException("es_connection/addresses element of configuration structure not found or is empty");
    }
    transportAddresses = new TransportAddress[adr.size()];
    int i = 0;
    for (Map<String, Object> a : adr) {
      if (Utils.isEmpty(a.get("host"))) {
        throw new SettingsException(
            "es_connection/addresses/host element of configuration structure not found or is empty");
      }
      if (Utils.isEmpty(a.get("port"))) {
        throw new SettingsException(
            "es_connection/addresses/port element of configuration structure not found or is empty");
      }
      transportAddresses[i++] = new InetSocketTransportAddress((String) a.get("host"), Utils.nodeIntegerValue(a
          .get("port")));
    }

    settingsConf = (Map<String, String>) sourceClientSettings.get("settings");
    if (settingsConf == null) {
      settingsConf = new HashMap<String, String>();
    }
  }

  /**
   * For unit tests, no any config performed inside.
   */
  protected SourceClientESTransportClient() {
    super(null);
  }

  @Override
  public synchronized void start() {
    if (client != null) {
      logger.info("Client started already, ignoring start() call");
      return;
    }
    TransportClient tclient = new TransportClient(ImmutableSettings.settingsBuilder().put(settingsConf).build());
    tclient.addTransportAddresses(transportAddresses);
    client = tclient;
  }

  @Override
  public synchronized void close() {
    if (client != null) {
      try {
        client.close();
      } finally {
        client = null;
      }
    }
  }

  public TransportAddress[] getTransportAddresses() {
    return transportAddresses;
  }

  public Map<String, String> getSettingsConf() {
    return settingsConf;
  }

}

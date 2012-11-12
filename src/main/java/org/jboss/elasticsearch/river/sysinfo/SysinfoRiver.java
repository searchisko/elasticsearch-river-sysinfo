/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.net.MalformedURLException;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;

/**
 * System Info River implementation class.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoRiver extends AbstractRiverComponent implements River {

  /**
   * Local ElasticSearch client to be used for indexing.
   */
  protected Client client;

  /**
   * Flag set to true if this river is stopped from ElasticSearch server.
   */
  protected volatile boolean closed = true;

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
   * Configure river.
   * 
   * @param settings used for configuration.
   */
  protected void configure(Map<String, Object> settings) {

    if (!closed)
      throw new IllegalStateException("Sysinfo River must be stopped to configure it!");

    // TODO read configuration
  }

  @Override
  public synchronized void start() {
    if (!closed)
      throw new IllegalStateException("Can't start already running river");
    logger.info("starting Sysinfo River");
    closed = false;

    // TODO start indexing threads
  }

  @Override
  public synchronized void close() {
    logger.info("closing Sysinfo River on this node");
    closed = true;
    // TODO stop indexing threads
  }

}

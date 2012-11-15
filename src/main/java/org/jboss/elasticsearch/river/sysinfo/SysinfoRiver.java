/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.util.concurrent.EsExecutors;
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
   * List of configured indexers.
   */
  protected List<SysinfoIndexer> indexers = new ArrayList<SysinfoIndexer>();

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
    for (SysinfoIndexer indexer : indexers) {
      Thread t = acquireThread("sysinfo_river_" + indexer.infoType.getName(), indexer);
      indexerThreads.add(t);
      t.start();
    }
    logger.info("Sysinfo River started");
  }

  @Override
  public synchronized void close() {
    logger.info("closing Sysinfo River on this node");
    closed = true;
    for (SysinfoIndexer indexer : indexers) {
      indexer.stop();
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
    logger.info("Sysinfo River closed");
  }

  protected Thread acquireThread(String threadName, Runnable runnable) {
    return EsExecutors.daemonThreadFactory(settings.globalSettings(), threadName).newThread(runnable);
  }

}

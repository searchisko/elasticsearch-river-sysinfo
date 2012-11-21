/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

/**
 * Thread used to index one type of system information.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoIndexer implements Runnable {

  private static final ESLogger logger = Loggers.getLogger(SysinfoIndexer.class);

  protected boolean closed = true;

  protected String name;
  protected SourceClient sourceClient;
  protected Client targetClient;
  protected SysinfoType infoType;
  protected String indexName;
  protected String typeName;
  protected long indexingPeriod = 0;
  protected Map<String, String> params;

  /**
   * Indexer constructor.
   * 
   * @param name name of indexer
   * @param sourceClient used to get informations from. Can be local or remote etc.
   * @param targetClient used to store informations into
   * @param infoType type of information indexed by this indexer
   * @param indexName name of index to store information into
   * @param typeName type of document in index o store information into
   * @param indexingPeriod indexing period [ms]
   * @param params additional parameters from info obtaining - possible content depends on infoType
   */
  public SysinfoIndexer(String name, SourceClient sourceClient, Client targetClient, SysinfoType infoType,
      String indexName, String typeName, long indexingPeriod, Map<String, String> params) {
    this.name = name;
    this.sourceClient = sourceClient;
    this.targetClient = targetClient;
    this.infoType = infoType;
    this.indexName = indexName;
    this.typeName = typeName;
    this.indexingPeriod = indexingPeriod;
    this.params = params;
  }

  /**
   * For unit test.
   */
  protected SysinfoIndexer(SourceClient sourceClient, Client targetClient) {
    this.sourceClient = sourceClient;
    this.targetClient = targetClient;
  }

  @Override
  public void run() {
    logger.info("Sysinfo river {} indexer started", name);
    closed = false;
    try {
      while (true) {
        if (closed) {
          return;
        }
        long start = System.currentTimeMillis();
        try {
          processLoopTask();
        } catch (InterruptedException e1) {
          return;
        } catch (Exception e) {
          if (closed)
            return;
          logger.error("Failed to process Sysinfo {} indexer due: {}", e, name, e.getMessage());
        }
        try {
          if (closed)
            return;
          long waitFor = indexingPeriod - (System.currentTimeMillis() - start);
          logger.debug("Sysinfo river {} indexer is going to sleep for {} ms", name, waitFor);
          if (waitFor > 0) {
            Thread.sleep(waitFor);
          }
        } catch (InterruptedException e1) {
          return;
        }
      }
    } finally {
      logger.info("Sysinfo river {} indexer stopped", name);
      closed = true;
    }
  }

  /**
   * Close indexer at the end of use.
   */
  public void close() {
    closed = true;
  }

  /**
   * Process indexing tasks.
   * 
   * @throws Exception
   * @throws InterruptedException id interrupted
   */
  protected void processLoopTask() throws Exception, InterruptedException {
    String content = sourceClient.readSysinfoValue(infoType, params);
    targetClient.prepareIndex(indexName, typeName).setSource(content).execute().actionGet();
  }

  @Override
  public String toString() {
    return "SysinfoIndexer [name=" + name + " infoType=" + infoType + ", indexName=" + indexName + ", typeName="
        + typeName + ", indexingPeriod=" + indexingPeriod + ", params=" + params + ", closed=" + closed + "]";
  }

}

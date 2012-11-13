/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

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

  protected SourceClient sourceClient;
  protected Client targetClient;
  protected SysinfoType infoType;
  protected String indexName;
  protected String typeName;
  protected long indexingPeriod = 0;

  public SysinfoIndexer(SourceClient sourceClient, Client targetClient, SysinfoType infoType, String indexName,
      String typeName, long indexingPeriod) {
    this.sourceClient = sourceClient;
    this.targetClient = targetClient;
    this.infoType = infoType;
    this.indexName = indexName;
    this.typeName = typeName;
    this.indexingPeriod = indexingPeriod;
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
    logger.info("Sysinfo river indexing task started");
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
          logger.error("Failed to process Sysinfo indexing task {}", e, e.getMessage());
        }
        try {
          if (closed)
            return;
          long waitFor = indexingPeriod - (System.currentTimeMillis() - start);
          logger.debug("Sysinfo river indexing task is going to sleep for {} ms", waitFor);
          if (waitFor > 0) {
            Thread.sleep(waitFor);
          }
        } catch (InterruptedException e1) {
          return;
        }
      }
    } finally {
      logger.info("Sysinfo river indexing task stopped");
      closed = true;
    }
  }

  public void stop() {
    closed = true;
  }

  /**
   * Process indexing tasks.
   * 
   * @throws Exception
   * @throws InterruptedException id interrupted
   */
  protected void processLoopTask() throws Exception, InterruptedException {
    String content = sourceClient.readSysinfoValue(infoType);
    targetClient.prepareIndex(indexName, typeName).setSource(content).execute().actionGet();
  }

}

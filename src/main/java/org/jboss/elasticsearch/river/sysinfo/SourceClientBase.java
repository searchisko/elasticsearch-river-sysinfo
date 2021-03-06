/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;
import java.util.Map;

/**
 * Abstract base implementation of {@link SourceClient} interface for simpler implementations.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public abstract class SourceClientBase implements SourceClient {

  @Override
  public String readSysinfoValue(SysinfoType infoType, Map<String, String> params) throws IOException,
      InterruptedException {
    if (infoType == null)
      throw new IllegalArgumentException("infoType parameter must be defined");
    switch (infoType) {
    case CLUSTER_HEALTH:
      return readClusterHealthInfo(params);
    case CLUSTER_STATE:
      return readClusterStateInfo(params);
    case CLUSTER_STATS:
      return readClusterStatsInfo(params);
    case PENDING_CLUSTER_TASKS:
      return readPendingClusterTasksInfo(params);
    case CLUSTER_NODES_INFO:
      return readClusterNodesInfoInfo(params);
    case CLUSTER_NODES_STATS:
      return readClusterNodesStatsInfo(params);
    case INDICES_STATUS:
      return readIndicesStatusInfo(params);
    case INDICES_STATS:
      return readIndicesStatsInfo(params);
    case INDICES_SEGMENTS:
      return readIndicesSegmentsInfo(params);
    case INDICES_RECOVERY:
      return readIndicesRecoveryInfo(params);
    default:
      throw new UnsupportedOperationException("Unsupported information type: " + infoType);
    }
  }

  /**
   * Load information for {@link SysinfoType#CLUSTER_STATE} type.
   * 
   * @param params configured to narrow down information
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterStateInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#CLUSTER_HEALTH} type.
   * 
   * @param params configured to narrow down information
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterHealthInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#CLUSTER_STATS} type.
   *
   * @param params
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterStatsInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#PENDING_CLUSTER_TASKS} type.
   *
   * @param params
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readPendingClusterTasksInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#CLUSTER_NODES_INFO} type.
   * 
   * @param params configured to narrow down information
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterNodesInfoInfo(Map<String, String> params) throws IOException,
      InterruptedException;

  /**
   * Load information for {@link SysinfoType#CLUSTER_NODES_STATS} type.
   * 
   * @param params configured to narrow down information
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterNodesStatsInfo(Map<String, String> params) throws IOException,
      InterruptedException;

  /**
   * Load information for {@link SysinfoType#INDICES_STATUS} type.
   * 
   * @param params configured to narrow down information
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readIndicesStatusInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#INDICES_STATS} type.
   * 
   * @param params configured to narrow down information
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readIndicesStatsInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#INDICES_SEGMENTS} type.
   * 
   * @param params configured to narrow down information
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readIndicesSegmentsInfo(Map<String, String> params) throws IOException,
      InterruptedException;

  /**
   * Load information for {@link SysinfoType#INDICES_RECOVERY} type.
   *
   * @param params
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readIndicesRecoveryInfo(Map<String, String> params) throws IOException,
      InterruptedException;

}

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
    default:
      throw new UnsupportedOperationException("Unsupported information type: " + infoType);
    }
  }

  /**
   * Load information for {@link SysinfoType#CLUSTER_STATE} type.
   * 
   * @param params configured to narrow down informations
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterStateInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#CLUSTER_HEALTH} type.
   * 
   * @param params configured to narrow down informations
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterHealthInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#CLUSTER_NODES_INFO} type.
   * 
   * @param params configured to narrow down informations
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterNodesInfoInfo(Map<String, String> params) throws IOException,
      InterruptedException;

  /**
   * Load information for {@link SysinfoType#CLUSTER_NODES_STATS} type.
   * 
   * @param params configured to narrow down informations
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readClusterNodesStatsInfo(Map<String, String> params) throws IOException,
      InterruptedException;

  /**
   * Load information for {@link SysinfoType#INDICES_STATUS} type.
   * 
   * @param params configured to narrow down informations
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readIndicesStatusInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#INDICES_STATS} type.
   * 
   * @param params configured to narrow down informations
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readIndicesStatsInfo(Map<String, String> params) throws IOException, InterruptedException;

  /**
   * Load information for {@link SysinfoType#INDICES_SEGMENTS} type.
   * 
   * @param params configured to narrow down informations
   * @return information JSON string
   * @throws IOException
   * @throws InterruptedException
   */
  protected abstract String readIndicesSegmentsInfo(Map<String, String> params) throws IOException,
      InterruptedException;

}

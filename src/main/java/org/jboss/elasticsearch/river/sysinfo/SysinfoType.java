/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import org.elasticsearch.common.settings.SettingsException;

/**
 * Enum with names of distinct ElasticSearch system info types which can be stored into ES index using this river.
 * <p>
 * After new type is added here do not forget to add necessary implementation into
 * {@link SourceClientBase#readSysinfoValue(SysinfoType, java.util.Map)}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public enum SysinfoType {

  /**
   * http://www.elasticsearch.org/guide/reference/api/admin-cluster-health.html
   */
  CLUSTER_HEALTH("cluster_health"),
  /**
   * http://www.elasticsearch.org/guide/reference/api/admin-cluster-state.html
   */
  CLUSTER_STATE("cluster_state");

  private String name;

  private SysinfoType(String name) {
    this.name = name;
  }

  /**
   * Get name of this type. Used in config files also.
   * 
   * @return name of type
   */
  public String getName() {
    return name;
  }

  /**
   * Get enum value based on String value read from configuration file.
   * 
   * @param value to be parsed (values stored in {@link #name} are supported here)
   * @return Enum value, never null
   * @throws SettingsException for bad value
   */
  public static SysinfoType parseConfiguration(String value) throws SettingsException {
    if (value == null) {
      throw new SettingsException("unsupported name for indexed information type: " + value);
    }
    for (SysinfoType t : values()) {
      if (t.getName().equalsIgnoreCase(value))
        return t;
    }
    throw new SettingsException("unsupported name for indexed information type: " + value);
  }

}
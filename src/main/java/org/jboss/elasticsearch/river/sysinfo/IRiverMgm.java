package org.jboss.elasticsearch.river.sysinfo;

import org.elasticsearch.river.RiverName;

/**
 * Interface with river management operations called over REST API.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public interface IRiverMgm {

  /**
   * Get name of river.
   * 
   * @return name of jira river
   */
  public abstract RiverName riverName();

}
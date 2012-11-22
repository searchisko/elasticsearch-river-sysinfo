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
   * @return name of river
   */
  public abstract RiverName riverName();

  /**
   * Stop river indexers, but leave instance existing in {@link #riverInstances} so it can be found over management REST
   * calls and/or reconfigured and started later again. Note that standard ES river {@link #close()} method
   * implementation removes river instance from {@link #riverInstances}.
   */
  public abstract void stop();

  /**
   * Restart river. Configuration of river is updated during restart from <code>_meta</code> file.
   */
  public abstract void restart();

  /**
   * Change indexing period for indexers.
   * 
   * @param indexerNames names of indexers to change period for
   * @param indexingPeriod to use
   * @return true if at least one indexer of passed names was found
   */
  public abstract boolean changeIndexerPeriod(String[] indexerNames, long indexingPeriod);

}
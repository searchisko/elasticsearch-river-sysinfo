/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;
import java.util.Map;

/**
 * Abstraction interface used to read status info from ES cluster over distinct protocols.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public interface SourceClient {

  /**
   * Read status information of given type from ES cluster.
   * 
   * 
   * @param infoType type of information to read
   * @param params additional params from info obtaining
   * @return read information
   * @throws IOException
   * @throws InterruptedException
   */
  public String readSysinfoValue(SysinfoType infoType, Map<String, String> params) throws IOException,
      InterruptedException;

}

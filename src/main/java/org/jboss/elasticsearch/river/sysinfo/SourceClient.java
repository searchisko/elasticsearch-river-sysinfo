/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;

/**
 * Abstraction interface used to read status info from ES cluster over distinct protocols.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public interface SourceClient {

  /**
   * Read status information of given type from ES cluster.
   * 
   * @param infoType type of information to read
   * @return read information
   * @throws IOException
   * @throws InterruptedException
   */
  public String readSysinfoValue(SysinfoType infoType) throws IOException, InterruptedException;

}

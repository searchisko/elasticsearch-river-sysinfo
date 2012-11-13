/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;

/**
 * Abstract base implementation of {@link SourceClient} interface for simpler subclassing for concrete implementations.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public abstract class SourceClientBase implements SourceClient {

  @Override
  public String readSysinfoValue(SysinfoType infoType) throws IOException, InterruptedException {
    if (infoType == null)
      throw new IllegalArgumentException("infoType parameter must be defined");
    switch (infoType) {
    case HEALTH:
      return readHealthInfo();
    case STATE:
      return readStateInfo();
      // TODO implement other types of informations
    default:
      throw new UnsupportedOperationException("Unsupported information type: " + infoType);
    }
  }

  protected abstract String readStateInfo() throws IOException, InterruptedException;

  protected abstract String readHealthInfo() throws IOException, InterruptedException;

}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.mgm.period;

import java.io.IOException;
import java.util.Arrays;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.jboss.elasticsearch.river.sysinfo.mgm.JRMgmBaseRequest;

/**
 * Request for SysinfoRiver indexers period change.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JRPeriodRequest extends JRMgmBaseRequest {

  protected String[] indexerNames;
  protected long period;

  JRPeriodRequest() {

  }

  /**
   * Construct request.
   * 
   * @param riverName for request
   * @param indexerNames names of indexers to change period for
   * @param period new period to set
   */
  public JRPeriodRequest(String riverName, String[] indexerNames, long period) {
    super(riverName);
    this.indexerNames = indexerNames;
    this.period = period;
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    super.readFrom(in);
    if (in.readBoolean()) {
      indexerNames = in.readStringArray();
    }
    period = in.readLong();
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    super.writeTo(out);
    out.writeBoolean(indexerNames != null);
    if (indexerNames != null) {
      out.writeStringArray(indexerNames);
    }
    out.writeLong(period);
  }

  public String[] getIndexerNames() {
    return indexerNames;
  }

  public void setIndexerNames(String[] indexerNames) {
    this.indexerNames = indexerNames;
  }

  public long getPeriod() {
    return period;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  @Override
  public String toString() {
    return "JRPeriodRequest [riverName=" + riverName + ", indexerNames=" + Arrays.toString(indexerNames) + ", period="
        + period + "]";
  }

}

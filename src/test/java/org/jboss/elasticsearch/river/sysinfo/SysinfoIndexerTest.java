/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test {@link SysinfoIndexer}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoIndexerTest {

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void processLoopTask() throws InterruptedException, Exception {

    SourceClient scMock = Mockito.mock(SourceClient.class);
    Client tMock = Mockito.mock(Client.class);

    SysinfoIndexer tested = new SysinfoIndexer(scMock, tMock);
    tested.infoType = SysinfoType.HEALTH;
    Mockito.when(scMock.readSysinfoValue(SysinfoType.HEALTH)).thenReturn("{test : test 2}");

    IndexRequestBuilder irbMock = Mockito.mock(IndexRequestBuilder.class);
    Mockito.when(tMock.prepareIndex(tested.indexName, tested.typeName)).thenReturn(irbMock);
    Mockito.when(irbMock.setSource("{test : test 2}")).thenReturn(irbMock);
    ListenableActionFuture lafMock = Mockito.mock(ListenableActionFuture.class);
    Mockito.when(irbMock.execute()).thenReturn(lafMock);

    tested.processLoopTask();

    Mockito.verify(scMock).readSysinfoValue(SysinfoType.HEALTH);
    Mockito.verify(tMock).prepareIndex(tested.indexName, tested.typeName);
    Mockito.verify(irbMock).setSource("{test : test 2}");
    Mockito.verify(irbMock).execute();
    Mockito.verify(lafMock).actionGet();

  }
}

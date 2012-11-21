/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

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

  @Test
  public void constructor() {
    SourceClient scMock = Mockito.mock(SourceClient.class);
    Client tcMock = Mockito.mock(Client.class);
    Map<String, String> settings = new HashMap<String, String>();
    SysinfoIndexer tested = new SysinfoIndexer("my name", scMock, tcMock, SysinfoType.CLUSTER_HEALTH, "my index",
        "my type", 125, settings);

    Assert.assertEquals("my name", tested.name);
    Assert.assertEquals(scMock, tested.sourceClient);
    Assert.assertEquals(tcMock, tested.targetClient);
    Assert.assertEquals(SysinfoType.CLUSTER_HEALTH, tested.infoType);
    Assert.assertEquals("my index", tested.indexName);
    Assert.assertEquals("my type", tested.typeName);
    Assert.assertEquals(125, tested.indexingPeriod);
    Assert.assertEquals(settings, tested.params);
    Assert.assertEquals(true, tested.closed);

  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void processLoopTask() throws InterruptedException, Exception {

    SourceClient scMock = Mockito.mock(SourceClient.class);
    Client tcMock = Mockito.mock(Client.class);
    Map<String, String> paramsMock = new HashMap<String, String>();

    SysinfoIndexer tested = new SysinfoIndexer("testindexer", scMock, tcMock);
    tested.infoType = SysinfoType.CLUSTER_HEALTH;
    tested.params = paramsMock;
    Mockito.when(scMock.readSysinfoValue(SysinfoType.CLUSTER_HEALTH, paramsMock)).thenReturn("{test : test 2}");

    IndexRequestBuilder irbMock = Mockito.mock(IndexRequestBuilder.class);
    Mockito.when(tcMock.prepareIndex(tested.indexName, tested.typeName)).thenReturn(irbMock);
    Mockito.when(irbMock.setSource("{test : test 2}")).thenReturn(irbMock);
    ListenableActionFuture lafMock = Mockito.mock(ListenableActionFuture.class);
    Mockito.when(irbMock.execute()).thenReturn(lafMock);

    tested.processLoopTask();

    Mockito.verify(scMock).readSysinfoValue(SysinfoType.CLUSTER_HEALTH, paramsMock);
    Mockito.verify(tcMock).prepareIndex(tested.indexName, tested.typeName);
    Mockito.verify(irbMock).setSource("{test : test 2}");
    Mockito.verify(irbMock).execute();
    Mockito.verify(lafMock).actionGet();
  }

  @Test
  public void runAndClose() throws Exception {
    SourceClient scMock = Mockito.mock(SourceClient.class);
    Client tcMock = Mockito.mock(Client.class);
    SysinfoIndexer tested = new SysinfoIndexer("testindexer", scMock, tcMock);
    tested.infoType = SysinfoType.CLUSTER_HEALTH;
    tested.indexingPeriod = 50;

    // case - run changes closed status at begin, exception do not finish it, but finishes correctly when indexer is
    // stopped
    {
      Mockito.when(scMock.readSysinfoValue(SysinfoType.CLUSTER_HEALTH, null)).thenThrow(
          new RuntimeException("mocked exception"));
      Thread t = new Thread(tested);
      t.start();
      while (tested.closed) {
        Thread.sleep(10);
      }
      Thread.sleep(70);
      Assert.assertFalse(tested.closed);
      tested.close();
      Thread.sleep(50);
      Assert.assertTrue(tested.closed);
    }

    // case - InteruuptedException finishes indexer correctly
    {
      Mockito.reset(scMock, tcMock);
      Mockito.when(scMock.readSysinfoValue(SysinfoType.CLUSTER_HEALTH, null)).thenThrow(
          new InterruptedException("mocked exception"));
      Thread t = new Thread(tested);
      t.start();
      Thread.sleep(200);
      Assert.assertTrue(tested.closed);
    }

  }

}

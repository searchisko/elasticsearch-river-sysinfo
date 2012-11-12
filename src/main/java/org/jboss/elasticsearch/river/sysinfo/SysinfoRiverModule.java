package org.jboss.elasticsearch.river.sysinfo;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.river.River;

/**
 * System Info River ElasticSearch Module class.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoRiverModule extends ActionModule {

  public SysinfoRiverModule() {
    super(true);
  }

  @Override
  protected void configure() {
    bind(River.class).to(SysinfoRiver.class).asEagerSingleton();
  }
}
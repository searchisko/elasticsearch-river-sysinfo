package org.jboss.elasticsearch.river.sysinfo;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.river.RiversModule;

/**
 * System Info River ElasticSearch Plugin class.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SysinfoRiverPlugin extends AbstractPlugin {

  @Inject
  public SysinfoRiverPlugin() {
  }

  @Override
  public String name() {
    return "river-sysinfo";
  }

  @Override
  public String description() {
    return "River Sysinfo Plugin";
  }

  public void onModule(RiversModule module) {
    module.registerRiver("sysinfo", SysinfoRiverModule.class);
  }

  public void onModule(RestModule module) {
  }

  public void onModule(ActionModule module) {
  }
}

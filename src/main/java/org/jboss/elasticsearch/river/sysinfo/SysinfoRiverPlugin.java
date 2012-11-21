package org.jboss.elasticsearch.river.sysinfo;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.river.RiversModule;
import org.jboss.elasticsearch.river.sysinfo.mgm.lifecycle.JRLifecycleAction;
import org.jboss.elasticsearch.river.sysinfo.mgm.lifecycle.RestJRLifecycleAction;
import org.jboss.elasticsearch.river.sysinfo.mgm.lifecycle.TransportJRLifecycleAction;
import org.jboss.elasticsearch.river.sysinfo.mgm.riverslist.ListRiversAction;
import org.jboss.elasticsearch.river.sysinfo.mgm.riverslist.RestListRiversAction;
import org.jboss.elasticsearch.river.sysinfo.mgm.riverslist.TransportListRiversAction;

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
    module.addRestAction(RestListRiversAction.class);
    module.addRestAction(RestJRLifecycleAction.class);
  }

  public void onModule(ActionModule module) {
    module.registerAction(ListRiversAction.INSTANCE, TransportListRiversAction.class);
    module.registerAction(JRLifecycleAction.INSTANCE, TransportJRLifecycleAction.class);
  }
}

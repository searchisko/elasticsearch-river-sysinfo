package org.jboss.elasticsearch.river.sysinfo.mgm.lifecycle;

import org.jboss.elasticsearch.river.sysinfo.mgm.NodeJRMgmBaseRequest;

/**
 * Node request for SysinfoRiver lifecycle command.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class NodeJRLifecycleRequest extends NodeJRMgmBaseRequest<JRLifecycleRequest> {

  NodeJRLifecycleRequest() {
    super();
  }

  /**
   * Construct node request with data.
   * 
   * @param nodeId this request is for
   * @param request to be send to the node
   */
  public NodeJRLifecycleRequest(String nodeId, JRLifecycleRequest request) {
    super(nodeId, request);
  }

  @Override
  protected JRLifecycleRequest newRequest() {
    return new JRLifecycleRequest();
  }

}

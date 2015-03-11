/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
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

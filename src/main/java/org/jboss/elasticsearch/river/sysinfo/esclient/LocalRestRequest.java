/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo.esclient;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.rest.RestRequest;

/**
 * Implementation of {@link RestRequest} used for {@link SourceClientESClient}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class LocalRestRequest extends RestRequest {

	Map<String, String> params = new HashMap<String, String>();

	public LocalRestRequest(Map<String, String> params) {
		if (params != null)
			this.params = params;
	}

	@Override
	public Method method() {
		return Method.GET;
	}

	@Override
	public String uri() {
		return "";
	}

	@Override
	public String rawPath() {
		return "";
	}

	@Override
	public boolean hasContent() {
		return false;
	}

	@Override
	public boolean contentUnsafe() {
		return false;
	}

	@Override
	public BytesReference content() {
		return null;
	}

	@Override
	public String header(String name) {
		return "";
	}

	@Override
	public boolean hasParam(String key) {
		return params.containsKey(key);
	}

	@Override
	public String param(String key) {
		return params.get(key);
	}

	@Override
	public Map<String, String> params() {
		return params;
	}

	@Override
	public String param(String key, String defaultValue) {
		if (hasParam(key))
			return param(key);
		return defaultValue;
	}

	/**
	 * Add parameter to the request.
	 * 
	 * @param key of parameter
	 * @param value of parameter
	 * @return self for call chaining.
	 */
	public LocalRestRequest addParam(String key, String value) {
		params.put(key, value);
		return this;
	}

	@Override
	public String toString() {
		return "LocalRestRequest [params=" + params + "]";
	}

	@Override
	public Iterable<Entry<String, String>> headers() {
		return null;
	}

}

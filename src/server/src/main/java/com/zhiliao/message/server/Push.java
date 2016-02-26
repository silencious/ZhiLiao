package com.zhiliao.message.server;

import com.zhiliao.server.model.rest.Resource;

public class Push extends ServerMessage {
	public Push(Resource resource) {
		super();
		this.resource = resource;
	}
	
	public Push() {
		super();
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	private Resource resource;

	@Override
	public String toString() {
		return "Push{" +
				"resource=" + resource +
				'}';
	}
}

package com.zhiliao.message.server;

import com.zhiliao.server.model.rest.Resource;

public class GetResponse extends Response {
	@Override
	public String toString() {
		return "GetResponse [resource=" + resource + "]";
	}

	public GetResponse() {
		super();
	}

	public GetResponse(long mark, Resource resource) {
		super(mark);
		this.resource = resource;
	}

	private Resource resource;

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

}

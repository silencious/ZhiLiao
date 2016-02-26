package com.zhiliao.message.client;

import com.zhiliao.server.model.rest.Resource;

public class ListRequest extends Request {
	@Override
	public String toString() {
		return "ListRequest [resource=" + resource + "]";
	}

	public ListRequest(long mark, Resource resource) {
		super(mark);
		this.resource = resource;
	}

	public ListRequest() {
		super();
	}

	private Resource resource;

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
}

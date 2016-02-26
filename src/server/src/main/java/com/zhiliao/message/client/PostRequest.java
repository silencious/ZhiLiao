package com.zhiliao.message.client;

import com.zhiliao.server.model.rest.Resource;

public class PostRequest extends Request {
	@Override
	public String toString() {
		return "PostRequest{" +
				"resource=" + resource +
				"} " + super.toString();
	}

	public PostRequest() {
		super();
	}
	public PostRequest(long mark, Resource resource) {
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

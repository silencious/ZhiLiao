package com.zhiliao.message.client;

import com.zhiliao.server.model.rest.Resource;

public class DeleteRequest extends Request {
	public DeleteRequest() {
		super();
	}

	public DeleteRequest(long mark, Resource resource) {
		super(mark);
		this.resource = resource;
	}

	//note: only the id field should be filled
	private Resource resource;

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
}

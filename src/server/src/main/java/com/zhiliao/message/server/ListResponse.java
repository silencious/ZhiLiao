package com.zhiliao.message.server;

import com.zhiliao.server.model.rest.Resource;

import java.util.List;

public class ListResponse extends Response {
	@Override
	public String toString() {
		return "ListResponse [list=" + list + "]";
	}

	public ListResponse() {
		super();
	}

	public ListResponse(long mark, List<Resource> list) {
		super(mark);
		this.list = list;
	}

	private List<Resource> list;

	public List<Resource> getList() {
		return list;
	}

	public void setList(List<Resource> list) {
		this.list = list;
	}
}

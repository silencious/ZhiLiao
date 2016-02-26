package com.zhiliao.message.server;


public class PostResponse extends Response {
	public PostResponse() {
		super();
	}

	public PostResponse(long mark, long id) {
		super(mark);
		this.id = id;
	}

	@Override
	public String toString() {
		return "PostResponse [id=" + id + "]";
	}

	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}

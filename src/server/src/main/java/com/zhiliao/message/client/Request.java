package com.zhiliao.message.client;


public class Request extends ClientMessage {
	public Request(long mark) {
		super();
		this.mark = mark;
	}

	public Request() {
		super();
	}

	protected long mark;

	public long getMark() {
		return mark;
	}

	public void setMark(long mark) {
		this.mark = mark;
	}

	@Override
	public String toString() {
		return "Request{" +
				"mark=" + mark +
				"} " + super.toString();
	}
}

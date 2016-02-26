package com.zhiliao.message.server;


public class Response extends ServerMessage {
	public Response() {
		super();
	}

	public Response(long mark) {
		super();
		this.mark = mark;
	}

	protected long mark;

	public long getMark() {
		return mark;
	}

	@Override
	public String toString() {
		return "Response{" +
				"mark=" + mark +
				"} " + super.toString();
	}

	public void setMark(long mark) {
		this.mark = mark;
	}


}

package com.zhiliao.message.client;



public class Subscribe extends ClientMessage {
	@Override
	public String toString() {
		return "Subscribe [branch=" + branch + "]";
	}

	public long getBranch() {
		return branch;
	}

	public void setBranch(long branch) {
		this.branch = branch;
	}

	private long branch;
}

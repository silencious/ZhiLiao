package com.zhiliao.message.client;

public class Unsubscribe extends ClientMessage {
	
	public long getBranch() {
		return branch;
	}

	public void setBranch(long branch) {
		this.branch = branch;
	}

	private long branch;
}

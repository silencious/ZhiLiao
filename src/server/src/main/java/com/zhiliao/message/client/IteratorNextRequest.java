package com.zhiliao.message.client;


public class IteratorNextRequest extends IteratorRequest {
	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	//count of items to fetch
	private long count = 8;

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	@Override
	public String toString() {
		return "IteratorNextRequest{" +
				"count=" + count +
				", skip=" + skip +
				"} " + super.toString();
	}

	//skip or not?
	//if skip, a {count} number of items is skipped rather than sent back
	private boolean skip = false;

	public IteratorNextRequest(long mark, long id, long count, boolean skip) {
		super(mark, id);
		this.count = count;
		this.skip = skip;
	}

	public IteratorNextRequest() {
	}
}

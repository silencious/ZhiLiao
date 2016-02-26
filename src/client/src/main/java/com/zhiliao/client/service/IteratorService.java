package com.zhiliao.client.service;

import java.util.HashMap;

public class IteratorService {
	// if a topic's iterator is active
	private volatile HashMap<Long, Boolean> iterators = new HashMap<Long, Boolean>();

	public boolean contains(Long id) {
		return iterators.containsKey(id);
	}

	public boolean isActive(Long id) {
		if (id != null && iterators.containsKey(id)) {
			return iterators.get(id);
		}
		return false;
	}

	public void init(Long id) {
		iterators.put(id, true);
	}

	public void end(Long id) {
		iterators.put(id, false);
	}

	public void reset(Long id) {
		iterators.remove(id);
	}

	// tell iterators that the connection has broken
	public void disconnect() {

	}

	public void clear() {
		iterators.clear();
	}
}

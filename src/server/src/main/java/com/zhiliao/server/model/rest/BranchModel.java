package com.zhiliao.server.model.rest;


import java.util.Set;

public class BranchModel extends Resource {

	private Long head;
	private Long founder;

	@Override
	public String toString() {
		return "BranchModel{" +
				"head=" + head +
				", founder=" + founder +
				", tags=" + tags +
				"} " + super.toString();
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	private Set<String> tags;

	public Long getHead() {
		return head;
	}

	public void setHead(Long head) {
		this.head = head;
	}

	public Long getFounder() {
		return founder;
	}

	public void setFounder(Long founder) {
		this.founder = founder;
	}
}

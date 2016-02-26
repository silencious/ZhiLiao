package com.zhiliao.server.model.rest;

public class ForwardModel extends CommitModel {
	public Long getOriginBranch() {
		return originBranch;
	}
	public void setOriginBranch(Long originBranch) {
		this.originBranch = originBranch;
	}
	public Long getRefered() {
		return refered;
	}
	public void setRefered(Long refered) {
		this.refered = refered;
	}

	// id of the commit refered
	private Long refered;

	@Override
	public String toString() {
		return "ForwardModel{" +
				"refered=" + refered +
				", originBranch=" + originBranch +
				"} " + super.toString();
	}

	// id of the origin branch from which the commit is forwarded
	private Long originBranch;
}

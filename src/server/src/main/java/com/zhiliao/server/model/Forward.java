package com.zhiliao.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Forward extends Commit {
	public Branch getOriginBranch() {
		return originBranch;
	}

	public void setOriginBranch(Branch originBranch) {
		this.originBranch = originBranch;
	}

	@ManyToOne
	@JoinColumn(name = "refered_id", insertable = false, updatable = false)
	private Commit refered;

	@Column(name = "refered_id")
	public Long refered_id;
	
	@ManyToOne
	@JoinColumn(name = "originBranch_id", insertable = false, updatable = false)
	private Branch originBranch;

	@Column(name = "originBranch_id")
	public Long originBranch_id;

	public Commit getRefered() {
		return refered;
	}

	public void setRefered(Commit refered) {
		this.refered = refered;
	}

}

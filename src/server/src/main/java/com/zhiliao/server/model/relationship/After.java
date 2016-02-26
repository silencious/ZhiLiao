package com.zhiliao.server.model.relationship;

import javax.persistence.*;

import com.zhiliao.server.model.Commit;

@Entity
public class After {
	@Override
	public String toString() {
		return "After [id=" + id + ", parent=" + parent
				+ ", bound=" + bound + "]";
	}

	public Commit getChild() {
		return child;
	}

	public void setChild(Commit child) {
		this.child = child;
	}

	public Commit getParent() {
		return parent;
	}

	public void setParent(Commit parent) {
		this.parent = parent;
	}

	public long getBound() {
		return bound;
	}

	public void setBound(long bound) {
		this.bound = bound;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne()
	@JoinColumn(name = "child_id", nullable = false)
	private Commit child;
	
	@ManyToOne
	@JoinColumn(name = "parent_id", nullable = false)
	private Commit parent;
	
	private long bound;

	public After(Commit child, Commit parent, long bound) {
		super();
		this.child = child;
		this.parent = parent;
		this.bound = bound;
	}
	
	public After() {
		
	}
}

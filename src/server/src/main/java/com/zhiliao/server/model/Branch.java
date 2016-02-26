package com.zhiliao.server.model;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Branch {
	@Override
	public String toString() {
		return "Branch [id=" + id + ", head=" + head + "]";
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Commit getHead() {
		return head;
	}

	public void setHead(Commit head) {
		this.head = head;
	}
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne()
	@JoinColumn(name = "head_id", insertable = false, updatable = false)
	private Commit head;

	@Column(name = "head_id")
	public Long head_id;
	
	@ManyToOne
	@JoinColumn(name = "founder_id", insertable = false, updatable = false)
	private User founder;

	@Column(name = "founder_id")
	public Long founder_id;

	@ElementCollection
	private Set<String> tags;

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public User getFounder() {
		return founder;
	}
	public void setFounder(User founder) {
		this.founder = founder;
	}

}

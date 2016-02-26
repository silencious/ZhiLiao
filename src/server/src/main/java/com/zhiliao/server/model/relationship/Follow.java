package com.zhiliao.server.model.relationship;

import com.zhiliao.server.model.Branch;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followed_id"}))
public class Follow {
	public Branch getFollower() {
		return follower;
	}

	public void setFollower(Branch follower) {
		this.follower = follower;
	}

	public Branch getFollowed() {
		return followed;
	}

	public void setFollowed(Branch followed) {
		this.followed = followed;
	}

	@Override
	public String toString() {
		return "Follow [id=" + id + ", follower=" + follower + ", followed="
				+ followed + "]";
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
	
	@ManyToOne
	@JoinColumn(name = "follower_id", insertable = false, updatable = false)
	private Branch follower;
	
	@ManyToOne
	@JoinColumn(name = "followed_id", insertable = false, updatable = false)
	private Branch followed;
	
	private long bound = 0;

	public long getBound() {
		return bound;
	}

	public void setBound(long bound) {
		this.bound = bound;
	}

	@Column(name = "follower_id")
	public Long follower_id;

	@Column(name = "followed_id")
	public Long followed_id;
}

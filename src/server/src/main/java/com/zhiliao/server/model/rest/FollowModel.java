package com.zhiliao.server.model.rest;

public class FollowModel extends Resource {
	@Override
	public String toString() {
		return "FollowModel [follower=" + follower + ", followed=" + followed
				+ "]";
	}
	public Long getFollowed() {
		return followed;
	}
	public void setFollowed(Long followed) {
		this.followed = followed;
	}
	
	private Long follower;

	public Long getFollower() {
		return follower;
	}
	public void setFollower(Long follower) {
		this.follower = follower;
	}

	private Long followed;
}

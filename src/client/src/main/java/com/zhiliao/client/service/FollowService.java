package com.zhiliao.client.service;

import java.util.ArrayList;

import com.zhiliao.server.model.rest.FollowModel;
import com.zhiliao.server.model.rest.UserModel;

public class FollowService {
	// user's followers
	private ArrayList<UserModel> followers = new ArrayList<UserModel>();
	// user's followings
	private ArrayList<UserModel> followings = new ArrayList<UserModel>();

	private Long followerId = null;
	private ArrayList<FollowModel> followTopics = new ArrayList<FollowModel>();

	public ArrayList<UserModel> getMyFollowers() {
		return followers;
	}

	public void setMyfollowers(ArrayList<UserModel> followers) {
		this.followers = followers;
	}

	public ArrayList<UserModel> getMyFollowings() {
		return followings;
	}

	public void setMyfollowings(ArrayList<UserModel> followings) {
		this.followings = followings;
	}

	public ArrayList<FollowModel> getFollowTopics(Long topicId) {
		if (followerId == topicId) {
			return followTopics;
		}
		return null;
	}

	public void addFollowTopics(Long topicId, ArrayList<FollowModel> topics) {
		if (followerId.equals(topicId)) {
			followTopics.addAll(topics);
		} else {
			followerId = topicId;
			followTopics = topics;
		}
	}
}

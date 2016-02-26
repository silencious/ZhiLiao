package com.zhiliao.server.service;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.User;
import com.zhiliao.server.model.relationship.Follow;
import com.zhiliao.server.exception.ErrorResponseException;

import java.util.Iterator;

public interface FollowService {
	public long addFollow(Follow follow, User user) throws ErrorResponseException;
	public Follow getFollow(long id) throws ErrorResponseException;
	public void deleteFollow(long id, User user) throws ErrorResponseException;
	public Iterator<Follow> findByFollow(Long follower, Long followed);
}

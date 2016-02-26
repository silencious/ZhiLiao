package com.zhiliao.server.websocket.rest;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.exception.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.User;
import com.zhiliao.server.model.relationship.Follow;
import com.zhiliao.server.model.rest.FollowModel;
import com.zhiliao.server.service.FollowService;

@Controller
@RequestMapping(FollowModel.class)
public class FollowRequestController {
	@Autowired
	private FollowService followService;

	public long post(FollowModel model, User user) throws ErrorResponseException {
		Long id = followService.addFollow(convert(model), user);
		return id;
	}
	
	public void delete(Long id, User user) throws ErrorResponseException {
		if (id == null)
			throw new ErrorResponseException(ErrorCode.BadRequest);
		
		followService.deleteFollow(id, user);
	}
	
	public FollowModel get(Long id, User user) throws ErrorResponseException {
		if (id == null)
			throw new ErrorResponseException(ErrorCode.BadRequest);
		
		Follow follow = followService.getFollow(id);
		
		return convert(follow);
	}
	
	public static FollowModel convert(Follow follow) {
		FollowModel model = new FollowModel();
		model.setId(follow.getId());
		model.setFollower(follow.follower_id);
		model.setFollowed(follow.followed_id);
		return model;
	}
	
	public static Follow convert(FollowModel model) {
		Follow follow = new Follow();
		follow.followed_id = model.getFollowed();
		follow.follower_id = model.getFollower();
		return follow;
	}
}

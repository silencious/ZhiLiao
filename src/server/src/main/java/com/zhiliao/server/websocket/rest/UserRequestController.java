package com.zhiliao.server.websocket.rest;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.exception.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.zhiliao.server.model.User;
import com.zhiliao.server.model.rest.UserModel;
import com.zhiliao.server.service.UserService;

import javax.annotation.Nullable;

@Controller
@RequestMapping(UserModel.class)
public class UserRequestController {
	@Autowired
	private UserService userService;
	
	public long post(UserModel model) throws ErrorResponseException {
		return userService.newUser(convert(model)).getId();
	}
	
	public UserModel get(@Nullable Long id, User user) throws ErrorResponseException {
		User user2;
		if (id == null)
			user2 = user;
		else
			user2 = userService.getUser(id);
		
		if (user2 == null)
			throw new ErrorResponseException(ErrorCode.NotFound);
		
		UserModel model = convert(user2);
		if (user2.getId() != user.getId()) {
			model.setPassword(null);
			model.setPrivateInfo(null);
		}
		return model;
	}
	
	public static User convert(UserModel model) {
		User user = new User();
		user.setId(model.getId());
		user.setUsername(model.getUsername());
		user.setPassword(model.getPassword());
		user.setPublicInfo(model.getPublicInfo());
		user.setPrivateInfo(model.getPrivateInfo());
		return user;
	}
	
	public static UserModel convert(User user) {
		UserModel model = new UserModel();
		model.setId(user.getId());
		model.setUsername(user.getUsername());
		model.setPassword(user.getPassword());
		model.setPublicInfo(user.getPublicInfo());
		model.setPrivateInfo(user.getPrivateInfo());
		return model;
	}
	

}

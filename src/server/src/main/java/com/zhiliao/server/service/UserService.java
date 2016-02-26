package com.zhiliao.server.service;

import com.zhiliao.server.model.User;
import com.zhiliao.server.exception.ErrorResponseException;

public interface UserService {
	User newUser(User user) throws ErrorResponseException;
	User getUser(long id);
	User getUserByUsername(String username);
}

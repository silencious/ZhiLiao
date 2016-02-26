package com.zhiliao.server.service;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zhiliao.server.model.User;
import com.zhiliao.server.exception.ErrorResponseException;

@Service
public class UserServiceImpl implements UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository repository;
	
	@Override
	public User newUser(User user) throws ErrorResponseException {
		try {
			return repository.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new ErrorResponseException(ErrorCode.Conflict);
		}
	}
	
	@Override
	public User getUserByUsername(String username) {
		User user = repository.findByUsername(username);
		return user;
	}

	@Override
	public User getUser(long id) {
		User user = repository.findOne(id);
		return user;
	}
}

package com.zhiliao.client.service;

import java.util.TreeMap;

import com.zhiliao.server.model.rest.UserModel;

public class UserService {
	// map userid to UserEntity
	private TreeMap<Long, UserModel> userIdMap = new TreeMap<Long, UserModel>();
	// map username to UserEntity
	private TreeMap<String, UserModel> userNameMap = new TreeMap<String, UserModel>();
	
	public UserModel get(Long id){
		if(userIdMap.containsKey(id)){
			return userIdMap.get(id);
		}else{
			return null;
		}
	}
	public UserModel get(String name){
		if(userNameMap.containsKey(name)){
			return userNameMap.get(name);
		}else{
			return null;
		}
	}
	public void add(UserModel user){
		userIdMap.put(user.getId(), user);
		userNameMap.put(user.getUsername(), user);
	}
}

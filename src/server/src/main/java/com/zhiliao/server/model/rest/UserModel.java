package com.zhiliao.server.model.rest;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("User")
public class UserModel extends Resource {
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}


	@Override
	public String toString() {
		return "UserModel{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				", publicInfo='" + publicInfo + '\'' +
				", privateInfo='" + privateInfo + '\'' +
				"} " + super.toString();
	}

	private String username;
	
	private String password;


	private String publicInfo;

	public String getPublicInfo() {
		return publicInfo;
	}

	public void setPublicInfo(String publicInfo) {
		this.publicInfo = publicInfo;
	}

	public String getPrivateInfo() {

		return privateInfo;
	}

	public void setPrivateInfo(String privateInfo) {
		this.privateInfo = privateInfo;
	}

	private String privateInfo;
}

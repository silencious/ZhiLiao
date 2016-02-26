package com.zhiliao.client.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiliao.server.model.rest.UserModel;

public class UserInfo {
	private static ObjectMapper mapper = new ObjectMapper();

	@JsonTypeName("PublicInfo")
	@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public static class PublicInfo {
		@JsonIgnore
		private String username;
		private Long age;
		private String gender;

		/*
		 * this is the branch to be followed, contains all public commit
		 * authored by this user
		 */
		private Long followedBranch;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public Long getAge() {
			return age;
		}

		public void setAge(Long age) {
			this.age = age;
		}

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public Long getFollowedBranch() {
			return followedBranch;
		}

		public void setFollowedBranch(Long followedBranch) {
			this.followedBranch = followedBranch;
		}
	};

	@JsonTypeName("PrivateInfo")
	@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public static class PrivateInfo {
		@JsonIgnore
		private Long id;
		@JsonIgnore
		private String password;
		private String email;

		// sessions to be shown on home screen
		private ArrayList<Long> sessions;

		/*
		 * this is the branch used to follow other users' followedBranch, so
		 * this user can know what 's his friends doing.
		 */
		private Long followerBranch;

		public Long getId() {
			return id;
		}

		private void setId(Long id) {
			this.id = id;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public ArrayList<Long> getSessions() {
			return sessions;
		}

		public void setSessions(ArrayList<Long> sessions) {
			this.sessions = sessions;
		}

		public Long getFollowerBranch() {
			return followerBranch;
		}

		public void setFollowerBranch(Long followerBranch) {
			this.followerBranch = followerBranch;
		}
	}

	private PublicInfo publicInfo;
	private PrivateInfo privateInfo;

	public PublicInfo getPublicInfo() {
		return publicInfo;
	}

	public void setPublicInfo(PublicInfo publicInfo) {
		this.publicInfo = publicInfo;
	}

	public PrivateInfo getPrivateInfo() {
		return privateInfo;
	}

	public void setPrivateInfo(PrivateInfo privateInfo) {
		this.privateInfo = privateInfo;
	}

	public UserInfo() {
		publicInfo = new PublicInfo();
		privateInfo = new PrivateInfo();
	}

	public UserInfo(UserModel user) {
		try {
			if (user.getPublicInfo() == null) {
				publicInfo = new PublicInfo();
			} else {
				System.out.println("Deserialize PublicInfo: "
						+ user.getPublicInfo());
				publicInfo = mapper.readValue(user.getPublicInfo(),
						PublicInfo.class);
			}
			if (user.getPrivateInfo() == null) {
				privateInfo = new PrivateInfo();
			} else {
				System.out.println("Deserialize PrivateInfo: "
						+ user.getPrivateInfo());
				privateInfo = mapper.readValue(user.getPrivateInfo(),
						PrivateInfo.class);
			}
			publicInfo.setUsername(user.getUsername());
			privateInfo.setId(user.getId());
			privateInfo.setPassword(user.getPassword());
		} catch (IOException e) {
			System.out.println("Error parsing userinfo");
			e.printStackTrace();
		}
	}

	public UserModel toUserModel() {
		UserModel user = new UserModel();
		user.setId(privateInfo.getId());
		user.setUsername(publicInfo.getUsername());
		user.setPassword(privateInfo.getPassword());
		try {
			user.setPrivateInfo(mapper.writeValueAsString(privateInfo));
			user.setPublicInfo(mapper.writeValueAsString(publicInfo));
		} catch (JsonProcessingException e) {
			System.out.println("Mapping userinfo error");
			e.printStackTrace();
		}
		return user;
	}

	public void saveSessions(List<TopicEntity> topics) {
		System.out.println("Save session of " + topics.size() + " topics");
		ArrayList<Long> sessions = new ArrayList<Long>();
		for (TopicEntity topic : topics) {
			if (!topic.isIgnore()) {
				sessions.add(topic.getTopicId());
			}
		}
		privateInfo.setSessions(sessions);
	}

}

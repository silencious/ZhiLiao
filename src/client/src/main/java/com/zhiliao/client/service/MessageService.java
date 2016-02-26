package com.zhiliao.client.service;

import java.util.LinkedList;
import java.util.TreeMap;

import com.zhiliao.client.entity.MessageEntity;
import com.zhiliao.client.entity.TopicEntity;
import com.zhiliao.client.util.Constant;

public class MessageService {
	// save all messages
	private TreeMap<Long, MessageEntity> messages = new TreeMap<Long, MessageEntity>();
	// local history of topics
	private LinkedList<TopicEntity> sessionTopics = new LinkedList<TopicEntity>();
	// discover topics, recommended by server or search result
	private LinkedList<TopicEntity> discoverTopics = new LinkedList<TopicEntity>();
	// user's followerTopic
	private TopicEntity followedTopic = null;
	// timeline topics
	private LinkedList<TopicEntity> timelineTopics = new LinkedList<TopicEntity>();
	// user's followerTopic
	private TopicEntity followerTopic = null;
	// trend topics, i.e. friends' commits
	private LinkedList<TopicEntity> trendTopics = new LinkedList<TopicEntity>();

	public MessageEntity getMessage(Long id) {
		if (id == null) {
			return null;
		}
		return messages.get(id);
	}

	public void addMessage(MessageEntity message) {
		if (message == null || message.getId() == null
				|| message.getTopicId() == null || message.getAuthor() == null) {
			return;
		}
		messages.put(message.getId(), message);

		Long topicId = message.getTopicId();
		TopicEntity topic = getTopic(topicId);
		if (topic == null) {
			System.out
					.println("Add a message whose topic doesn't exist locally");
			topic = new TopicEntity();
			sessionTopics.add(topic);
		}
		topic.addMessage(message);
		if (sessionTopics.contains(topic) && !topic.isEmpty()) {
			MessageEntity m1 = topic.getLatest();
			MessageEntity m2 = sessionTopics.peekFirst()
					.getLatest();
			if(m1 != null && m2 != null){
				if (m1.getDate() > m2.getDate()) {
					topTopic(topic, Constant.SESSION);
				}
			}
		}
		if (timelineTopics.contains(topic) && !topic.isEmpty()) {
			MessageEntity m1 = topic.getLatest();
			MessageEntity m2 = timelineTopics.peekFirst()
					.getLatest();
			if(m1 != null && m2 != null){
				if (m1.getDate() > m2.getDate()) {
					topTopic(topic, Constant.SESSION);
				}
			}
		}
		if (trendTopics.contains(topic) && !topic.isEmpty()) {
			MessageEntity m1 = topic.getLatest();
			MessageEntity m2 = trendTopics.peekFirst()
					.getLatest();
			if(m1 != null && m2 != null){
				if (m1.getDate() > m2.getDate()) {
					topTopic(topic, Constant.SESSION);
				}
			}
		}
	}

	public boolean exist(Long topicId, int type) {
		switch (type) {
		case Constant.SESSION:
			for (TopicEntity topic : sessionTopics) {
				if (topicId.equals(topic.getTopicId())) {
					return true;
				}
			}
			break;
		case Constant.DISCOVER:
			for (TopicEntity topic : discoverTopics) {
				if (topicId.equals(topic.getTopicId())) {
					return true;
				}
			}
			break;
		case Constant.TIMELINE:
			for (TopicEntity topic : timelineTopics) {
				if (topicId.equals(topic.getTopicId())) {
					return true;
				}
			}
			break;
		case Constant.TREND:
			for (TopicEntity topic : trendTopics) {
				if (topicId.equals(topic.getTopicId())) {
					return true;
				}
			}
			break;
		}
		return false;
	}

	public TopicEntity getTopic(Long topicId) {
		if (topicId == null) {
			System.out.println("Trying to get a topic with null id");
			return null;
		}
		for (TopicEntity topic : sessionTopics) {
			if (topicId.equals(topic.getTopicId())) {
				return topic;
			}
		}
		for (TopicEntity topic : discoverTopics) {
			if (topicId.equals(topic.getTopicId())) {
				return topic;
			}
		}
		for (TopicEntity topic : timelineTopics) {
			if (topicId.equals(topic.getTopicId())) {
				return topic;
			}
		}
		for (TopicEntity topic : trendTopics) {
			if (topicId.equals(topic.getTopicId())) {
				return topic;
			}
		}
		if (followedTopic != null && followedTopic.getTopicId().equals(topicId)) {
			return followedTopic;
		}
		if (followerTopic != null && followerTopic.getTopicId().equals(topicId)) {
			return followerTopic;
		}

		System.out.println("Topic not exist locally");
		return null;
	}

	public LinkedList<TopicEntity> getTopics(int type) {
		switch (type) {
		case Constant.SESSION:
			return sessionTopics;
		case Constant.DISCOVER:
			return discoverTopics;
		case Constant.TIMELINE:
			return timelineTopics;
		case Constant.TREND:
			return trendTopics;
		}
		return null;
	}

	// add a topic
	public void addTopic(TopicEntity topic, int type) {
		if (topic == null || topic.getTopicId() == null) {
			return;
		}
		if (getTopics(type).contains(topic)) {
			return;
		}
		getTopics(type).add(topic);

		if (type == Constant.TREND && !topic.isEmpty()) {
			for (int i = 0; i < trendTopics.size(); i++) {
				if (trendTopics.get(i).getLatest().getDate() < topic
						.getLatest().getDate()) {
					trendTopics.removeLastOccurrence(topic);
					trendTopics.add(i, topic);
					break;
				}
			}
		}
	}

	public void topTopic(TopicEntity topic, int type) {
		if (topic == null || topic.getTopicId() == null) {
			return;
		}
		System.out.println("Top topic id=" + topic.getTopicId() + " type="
				+ type);
		LinkedList<TopicEntity> list = getTopics(type);
		if (list.contains(topic)) {
			list.remove(topic);
		}
		list.addFirst(topic);
	}

	public void setTopic(LinkedList<TopicEntity> list, int type) {
		switch (type) {
		case Constant.SESSION:
			sessionTopics = list;
		case Constant.DISCOVER:
			discoverTopics = list;
		case Constant.TIMELINE:
			timelineTopics = list;
		case Constant.TREND:
			trendTopics = list;
		}
	}

	public void clearTopics() {
		sessionTopics.clear();
		discoverTopics.clear();
		timelineTopics.clear();
		trendTopics.clear();
	}

	public TopicEntity getFollowedTopic() {
		return followedTopic;
	}

	public void setFollowedTopic(TopicEntity followedTopic) {
		this.followedTopic = followedTopic;
	}

	public TopicEntity getFollowerTopic() {
		return followerTopic;
	}

	public void setFollowerTopic(TopicEntity followerTopic) {
		this.followerTopic = followerTopic;
	}
}

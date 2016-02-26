package com.zhiliao.client.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class TopicEntity {
	private Long topicId;
	private Long founder;
	private boolean ignore = false;
	private TreeMap<Long, MessageEntity> messages = new TreeMap<Long, MessageEntity>();
	private Set<String> Tags = new HashSet<String>();

	public ArrayList<MessageEntity> getMsgColl() {
		ArrayList<MessageEntity> coll = new ArrayList<MessageEntity>();
		synchronized (messages) {
			if (!messages.isEmpty()) {
				coll.addAll(messages.values());
			}
		}
		// coll.addAll(unconfirmed.values());
		return coll;
	}

	public TreeMap<Long, MessageEntity> getMsgs() {
		return messages;
	}

	public void setMsgs(TreeMap<Long, MessageEntity> messages) {
		this.messages = messages;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public Long getFounder() {
		return founder;
	}

	public void setFounder(Long founder) {
		this.founder = founder;
	}

	public boolean isEmpty() {
		return messages.isEmpty();
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public MessageEntity getMessage(Long id) {
		return messages.get(id);
	}

	public MessageEntity getLatest() {
		if (messages.isEmpty()) {
			return null;
		}
		return messages.lastEntry().getValue();
	}

	public MessageEntity getLatest(Long author) {
		NavigableMap<Long, MessageEntity> reverse = messages.descendingMap();
		for (Entry<Long, MessageEntity> entry : reverse.entrySet()) {
			if (entry.getValue().getAuthor() == author) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void addMessage(MessageEntity message) {
		if (message != null && message.getId() != null) {
			messages.put(message.getId(), message);
		}
	}

	public void addAll(TopicEntity topic) {
		messages.putAll(topic.getMsgs());
	}

	public String getPreview() {
		// give a summary of a topic, including its latest updates
		// a naive version, not completed yet
		int n = messages.size();
		if (n == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		/*
		 * if (n > 1) {
		 * sb.append(messages.lowerEntry(messages.lastKey()).getValue()
		 * .getPreview()); sb.append("\n"); }
		 */
		sb.append(messages.lastEntry().getValue().getPreview());
		return sb.toString();
	}

	public Set<String> getTags() {
		return Tags;
	}

	public void setTags(Set<String> tags) {
		Tags = tags;
	}
}

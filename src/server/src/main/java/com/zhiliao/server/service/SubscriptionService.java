package com.zhiliao.server.service;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

@Service
public class SubscriptionService {
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

	private SetMultimap<Long, Object> subscribers = Multimaps.synchronizedSetMultimap(HashMultimap.create());
	
	public void Subscribe(long topicId, Object session) {
		logger.debug("subscribe {} to {}", session, topicId);
		subscribers.put(topicId, session);
	}
	
	public void unsubscribe(long topicId, Object session) {
		logger.debug("unsubscribe {} from {}", session, topicId);
		subscribers.remove(topicId, session);
	}
	
	public Set<Object> list(long topicId) {
		return subscribers.get(topicId);
	}
}

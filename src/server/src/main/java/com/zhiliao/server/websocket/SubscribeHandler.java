package com.zhiliao.server.websocket;

import com.zhiliao.message.client.Unsubscribe;
import com.zhiliao.server.websocket.annotation.MessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhiliao.message.client.Subscribe;
import com.zhiliao.server.service.SubscriptionService;

@Service
public class SubscribeHandler   {
	private static final Logger logger = LoggerFactory.getLogger(SubscribeHandler.class);
	@Autowired
	SubscriptionService subscriptionService;

	@MessageMapping(Subscribe.class)
	public void subscribe(
			MessageSocket socket,
			Subscribe subscribe) {
		subscriptionService.Subscribe(subscribe.getBranch(), socket);
	}

	@MessageMapping(Unsubscribe.class)
	public void unsubscribe(
			MessageSocket socket,
			Unsubscribe unsubscribe ) {
		subscriptionService.unsubscribe(unsubscribe.getBranch(), socket);
	}

}

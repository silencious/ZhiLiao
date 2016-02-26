package com.zhiliao.server.websocket;

import java.io.IOException;
import java.util.Map;

import javax.websocket.ClientEndpointConfig.Configurator;

import org.junit.Assert;

import com.zhiliao.client.websocket.BasicAuthConfigurator;
import com.zhiliao.client.websocket.MessageApi;
import com.zhiliao.message.server.ServerMessage;

public class BasicTest {
	protected MessageApi api = new MessageApi();
	protected long branch = 4l;
	
	public BasicTest() {
		Map handlers = api.getOnMessageHandlers();
		handlers.put(ServerMessage.class, new MessageApi.OnMessageHandler<ServerMessage>() {
			@Override
			public void handle(MessageApi api, ServerMessage message) {
				System.out.println(message);
			}
		});
		Configurator config = new BasicAuthConfigurator("guest", "guest");
		try {
			api.connect(config);
			//api.connect(null);
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}

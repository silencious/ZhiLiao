package com.zhiliao.client.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiliao.message.client.ClientMessage;

public class MessageEncoder implements Encoder.Text<ClientMessage> {
	private static ObjectMapper mapper = new ObjectMapper();

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public String encode(ClientMessage object) throws EncodeException {
		System.out.println(object);
		try {
			String _object = mapper.writeValueAsString(object);
			return _object;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new EncodeException(object, e.getMessage());
		}
	}
	
}
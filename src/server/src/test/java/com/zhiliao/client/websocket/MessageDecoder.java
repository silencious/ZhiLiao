package com.zhiliao.client.websocket;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiliao.message.server.ServerMessage;

public class MessageDecoder implements Decoder.Text<ServerMessage> {
	private static ObjectMapper mapper = new ObjectMapper();

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public ServerMessage decode(String s) throws DecodeException {
		System.out.println(s);
		try {
			return mapper.readValue(s, ServerMessage.class);
		} catch (IOException e) {
			throw new DecodeException(s, e.getMessage());
		}
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

	
}
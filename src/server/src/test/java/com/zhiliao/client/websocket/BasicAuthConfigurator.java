package com.zhiliao.client.websocket;

import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;


public class BasicAuthConfigurator extends ClientEndpointConfig.Configurator {
	private String username;
	private String password;
	
	public BasicAuthConfigurator(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void beforeRequest(Map<String, List<String>> headers) {
		String str = username + ":" + password;
		List<String> list = headers.get("Authorization");
		if (list == null) {
			list = new ArrayList<String>();
			headers.put("Authorization", list);
		}
		
		list.add("Basic " + new String(Base64.encodeBase64(str.getBytes())));
    }
}

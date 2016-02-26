package com.zhiliao.server.websocket.rest;

import com.zhiliao.client.websocket.rest.RestClient;
import com.zhiliao.server.websocket.BasicTest;

public class RestTest extends BasicTest {
	protected RestClient client = new RestClient(api);
}

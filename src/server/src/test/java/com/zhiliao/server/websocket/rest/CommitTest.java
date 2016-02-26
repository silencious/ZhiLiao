package com.zhiliao.server.websocket.rest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import com.zhiliao.message.client.Subscribe;
import com.zhiliao.server.model.rest.CommitModel;
import com.zhiliao.server.model.rest.ForwardModel;

public class CommitTest extends RestTest {
	@Test()
	public void post() throws InterruptedException, ExecutionException {
		Subscribe subscribe = new Subscribe();
		subscribe.setBranch(branch);
		api.send(subscribe);

		subscribe.setBranch(26);
		api.send(subscribe);

		CommitModel model = new CommitModel();
		model.setBranch(branch);
		model.setMsg("Hello World");
		
		Future<Long> future = client.post(model);
		future.get();
	}
	
	@Test
	public void post_forward() throws InterruptedException, ExecutionException  {
		ForwardModel model = new ForwardModel();
		model.setBranch(branch);
		model.setMsg("see this!");
		
		model.setOriginBranch(26l);
		model.setRefered(0l);
		
		Future<Long> future = client.post(model);
		future.get();
 	}
}

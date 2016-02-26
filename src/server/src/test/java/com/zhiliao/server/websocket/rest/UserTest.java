package com.zhiliao.server.websocket.rest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.zhiliao.client.websocket.rest.ErrorException;
import com.zhiliao.message.server.ErrorResponse;
import com.zhiliao.server.exception.ErrorCode;
import org.junit.Assert;
import org.junit.Test;

import com.zhiliao.server.model.rest.UserModel;

public class UserTest extends RestTest {
	private long userId = 0l;
	@Test(timeout=5000)
	public void get() throws  ExecutionException, InterruptedException {
		UserModel model = new UserModel();
		model.setId(null);
		Future<UserModel> fut = client.get(model);
		System.out.println(fut.get());
	}
	
	@Test(timeout=5000)
	public void post() throws InterruptedException, ExecutionException {
		UserModel model = new UserModel();
		model.setUsername("admin");
		model.setPassword("admin");

		Future<Long> fut = client.post(model);
		try {
			fut.get();
		} catch (ExecutionException e) {
			ErrorException errorException = (ErrorException) e.getCause();
			Assert.assertEquals(errorException.getError(), ErrorCode.Conflict);
		}
	}
}

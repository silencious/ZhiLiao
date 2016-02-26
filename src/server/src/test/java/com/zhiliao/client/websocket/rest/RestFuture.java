package com.zhiliao.client.websocket.rest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.zhiliao.server.exception.ErrorCode;

public class RestFuture<T> implements Future<T> {
	private CountDownLatch latch = new CountDownLatch(1);
	private T response;
	private ErrorCode error = null;
	
	public void set(T response) {
		this.response = response;
		latch.countDown();
	}
	
	public void set(ErrorCode error) {
		this.error = error;
		latch.countDown();
	}
	
	public T get() throws InterruptedException, ExecutionException {
		latch.await();
		if (error == null)
			return response;
		else
			throw new ExecutionException(new ErrorException(error));
	}
	
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
		latch.await(timeout, unit);
		if (error == null)
			return response;
		else 
			throw new ExecutionException(new ErrorException(error));
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}
}

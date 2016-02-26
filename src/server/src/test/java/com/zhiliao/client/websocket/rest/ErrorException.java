package com.zhiliao.client.websocket.rest;

import com.zhiliao.server.exception.ErrorCode;

public class ErrorException extends Exception {

	@Override
	public String toString() {
		return "ErrorException [error=" + error + "]";
	}

	private ErrorCode error;
	
	public ErrorCode getError() {
		return error;
	}

	public void setError(ErrorCode error) {
		this.error = error;
	}

	public ErrorException(ErrorCode error) {
		this.error = error;
	}
	
	
}

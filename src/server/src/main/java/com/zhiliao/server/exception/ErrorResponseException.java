package com.zhiliao.server.exception;

public class ErrorResponseException extends Exception {

	private ErrorCode error;
	private String reason;

	public void setError(ErrorCode error) {
		this.error = error;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public ErrorCode getError() {
		return error;
	}
	public ErrorResponseException(ErrorCode error) {
		this.error = error;
	}

	public ErrorResponseException(ErrorCode error, String reason) {
		this.error = error;
		this.reason = reason;
	}
}

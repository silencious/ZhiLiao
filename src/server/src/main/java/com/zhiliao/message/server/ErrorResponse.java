package com.zhiliao.message.server;


import com.zhiliao.server.exception.ErrorCode;

public class ErrorResponse extends Response {
	public ErrorResponse() {
		super();
	}

	public ErrorResponse(long mark, ErrorCode error) {
		super(mark);
		this.error = error;
	}

	@Override
	public String toString() {
		return "ErrorResponse{" +
				"error=" + error +
				"} " + super.toString();
	}

	public ErrorCode getError() {
		return error;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	private ErrorCode error;

	private String reason;

	public void setError(ErrorCode error) {
		this.error = error;
	}
}

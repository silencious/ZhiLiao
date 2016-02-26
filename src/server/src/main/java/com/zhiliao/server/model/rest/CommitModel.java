package com.zhiliao.server.model.rest;

public class CommitModel extends Resource {

	public Long getBranch() {
		return branch;
	}
	public void setBranch(Long branch) {
		this.branch = branch;
	}
	public Long getReplied() {
		return replied;
	}
	public void setReplied(Long replied) {
		this.replied = replied;
	}

	@Override
	public String toString() {
		return "CommitModel{" +
				"author=" + author +
				", date=" + date +
				", branch=" + branch +
				", msg='" + msg + '\'' +
				", replied=" + replied +
				'}';
	}

	public Long getAuthor() {
		return author;
	}
	public void setAuthor(Long author) {
		this.author = author;
	}
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

	// the id of the author of the message
	private Long author;
	
	// milliseconds since 1970-01-01
	private Long date;
	
	// id of the branch to which the message post
	private Long branch;
	
	// the message itself
	private String msg;
	
	// id of the commit to which this commit reply 
	private Long replied;
	
}

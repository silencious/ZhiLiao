package com.zhiliao.client.entity;

public class MessageEntity {
	private Long id;

	private Long topicId;
	
	private Long replied;

	private Long author;
	
	private String fromName;
	
	private String toName;

	private Long date;

	private String msg;

	public MessageEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public Long getReplied() {
		return replied;
	}

	public void setReplied(Long replied) {
		this.replied = replied;
	}

	public Long getAuthor() {
		return author;
	}

	public void setAuthor(Long author) {
		this.author = author;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String name) {
		this.fromName = name;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
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

	public String getPreview(){
		StringBuilder sb = new StringBuilder();
		sb.append(fromName);
		if(toName != null){
			sb.append("->");
			sb.append(toName);
		}
		sb.append(":");
		sb.append(msg.replaceAll("\\s+", " "));
		return sb.toString();
	}
}

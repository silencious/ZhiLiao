package com.zhiliao.server.model.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Resource {
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	protected Long id;

	@Override
	public String toString() {
		return "Resource{" +
				"id=" + id +
				'}';
	}
}

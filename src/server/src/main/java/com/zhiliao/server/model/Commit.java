package com.zhiliao.server.model;

import java.util.Calendar;
import java.util.Collection;

import com.zhiliao.server.model.relationship.After;

import javax.persistence.*;

@Entity
public class Commit implements Comparable<Commit> {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@Override
	public String toString() {
		return "Commit [id=" + id + ", author=" + author + ", date=" + date
				+ ", msg=" + msg + ", replied=" + replied + ", parents="
				+ parents + "]";
	}

	public Commit getReplied() {
		return replied;
	}

	public void setReplied(Commit replied) {
		this.replied = replied;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Collection<After> getParents() {
		return parents;
	}

	public void setParents(Collection<After> parents) {
		this.parents = parents;
	}

	@ManyToOne
	@JoinColumn(name = "author_id", insertable = false, updatable = false)
	private User author;

	@Column(name = "author_id")
	public Long author_id;
	
	private Calendar date = Calendar.getInstance();

	private String msg;
	
	@ManyToOne
	@JoinColumn(name = "replied_id", insertable = false, updatable = false)
	private Commit replied;

	@Column(name = "replied_id")
	public Long replied_id;
	
	@OneToMany(mappedBy = "child", cascade = {CascadeType.ALL})
	private Collection<After> parents;

	@Override
	public int compareTo(Commit commit) {
		Long res = getId() - commit.getId();
		if (res < 0)
			return -1;
		else if (res == 0)
			return 0;
		else
			return 1;
	}
}

package com.zhiliao.server.model;

import com.zhiliao.server.model.relationship.Prefer;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "`user`")
public class User {
	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password="
				+ password + "]";
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	
	@Column(unique = true)
	private String username;
	
	private String password;

	public Collection<Prefer> getPrefers() {
		return prefers;
	}

	public void setPrefers(Collection<Prefer> prefers) {
		this.prefers = prefers;
	}

	@OneToMany(mappedBy = "user")
	private Collection<Prefer> prefers;

	@Column(length = 65535)
	private String publicInfo;

	public String getPublicInfo() {
		return publicInfo;
	}

	public void setPublicInfo(String publicInfo) {
		this.publicInfo = publicInfo;
	}

	public String getPrivateInfo() {

		return privateInfo;
	}

	public void setPrivateInfo(String privateInfo) {
		this.privateInfo = privateInfo;
	}

	@Column(length = 65535)
	private String privateInfo;
}

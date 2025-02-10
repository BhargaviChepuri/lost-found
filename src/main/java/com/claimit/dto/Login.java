
package com.claimit.dto;

public class Login {

	private Long id;
	private String email;
	private String password;
	private boolean isAdmin;

	public Login(Long id, String email, String password, boolean isAdmin) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.isAdmin = isAdmin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

}

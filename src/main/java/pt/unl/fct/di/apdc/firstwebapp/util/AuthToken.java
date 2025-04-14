package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2;

	public String username;
	public String tokenID;
	public String role;
	public long creationDate;
	public long expirationDate;
	public String verifier;

	public AuthToken() {
	}

	public AuthToken(String username, String role) {
		this.username = username;
		this.role = role;
		this.tokenID = UUID.randomUUID().toString();
		this.creationDate = System.currentTimeMillis();
		this.expirationDate = this.creationDate + EXPIRATION_TIME;
		this.verifier = UUID.randomUUID().toString();
	}

	public String getRole() {
		return role;
	}
}
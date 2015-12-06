package com.mereckas.dfs.authentication.client;

import com.mereckas.dfs.shared.ErrorReporter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sun.xml.internal.bind.api.impl.NameConverter;
import org.apache.commons.codec.binary.Base64;

public class AuthenticationClient {
	private int port;
	private String hostname;
	private String identity;
	private Socket s;
	private boolean connected;
	private MessageDigest messageDigest;

	public AuthenticationClient(String authServerHostname, int authServerPort, String identity) {
		this.port = authServerPort;
		this.hostname = authServerHostname;
		this.identity = identity;
		this.connected = false;
		try {
			this.messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			ErrorReporter.logError("Unable to create message digest", e.getMessage(),
					Thread.currentThread().getStackTrace()[2].getLineNumber());
		}
	}

	public void connect() {
		InetAddress address;
		try {
			address = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			ErrorReporter.logError("Unable to resolve hostname", e.getMessage(),
					Thread.currentThread().getStackTrace()[2].getLineNumber());
			return;
		}
		try {
			s = new Socket(address, port);
		} catch (IOException e) {
			ErrorReporter.logError("Error connecting to authentication server", e.getMessage(),
					Thread.currentThread().getStackTrace()[2].getLineNumber());
			return;
		}
		connected = true;
	}

	public boolean authenticate() {
		if (!connected) {
			connect();
			if (!connected) {
				ErrorReporter.logError("Unable to connect", "AuthClient.authenticate()", 0);
				return false;
			}
		}
		String hashedIdentity = getHashFromIdentity(identity);
		if(hashedIdentity!=null){
			System.out.println("Hashed identity: " + hashedIdentity);
			return true;
		}else{
			ErrorReporter.logError("Hashed identity was null","AuthClient.authenticate()",0);
			return false;
		}
	}

	private String getHashFromIdentity(String identity) {
		if (messageDigest != null) {
			return new String(Base64.encodeBase64(messageDigest.digest(identity.getBytes(StandardCharsets.UTF_8))));
		} else {
			ErrorReporter.logError("Message digest is null", "AuthClient.getHashFromIdentity()", 0);
			return null;
		}
	}
}

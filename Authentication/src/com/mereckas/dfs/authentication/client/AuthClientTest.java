package com.mereckas.dfs.authentication.client;
public class AuthClientTest {
	public static void main(String[] args){
		AuthenticationClient authenticationClient = new AuthenticationClient("0.0.0.0",8000,"Test");
		authenticationClient.connect();
		authenticationClient.authenticate();
	}
}

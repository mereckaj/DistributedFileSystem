package com.mereckas.dfs.authentication.server;
public class Main {
	private static int port;
	private static final int SERVER_MAX_BACKLOG = 50;
	public static void main(String[] args){
		System.out.println("Started");
		parseArguments(args);
		AuthenticationServer authenticationServer = new AuthenticationServer(port,SERVER_MAX_BACKLOG);
		authenticationServer.run();
	}
	private static void parseArguments(String[] args){
		for (String arg : args) {
			if (arg.contains("port")) {
				try {
					port = Integer.parseInt(arg.substring(arg.indexOf("=") + 1).trim());
				} catch (NumberFormatException e) {
					System.err.println("Could not parse port <" + arg + ">" + e.getMessage());
					System.exit(1);
				}
			}
		}
	}
}

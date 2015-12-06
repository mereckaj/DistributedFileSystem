package com.mereckas.dfs.authentication.server;
public class Main {
	private static int port;
	public static void main(String[] args){

	}
	private static void parseArguments(String[] args){
		for (String arg : args) {
			if (arg.contains("PORT")) {
				try {
					port = Integer.parseInt(arg.substring(arg.indexOf(":") + 1).trim());
				} catch (NumberFormatException e) {
					System.err.println("Could not parse port <" + arg + ">");
					System.exit(1);
				}
			}
		}
	}
}

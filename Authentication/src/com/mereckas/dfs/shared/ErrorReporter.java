package com.mereckas.dfs.shared;

public class ErrorReporter {
	public static void logError(String message,String error,int lineNumber){
		System.err.println(message + " | " + error + " | " + "line: " + lineNumber);
	}
}

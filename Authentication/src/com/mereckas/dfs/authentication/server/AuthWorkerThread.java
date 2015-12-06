package com.mereckas.dfs.authentication.server;

import com.mereckas.dfs.shared.MessageQueueWorkerThread;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import com.mereckas.dfs.shared.ErrorReporter;
public class AuthWorkerThread implements Runnable {
	Socket s;
	MessageQueueWorkerThread messageQueueWorkerThread;
	public AuthWorkerThread(Socket s){
		this.s = s;
		try {
			this.messageQueueWorkerThread = new MessageQueueWorkerThread(new OutputStreamWriter(s.getOutputStream()));
		} catch (IOException e) {
			ErrorReporter.logError("Could not get OutputStream",e.getMessage(),Thread.currentThread().getStackTrace()[2].getLineNumber());
		}
	}

	@Override
	public void run() {
		System.out.println("Added new worker");
		try {
			s.close();
		} catch (IOException e) {
			ErrorReporter.logError("Error closing socket",e.getMessage(),Thread.currentThread().getStackTrace()[2].getLineNumber());
		}
	}
}

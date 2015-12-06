package com.mereckas.dfs.authentication.server;

import com.mereckas.dfs.shared.ThreadPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class AuthenticationServer {
	private final int THREAD_POOL_SIZE = 10;
	private boolean running;
	private ServerSocket socket;
	private ThreadPool<AuthWorkerThread> threadPool;

	public AuthenticationServer(int port, int backlog) {
		try {
			socket = new ServerSocket(port, backlog, InetAddress.getByName("0.0.0.0"));
		} catch (UnknownHostException e) {
			System.err.println("Unknown host when creating server socket: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Could not create socket: " + e.getMessage());
		}
		threadPool = new ThreadPool<>(THREAD_POOL_SIZE);
		System.out.println("Created Authentication Server: Port <" + port + ">"
				+ " Thread pool size <" + THREAD_POOL_SIZE +">"
				+ " Backlog <" + backlog +">");
	}

	public void run() {
		running = true;
		while (running) {
			try {
				threadPool.addJobToQueue(new AuthWorkerThread(socket.accept()));
			} catch (IOException e) {
				System.err.println("Error accepting socket: " + e.getMessage());
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}

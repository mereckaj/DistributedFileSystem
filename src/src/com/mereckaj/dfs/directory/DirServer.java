package com.mereckaj.dfs.directory;

import com.mereckaj.dfs.shared.ThreadPool;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;

public class DirServer implements Runnable {
	private int port;
	private String hostname;
	private ServerSocket serverSocket;
	private ThreadPool<DirServerWorker> threadPool;
	private boolean running;

	public DirServer(int port,String hostname,int threadPoolSize){
		this.port = port;
		this.hostname = hostname;
		this.threadPool = new ThreadPool<>(threadPoolSize);
		this.running = false;
		try {
			this.serverSocket = new ServerSocket(port,50,InetAddress.getByName(hostname));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		running = true;
		while(running){
			try {
				threadPool.addJobToQueue(new DirServerWorker(serverSocket.accept(),"/"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void terminate() {
		threadPool.terminate();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop(){
		running = false;
		terminate();
	}
}

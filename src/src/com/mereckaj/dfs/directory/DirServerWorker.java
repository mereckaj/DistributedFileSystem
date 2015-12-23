package com.mereckaj.dfs.directory;

import com.mereckaj.dfs.shared.MessageQueueWorkerThread;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class DirServerWorker implements Runnable {
	private Socket socket;
	private InputStreamReader isr;
	private OutputStreamWriter osw;
	private MessageQueueWorkerThread msqw;
	private boolean running;
	private static final int RECEIVE_BUFFER_SIZE = 65536;
	private String rootDirectory;
	public DirServerWorker(Socket s, String startDir) {
		try {
			isr = new InputStreamReader(s.getInputStream());
			osw = new OutputStreamWriter(s.getOutputStream());
			msqw = new MessageQueueWorkerThread(osw);
			running = true;
			rootDirectory = startDir;
			socket = s;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (running) {
			String m = readMessage();
			dealWithMessage(m);
		}
	}

	private void dealWithMessage(String message) {
		System.out.println("command: " + message);
		String[] commands;
		if (message != null && message.length() > 1) {
			commands = message.split("\n");
		} else {
			return;
		}
		try {
			if (commands[0].contains("RESOLVE")) {
				doResolveCommand(commands);
			}else{
				throw new Exception();
			}
		} catch (Exception e) {
			System.err.println("Invalid command: " + message);
		}
	}

	private void doResolveCommand(String[] commands) {
		System.out.println("Resolved");
	}

	private String readMessage() {
		char[] buffer = new char[RECEIVE_BUFFER_SIZE];
		char[] result = null;
		int read = 0;
		boolean get = true;
		while (get) {
			try {
				if (socket.isClosed()) {
					terminate();
					return "";
				}
				read = isr.read(buffer, 0, buffer.length);
				if (read > 0) {
					result = new char[read];
					System.arraycopy(buffer, 0, result, 0, read);
					get = false;
				}
			} catch (IOException e) {
				get = false;
			}
		}
		return new String(result);
	}

	public void terminate() {
		try {
			running = false;
			msqw.running = false;
			msqw.osw.close();
			msqw.messageQueue.clear();
			isr.close();
			osw.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package com.mereckaj.dfs.directory;

import com.mereckaj.dfs.shared.MessageQueueWorkerThread;
import com.sun.corba.se.impl.activation.ServerMain;

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
		String[] commands;
		if (message != null && message.length() > 1) {
			commands = message.split(" ");
		} else {
			return;
		}
		try {
			if (commands[0].equalsIgnoreCase("ls")) {
				doListCommand(commands);
			} else if (commands[0].equalsIgnoreCase("cd")) {
				doChangeDirCommand(commands);
			} else if (commands[0].equalsIgnoreCase("mkdir")) {
				doMakeDirCommand(commands);
			} else if (commands[0].equalsIgnoreCase("touch")) {
				doTouchCommand(commands);
			} else {
				throw new Exception("Unrecognized command");
			}
		} catch (Exception e) {
			System.err.println("Invalid command: " + message);
		}
	}

	private void doTouchCommand(String[] commands) {
		//TODO: implement
	}

	private void doMakeDirCommand(String[] commands) {
		//TODO: implement
	}

	private void doChangeDirCommand(String[] commands) {
		//TODO: implement
	}

	private void doListCommand(String[] commands) {
		//TODO: implement
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
				e.printStackTrace();
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

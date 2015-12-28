import com.mereckaj.dfs.shared.MessageQueueWorkerThread;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class FileServerWorker implements Runnable {
	private Socket socket;
	private InputStreamReader isr;
	private OutputStreamWriter osw;
	private MessageQueueWorkerThread msqw;
	private boolean running;
	private static final int RECEIVE_BUFFER_SIZE = 65536;
	private static FileManager fm = FileManager.getInstance();

	public FileServerWorker(Socket s) {
		try {
			isr = new InputStreamReader(s.getInputStream());
			osw = new OutputStreamWriter(s.getOutputStream());
			msqw = new MessageQueueWorkerThread(osw);
			running = true;
			socket = s;
			msqw.start();
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
		System.out.println("command:\n" + message);
		String[] commands;
		if (message != null && message.length() > 1) {
			commands = message.split("\n");
		} else {
			return;
		}
		try {
			if (commands[0].contains("READ_REQUEST")) {
				doReadRequest(commands);
			} else if (commands[0].contains("WRITE_REQUEST")) {
				doWriteRequest(commands);
			} else if (commands[0].contains("LIST_REQUEST")) {
				doListRequest(commands);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Invalid command: " + message);
		}
	}

	private void doListRequest(String[] commands) {
		msqw.addMessageToQueue("LOL");
	}

	private void doWriteRequest(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		String file = commands[2].split(":")[1].trim();
		String data = commands[3].split(":")[1].trim();
		String message = "WRITE_RESPONSE:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n";
		if (fm.write(dir, file, data)) {
			message += "SUCCESS:TRUE\n";
		} else {
			message += "SUCCESS:TRUE\n";
		}
		msqw.addMessageToQueue(message);
	}

	private void doReadRequest(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		String file = commands[2].split(":")[1].trim();
		String message = "READ_RESPONSE:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n";
		if (fm.exists(dir, file)) {
			message += "EXISTS:TRUE\n"
					+ "DATA:" + fm.read(dir, file) + "\n";
		} else {
			message += "EXISTS:FALSE\n" +
					"DATA: ";
		}
		msqw.addMessageToQueue(message);
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

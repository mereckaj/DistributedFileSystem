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
	private static FileMapper fm = FileMapper.getInstance();

	public DirServerWorker(Socket s) {
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
			if (commands[0].contains("RESOLVE_REQUEST")) {
				doResolveCommand(commands);
			} else if (commands[0].contains("ADD_DIR")) {
				doAddCommand(commands);
			} else if (commands[0].contains("REM_DIR")) {
				doRemCommand(commands);
			} else if (commands[0].contains("LIST_DIR_REQUEST")) {
				doListCommand(commands);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Invalid command: " + message);
		}
	}

	private void doListCommand(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		ServiceInfo[] list = fm.getAllServiceList();
		String message = "LIST_DIR_RESPONSE:\n" +
				"DIR:" + dir + "\n";
		for (ServiceInfo x : list) {
			if (x.key.contains(dir) && !x.key.equals(dir)) {
				message += "DIR_R:" + x.key + "\n";
			}
		}
		System.out.println("LIST:" + message);
		msqw.addMessageToQueue(message);
	}

	private void doRemCommand(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		fm.remove(dir);
	}

	private void doAddCommand(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		String[] possibleDirs = fm.getAllPossibleSubstrings(dir);
		ServiceInfo si = fm.get(dir);
		if (si == null) {
			for (int i = possibleDirs.length - 1; i >= 0; i--) {
				si = fm.get(possibleDirs[i]);
				if (si != null) {
					fm.add(dir, si.ip, si.port, si.serviceInfo);
					return;
				}
			}
		} else {
			fm.add(dir, si.ip, si.port, si.serviceInfo);
		}
	}

	private void doResolveCommand(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		String file = commands[2].split(":")[1].trim();

		String message = "RESOLVE_RESPONSE:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n";

		ServiceInfo si = fm.get(dir);
		if (si == null) {
			message += "EXISTS:FALSE\n" +
					"HOST: \n" +
					"PORT: \n";
		} else {
			message += "EXISTS:TRUE\n" +
					"HOST:" + si.ip + "\n" +
					"PORT:" + si.port + "\n";
		}
		msqw.addMessageToQueue(message);
		System.out.println(message);
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

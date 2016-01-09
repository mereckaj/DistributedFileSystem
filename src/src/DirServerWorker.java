import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class DirServerWorker implements Runnable {
	private Socket socket;
	private InputStreamReader isr;
	private OutputStreamWriter osw;
	private MessageQueueWorkerThread msqw;
	private BufferedReader br;
	private boolean running;
	private static final int RECEIVE_BUFFER_SIZE = 65536;
	private static FileMapper fm = FileMapper.getInstance();
	private int taskID = 0;

	public DirServerWorker(Socket s,int task) {
		try {
			isr = new InputStreamReader(s.getInputStream());
			osw = new OutputStreamWriter(s.getOutputStream());
			msqw = new MessageQueueWorkerThread(osw);
			br = new BufferedReader(isr);
			running = true;
			socket = s;
			msqw.start();
			taskID = task;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Started task: " + taskID);
		String m = readMessage();
		dealWithMessage(m);
		System.out.println("Finished task: " + taskID);
	}

	private void dealWithMessage(String message) {
//		System.out.println("command:\n" + message);
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
//			terminate();
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
//		System.out.println("LIST:" + message);
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
//		System.out.println(message);
	}

	private String readMessage() {
		String line = "";
		String response = "";
		try {
			while ((line = br.readLine()) != null) {
//				System.out.println(line);
				response = response + line + "\n";
				if (!br.ready()) {
					break;
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}
		return response;
	}

	public void terminate() {
		try {
			running = false;
			msqw.running = false;
			System.out.println("Closed msqw");
			msqw.osw.close();
			msqw.messageQueue.clear();
			System.out.println("Cleared remaining queue");
			isr.close();
			osw.close();
			socket.close();
			System.out.println("Closed all streams");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

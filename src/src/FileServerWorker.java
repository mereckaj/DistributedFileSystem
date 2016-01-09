import java.io.BufferedReader;
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
	private static final int SOCKET_TIMEOUT = 5000;
	private static FileManager fm = FileManager.getInstance();
	private BufferedReader br;
	private StringBuilder sb;

	public FileServerWorker(Socket s) {
		try {
			isr = new InputStreamReader(s.getInputStream());
			osw = new OutputStreamWriter(s.getOutputStream());
			msqw = new MessageQueueWorkerThread(osw);
			br = new BufferedReader(isr);
			sb = new StringBuilder();
			running = true;
			socket = s;
			socket.setSoTimeout(SOCKET_TIMEOUT);
			msqw.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
			String m = readMessage();
			dealWithMessage(m);
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
		System.out.println("Message prepared");
		msqw.addMessageToQueue(message);
		System.out.println("Message added to send queue");
	}

	private String readMessage() {
		String line;
		String response = "";
		try {
			while ((line = br.readLine()) != null) {
				System.out.println(line);
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

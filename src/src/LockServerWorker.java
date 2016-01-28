import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class LockServerWorker implements Runnable {
	private Socket socket;
	private InputStreamReader isr;
	private OutputStreamWriter osw;
	private MessageQueueWorkerThread msqw;
	private BufferedReader br;
	private boolean running;
	private static final int RECEIVE_BUFFER_SIZE = 65536;
	private static LockManager lockManager = LockManager.getInstance();
	private int taskID = 0;

	public LockServerWorker(Socket s, int task) {
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
		String m = readMessage();
		dealWithMessage(m);
		System.out.println("Terminated thread");
	}

	private void dealWithMessage(String message) {
		String[] commands;
		System.out.println("Dealing with ");
		System.out.println("-------------------");
		System.out.println(message);
		System.out.println("-------------------");
		if (message != null && message.length() > 1) {
			commands = message.split("\n");
		} else {
			return;
		}
		try {
			if (commands[0].contains("UNLOCK_FILE")) {
				doUnlock(commands);
			} else if (commands[0].contains("LOCK_FILE")) {
				doLock(commands);
			} else if (commands[0].contains("IS_LOCKED")) {
				doCheckIfLocked(commands);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Invalid command: " + message);
		}
	}

	private void doCheckIfLocked(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		String file = commands[2].split(":")[1].trim();
		System.out.println("Checking: " + dir + "" + file);
		int locked = lockManager.isLocked(file, dir);
		String message = prepareLockCheckMessage(locked, dir, file);
		msqw.addMessageToQueue(message);
		System.out.println("Check reply sent: " + dir + "" + file);
	}

	private String prepareLockCheckMessage(int locked, String dir, String file) {
		return "IS_LOCKED_RESPONSE:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n" +
				"LOCKED:" + (locked == 0 ? "FALSE" : "TRUE") + "\n" +
				"Q_SIZE:" + locked + "\n";
	}

	private void doLock(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		String file = commands[2].split(":")[1].trim();
		System.out.println("Locking: " + dir + "" + file);
		lockManager.lock(file, dir, Thread.currentThread());
		String message = prepareLockedReponse(file, dir);
		msqw.addMessageToQueue(message);
		System.out.println("Send reply");
	}

	private String prepareLockedReponse(String file, String dir) {
		return "LOCK_ACCESS:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n";
	}

	private void doUnlock(String[] commands) {
		String dir = commands[1].split(":")[1].trim();
		String file = commands[2].split(":")[1].trim();
		System.out.println("Unlocking: " + dir + file);
		lockManager.unlock(file, dir);
		System.out.println("Unlocked: " + dir + file);
		msqw.addMessageToQueue("UNLOCKED_THE_FUCKING_LOCK");
	}

	private String readMessage() {
		String line = "";
		String response = "";
		try {
			while ((line = br.readLine()) != null) {
				response = response + line + "\n";
				if (!br.ready()) {
					break;
				}
			}
		} catch (IOException e) {
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

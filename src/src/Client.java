
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client extends Thread {
	private BufferedReader br;

	public Client() {
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	public void run() {
		boolean running = true;
		String userInput;
		while (running) {
			try {
				userInput = br.readLine();
				parseInput(userInput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void parseInput(String userInput) {
		String[] commands;
		if (userInput != null && userInput.length() > 1) {
			commands = userInput.split(" ");
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
			} else if (commands[0].equalsIgnoreCase("help")) {
				doHelpCommand(commands);
			} else {
				throw new Exception("Unrecognized command");
			}
		} catch (Exception e) {
			System.err.println("Invalid command: " + userInput);
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

	private void doHelpCommand(String[] commands) {
		System.out.println("<--Help Start-->");
		System.out.println("Command Info");
		System.out.println("help\n" +
				"\t(Show this menu)");
		System.out.println("ls\n" +
				"\t(List files in this directory)");
		System.out.println("cd\n" +
				"\t(Changed Directory)");
		System.out.println("cd ..\n" +
				"\t(Go back one directory)");
		System.out.println("mkdir <name>\n" +
				"\t(Make a new directory name <name>)");
		System.out.println("touch <name>\n" +
				"\t(Make a new file named <name>)");
		System.out.println("<--Help End-->");
	}
}

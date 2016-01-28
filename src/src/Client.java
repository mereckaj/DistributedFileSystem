import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;

public class Client extends Thread {
	private static final int RECEIVE_BUFFER_SIZE = 65536;
	private BufferedReader br;
	public static ServiceInfo dirServer = new ServiceInfo("188.166.36.191", 8080, ServiceInfo.ServiceType.DIR_SERVICE, null);

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
			if (commands[0].equalsIgnoreCase("write")) {
				doWriteCommand(commands);
			} else if (commands[0].equalsIgnoreCase("read")) {
				doReadCommand(commands);
			} else if (commands[0].equalsIgnoreCase("help")) {
				doHelpCommand(commands);
			} else if (commands[0].equalsIgnoreCase("lock")) {
				doLockCommand(commands);
			} else if (commands[0].equalsIgnoreCase("unlock")) {
				doUnlockCommand(commands);
			} else {
				throw new Exception("Unrecognized command");
			}
		} catch (Exception e) {
			System.err.println("Invalid command: " + userInput);
		}
	}

	private void doUnlockCommand(String[] commands) {
		try {
			Socket socketToDirService = new Socket(InetAddress.getByName("0.0.0.0"), 8085);
			System.out.println("Connected to server: " + dirServer.ip + ":" + dirServer.port);

			String message = "UNLOCK_FILE:\n" +
					"DIR: /etc/\n" +
					"FILE: test.txt\n";

			socketToDirService.getOutputStream().write(message.getBytes(),0,message.length());
			socketToDirService.getOutputStream().flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doLockCommand(String[] commands) {
		try {
			Socket socketToDirService = new Socket(InetAddress.getByName("0.0.0.0"), 8085);
			System.out.println("Connected to server: " + dirServer.ip + ":" + dirServer.port);

			String message = "LOCK_FILE:\n" +
					"DIR: /etc/\n" +
					"FILE: test.txt\n";

			socketToDirService.getOutputStream().write(message.getBytes(),0,message.length());
			socketToDirService.getOutputStream().flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String validate(String dir) {
		dir = dir.trim();
		dir = dir.replace(" ", "\\ ");
		if (dir.startsWith("/")) {
			if (!dir.endsWith("/")) {
				dir = dir + "/";
			}
		} else {
			return validate("/" + dir);
		}
		return dir;
	}

	private void doWriteCommand(String[] commands) {
		if (commands.length < 3) {
			System.out.println("Too few arguments\t WRITE location/file data");
			return;
		}
		try {
			Socket socketToDirService = new Socket(InetAddress.getByName(dirServer.ip), dirServer.port);
			System.out.println("Connected to server: " + dirServer.ip + ":" + dirServer.port);


			String dir = parseDir(commands[1]);
			dir = validate(dir);
			System.out.println("Parsed dir: " + dir);
			String file = parseFile(commands[1]);

			ServiceInfo fileServiceInfo = resolveFileServer(dir, file, socketToDirService);
			if (fileServiceInfo == null) {
				System.out.println(dir + "" + file + "File does not exist");
				socketToDirService.close();
				return;
			}

			Socket socketToFileService = new Socket(InetAddress.getByName(fileServiceInfo.ip), fileServiceInfo.port);

			String data = "";
			for (int i = 2; i < commands.length; i++) {
				data += commands[i];
			}

			String encodedData = DatatypeConverter.printBase64Binary(data.getBytes());

			boolean responseSuccessful = putDataToFileServer(fileServiceInfo, dir, file, encodedData, socketToFileService);
			if (!responseSuccessful) {
				System.out.println("Could not write " + dir + "" + file + " with: " + data);
			} else {
				System.out.println("Successfully wrote to " + dir + "" + file);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean putDataToFileServer(ServiceInfo si, String dir, String file, String data, Socket s) {
		BufferedOutputStream bos;
		boolean result = false;
		try {
			bos = new BufferedOutputStream(s.getOutputStream());

			String getFileMessage = createWriteMessage(dir, file, data);
			System.out.println("Prepared a writeRequest");

			bos.write(getFileMessage.getBytes(), 0, getFileMessage.length());
			bos.flush();
			System.out.println("Sent write request");

			String writeReply = readMessage(new InputStreamReader(s.getInputStream()));
			System.out.println("Got write reply:\n" + writeReply);

			result = checkIfWriteSuccessful(writeReply);
			System.out.println("Parsed write reply: " + result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void doReadCommand(String[] commands) {
		if (commands.length < 2) {
			System.out.println("Too few arguments\t READ location/file");
			return;
		}
		try {
			Socket socketToDirService = new Socket(InetAddress.getByName(dirServer.ip), dirServer.port);
			System.out.println("Connected to server: " + dirServer.ip + ":" + dirServer.port);


			String dir = parseDir(commands[1]);
			dir = validate(dir);
			System.out.println("Parsed dir: " + dir);
			String file = parseFile(commands[1]);

			ServiceInfo fileServiceInfo = resolveFileServer(dir, file, socketToDirService);
			if (fileServiceInfo == null) {
				System.out.println(dir + "" + file + "File does not exist");
				socketToDirService.close();
				return;
			}

			Socket socketToFileService = new Socket(InetAddress.getByName(fileServiceInfo.ip), fileServiceInfo.port);

			String encodedFileData = getDataFromFileService(fileServiceInfo, dir, file, socketToFileService);

			if (encodedFileData == null) {
				System.out.println(dir + "" + file + "File does not exist");
				socketToDirService.close();
				socketToFileService.close();
				return;
			}

			String decodedFileData = new String(DatatypeConverter.parseBase64Binary(encodedFileData));
			System.out.println("Data: " + decodedFileData);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getDataFromFileService(ServiceInfo fileServiceInfo, String dir, String file, Socket s) {
		BufferedOutputStream bos;
		String result = null;
		try {
			bos = new BufferedOutputStream(s.getOutputStream());

			String getFileMessage = createReadMessage(dir, file);
			System.out.println("Prepared a getRequest");

			bos.write(getFileMessage.getBytes(), 0, getFileMessage.length());
			bos.flush();
			System.out.println("Send read request");

			String readReply = readMessage(new InputStreamReader(s.getInputStream()));
			System.out.println("Got read reply:\n" + readReply);

			result = parseReadReply(readReply);
			System.out.println("Parsed read reply");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String createReadMessage(String dir, String file) {
		return "READ_REQUEST:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n";
	}

	private String createWriteMessage(String dir, String file, String data) {
		return "WRITE_REQUEST:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n" +
				"DATA:" + data + "\n";
	}

	private ServiceInfo resolveFileServer(String dir, String file, Socket s) {
		BufferedOutputStream bos;
		ServiceInfo si = null;

		try {
			bos = new BufferedOutputStream(s.getOutputStream());

			System.out.println("Parsed file: " + file);
			String resolveMessage = createResolveMessage(dir, file);

			bos.write(resolveMessage.getBytes(), 0, resolveMessage.length());
			bos.flush();
			System.out.println("Wrote to dir server");

			String resolveReply = readMessage(new InputStreamReader(s.getInputStream()));
			System.out.println("Dir server reply:\n" + resolveReply);

			si = parseResolveReply(resolveReply);
			System.out.println("Parsed reply");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return si;
	}

	private String readMessage(InputStreamReader isr) {
		char[] buffer = new char[RECEIVE_BUFFER_SIZE];
		char[] result = null;
		int read;
		boolean get = true;

		while (get) {
			try {
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

	private String parseFile(String command) {
//		/etc/kek/file.txt -> file.txt
		return command.substring(command.lastIndexOf("/") + 1, command.length());
	}

	private String parseDir(String command) {
//		/etc/kek/file.txt -> /etc/kek/
		return command.substring(0, command.lastIndexOf("/") + 1);
	}

	private String parseReadReply(String reply) {
		String[] lines = reply.split("\n");
		boolean exists = lines[3].split(":")[1].trim().equalsIgnoreCase("TRUE");
		System.out.println("exists: " + lines[3] + " -->> " + exists);
		if (exists) {
			return lines[4].split(":")[1].trim();
		} else {
			return null;
		}
	}

	private boolean checkIfWriteSuccessful(String reply) {
		String[] lines = reply.split("\n");
		boolean exists = lines[3].split(":")[1].trim().equalsIgnoreCase("TRUE");
		System.out.println("exists: " + lines[3] + " -->> " + exists);
		return exists;
	}

	private ServiceInfo parseResolveReply(String reply) {
		String[] lines = reply.split("\n");
		boolean exists = lines[3].split(":")[1].trim().equalsIgnoreCase("TRUE");
		System.out.println("exists: " + lines[3] + " -->> " + exists);
		if (exists) {
			String ip = lines[4].split(":")[1].trim();
			int port = Integer.parseInt(lines[5].split(":")[1].trim());
			String dir = lines[1].split(":")[1].trim();
			return new ServiceInfo(ip, port, ServiceInfo.ServiceType.DIR_SERVICE, dir);
		} else {
			return null;
		}
	}

	private String createResolveMessage(String dir, String file) {
		return "RESOLVE_REQUEST:\n" +
				"DIR:" + dir + "\n" +
				"FILE:" + file + "\n";
	}

	private void doHelpCommand(String[] commands) {
		System.out.println("<--Help Start-->");
		System.out.println("Command Info");
		System.out.println("help\n" +
				"\t(Show this menu)");
		System.out.println("write <path>/<file> <data>\n" +
				"\tWrite <data> to <file> located in <path>");
		System.out.println("read <path>/<file>\n" +
				"\tRead <file> located in <path>");
		System.out.println("<--Help End-->");
	}
}

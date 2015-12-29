
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

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
			} else {
				throw new Exception("Unrecognized command");
			}
		} catch (Exception e) {
			System.err.println("Invalid command: " + userInput);
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

			ServiceInfo fileServiceInfo = resolveFileServer(dir,file,socketToDirService);
			if(fileServiceInfo==null){
				System.out.println(dir + "" + file + "File does not exist");
				socketToDirService.close();
				return;
			}

			Socket socketToFileService = new Socket(InetAddress.getByName(fileServiceInfo.ip),fileServiceInfo.port);

			String encodedFileData = getDataFromFileService(fileServiceInfo,dir,file,socketToFileService);

			if(encodedFileData==null){
				System.out.println(dir + "" + file + "File does not exist");
				socketToDirService.close();
				socketToFileService.close();
				return;
			}

			String decodedFileData = new String(Base64.decode(encodedFileData));
			System.out.println("Data: " + decodedFileData);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getDataFromFileService(ServiceInfo fileServiceInfo, String dir, String file, Socket s) {
		BufferedOutputStream bos = null;
		String result = null;
		try{
			bos = new BufferedOutputStream(s.getOutputStream());

			String getFileMessage = createReadMessage(dir,file);
			System.out.println("Prepared a getRequest");

			bos.write(getFileMessage.getBytes(),0,getFileMessage.length());
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
				"DIR:"+ dir + "\n" +
				"FILE:" + file + "\n";
	}

	private ServiceInfo resolveFileServer(String dir, String file, Socket s) {
		BufferedOutputStream bos = null;
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
		int read = 0;
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

	private void doWriteCommand(String[] commands) {

	}
	private String parseReadReply(String reply){
		String[] lines = reply.split("\n");
		boolean exists = lines[3].split(":")[1].trim().equalsIgnoreCase("TRUE");
		System.out.println("exists: " + lines[3] + " -->> " + exists);
		if (exists) {
			return lines[4].split(":")[1].trim();
		} else {
			return null;
		}
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

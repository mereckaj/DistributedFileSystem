import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class FileServer extends Thread {
	private int port;
	private String hostname;
	private ServerSocket serverSocket;
	private ThreadPool<FileServerWorker> threadPool;
	private boolean running;

	public FileServer(int port, String hostname, int threadPoolSize) {
		this.port = port;
		this.hostname = hostname;
		this.threadPool = new ThreadPool<>(threadPoolSize);
		this.running = false;
		try {
			this.serverSocket = new ServerSocket(port, threadPoolSize * 50, InetAddress.getByName(hostname));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				threadPool.addJobToQueue(new FileServerWorker(serverSocket.accept()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void terminate() {
		threadPool.terminate();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		running = false;
		terminate();
	}

	public static void main(String args[]){
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		System.out.println("Starting File Server on: " + ip + ":" + port);
		FileServer fs = new FileServer(port,ip,10);
		fs.run();
	}
}

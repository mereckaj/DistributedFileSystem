import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class DirServer implements Runnable {
	private int port;
	private String hostname;
	private ServerSocket serverSocket;
	private ThreadPool<DirServerWorker> threadPool;
	private boolean running;

	public DirServer(int port, String hostname, int threadPoolSize) {
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
				DirServerWorker worker = new DirServerWorker(serverSocket.accept());
				threadPool.addJobToQueue(worker);
				System.out.println("Accepted new connection: " + worker.toString());
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

	public void stop() {
		running = false;
		terminate();
	}

	public static void main(String[] args) {
		String ip = args[0];
		int port = new Integer(args[1]);
		System.out.println("Starting Directory Server on: " + ip + ":" + port);
		DirServer dirServer = new DirServer(port, ip, 10);
		dirServer.run();
	}
}

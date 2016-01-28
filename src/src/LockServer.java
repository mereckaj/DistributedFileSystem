import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class LockServer implements Runnable {
	private int port;
	private String hostname;
	private ServerSocket serverSocket;
	private ThreadPool<LockServerWorker> threadPool;
	private boolean running;
	int taskcount = 0;

	public LockServer(int port, String hostname, int threadPoolSize) {
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
				System.out.println("Waiting for new connection");
				System.out.println("------------------------");
				System.out.println("Task count: " + taskcount);
				LockServerWorker worker = new LockServerWorker(serverSocket.accept(),taskcount);
				threadPool.addJobToQueue(worker);
				taskcount++;
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
		LockServer lockServer = new LockServer(port, ip, 10);
		lockServer.run();
	}
}

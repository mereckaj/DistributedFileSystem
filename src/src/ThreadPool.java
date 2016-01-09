
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool<T extends Runnable> {
	public ExecutorService executorService;

	public ThreadPool(int threadPoolSize) {
		this.executorService = Executors.newFixedThreadPool(threadPoolSize);
	}

	public void addJobToQueue(T t) {
		executorService.execute(t);
		System.out.println("Executed job: " + t.toString());
	}

	public void terminate() {
		executorService.shutdownNow();
	}
}


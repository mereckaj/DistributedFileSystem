
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPool<T extends Runnable> {
	public ExecutorService executorService;

	public ThreadPool(int threadPoolSize) {
		this.executorService = Executors.newFixedThreadPool(threadPoolSize);
	}

	public void addJobToQueue(T t) {
		Future<String> res = (Future<String>) executorService.submit(t);
		try{
			display(res.get());
		}catch (ExecutionException e){
			e.printStackTrace();
			System.out.println("Future exception");
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Future exception");
		}
	}

	private void display(String s){
		System.out.println("Future result: " + s);
	}
	public void terminate() {
		executorService.shutdownNow();
	}
}


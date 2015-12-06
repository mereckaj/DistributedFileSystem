package com.mereckas.dfs.shared;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool<T extends Runnable>{
	private ExecutorService executorService;

	private ThreadPool(int threadPoolSize) {
		this.executorService = Executors.newFixedThreadPool(threadPoolSize);
	}

	public void addJobToQueue(T jobToSchedule) {
		executorService.execute(jobToSchedule);
	}

	public void terminate() {
		executorService.shutdownNow();
	}
}


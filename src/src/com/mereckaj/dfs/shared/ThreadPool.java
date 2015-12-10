package com.mereckaj.dfs.shared;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool<T extends Runnable>{
	private int threadPoolSize;
	private ExecutorService executorService;
	public ThreadPool(int threadPoolSize){
		this.executorService = Executors.newFixedThreadPool(threadPoolSize);
		this.threadPoolSize = threadPoolSize;
	}
	public void addJobToQueue(T t){
		executorService.execute(t);
	}
	public void terminate() {
		executorService.shutdownNow();
	}
}


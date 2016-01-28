import java.util.concurrent.atomic.AtomicBoolean;

public class SpinToWInLock {
	private AtomicBoolean lock;

	public SpinToWInLock() {
		lock = new AtomicBoolean(false);
	}

	public synchronized void lock(Thread t) {
		while (true) {
			if (!lock.compareAndSet(false, true)) {
				do {
					try {
						t.sleep(5);
					} catch (InterruptedException e) {
					}
				} while (!lock.get());
			} else {
				System.out.println("Got " + Thread.currentThread().getName());
				return;
			}
		}
	}

	public void unlock() {
		System.out.println("In lock unlock");
		lock.set(false);
		System.out.println("unlocked");
	}
}

import java.util.HashMap;

public class LockManager {
	private static LockManager instance = new LockManager();
	private static HashMap<String, SpinToWInLock> map;

	private LockManager() {
		if (instance == null) {
			map = new HashMap<>();
		}
	}

	public static LockManager getInstance() {
		return instance;
	}

	public synchronized void lock(String file, String dir, Thread t) {
		dir = validate(dir);
		System.out.println("Checking if lock exists for: " + dir + "" + file);
		if (map.containsKey(dir + file)) {
			System.out.println("Found lock for: " + dir + "" + file);
			map.get(dir + file).lock(t);
		} else {
			System.out.println("No lock for: " + dir + "" + file);
			map.put(dir + file, new SpinToWInLock());
			map.get(dir + file).lock(t);
		}
		System.out.println("Acquired the lock");
	}

	public synchronized void unlock(String file, String dir) {
		dir = validate(dir);
		if(map.containsKey(dir+file)){
			System.out.println("Unlock found");
			map.get(dir + file).unlock();
		}else{
			System.out.println("Unlock not found");
		}
	}

	public int isLocked(String file, String dir) {
		dir = validate(dir);
		if (map.containsKey(dir + file)) {
			return 1;
		} else {
			return 0;
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
}

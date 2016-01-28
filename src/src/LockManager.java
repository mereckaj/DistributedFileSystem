import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LockManager {
	private static LockManager instance = new LockManager();
	private CopyOnWriteArrayList<String> list;

	private LockManager() {
		if (instance == null) {
			list = new CopyOnWriteArrayList<String>();
		}
	}

	public static LockManager getInstance() {
		return instance;
	}

	public boolean lock(String file, String dir) {
		dir = validate(dir);
		if (list.contains(dir + file)) {
			return false;
		}else{
			list.add(dir+ file);
			return true;
		}
	}

	public void unlock(String file, String dir) {
		dir = validate(dir);
		if(list.contains(dir+file)){
			list.remove(dir + file);
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

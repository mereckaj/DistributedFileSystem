
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FileMapper {
	private static final String rootDir = "~";
	private static FileMapper instance = new FileMapper();
	private ConcurrentHashMap<String, ServiceInfo> map = new ConcurrentHashMap<>();

	private FileMapper() {
		if (instance == null) {
			map.put("/", new ServiceInfo("188.166.36.191", 8081, ServiceInfo.ServiceType.FILE_SERVICE, "/"));
			map.put("/etc/", new ServiceInfo("188.166.36.191", 8082, ServiceInfo.ServiceType.FILE_SERVICE, "/etc/"));
			map.put("/tmp/", new ServiceInfo("188.166.36.191",8083, ServiceInfo.ServiceType.FILE_SERVICE, "/tmp"));
		}
	}

	public static FileMapper getInstance() {
		return instance;
	}

	public boolean exists(String dir) {
		dir = validate(dir);
		if (map.containsKey(dir)) {
			return true;
		}
		return false;
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

	public void remove(String dir) {
		dir = validate(dir);
		ServiceInfo dirs[] = getAllServiceList();
		for (ServiceInfo x : dirs) {
			if (x.key.contains(dir)) {
				System.out.println("Removed: " + x.key);
				map.remove(x.key);
			}
		}
		if (exists(dir)) {
			System.out.println("Removed: " + dir);
			map.remove(dir);
		}

	}


	public ServiceInfo get(String dir) {
		return map.get(dir);
	}

	public String[] getAllPossibleSubstrings(String dir) {
		String[] dirs = dir.split("/");
		String[] result = new String[dirs.length];
		for (int i = dirs.length - 1; i >= 0; i--) {
			String sub = "";
			for (int j = 0; j <= i; j++) {
				sub += dirs[j];
				sub = validate(sub);
			}
			result[i] = sub;
		}
		return result;
	}

	public void add(String dir, String addr, int port, ServiceInfo.ServiceType info) {
		if (serviceExists(new ServiceInfo(addr, port, info, dir), dir)) {
			System.out.println("Service already exists");
			return;
		}
		dir = validate(dir);
		String[] possibilities = getAllPossibleSubstrings(dir);
		for (int i = possibilities.length - 1; i >= 0; i--) {
			if (!exists(possibilities[i])) {
				System.out.println("Adding Service\t" + info.name() + "\t" + possibilities[i] + "\t" + addr + ":" + port);
				_add(possibilities[i], new ServiceInfo(addr, port, info, dir));
			}
		}
		if (!exists(dir)) {

		}
	}

	private boolean serviceExists(ServiceInfo serviceInfo, String dir) {
		ServiceInfo[] sis = this.getAllServiceList();
		for (ServiceInfo x : sis) {
			if ((x.ip.equals(serviceInfo.ip)) && (x.port == serviceInfo.port)) {
				if (x.key.equals(dir)) {
					return true;
				} else if (x.key.contains(dir)) {
					return false;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private void _add(String s, ServiceInfo si) {
		si.key = s;
		map.put(s, si);
	}

	public static void main(String[] args) {
		FileMapper fm = FileMapper.getInstance();
		fm.add("/tmp/test", "0.0.0.0", 8085, ServiceInfo.ServiceType.FILE_SERVICE);
		fm.add("/tmp/lel", "0.0.0.0", 8081, ServiceInfo.ServiceType.FILE_SERVICE);
		fm.add("/tmp/test/lel2", "0.0.0.0", 8082, ServiceInfo.ServiceType.FILE_SERVICE);
		System.out.println("---------");
		for (ServiceInfo x : fm.getAllServiceList()) {
			System.out.println(x.getFullInfo());
		}
	}

	public ServiceInfo[] getAllServiceList() {
		Set<String> keySet = map.keySet();
		String[] keyArray = new String[keySet.size()];
		keySet.toArray(keyArray);
		ServiceInfo[] result = new ServiceInfo[keySet.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = map.get(keyArray[i]);
		}
		return result;
	}
}

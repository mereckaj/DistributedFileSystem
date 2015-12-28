public class ServiceInfo {

	public enum ServiceType {DIR_SERVICE, AUTH_SERVICE, FILE_SERVICE}


	public String ip;
	public int port;
	public ServiceType serviceInfo;
	public String key;

	public ServiceInfo(String ip, int port, ServiceType serviceInfo, String key) {
		this.ip = ip;
		this.port = port;
		this.serviceInfo = serviceInfo;
		this.key = key;
	}

	public String toString() {
		return ip + ":" + port;
	}

	public String getFullInfo() {
		return serviceInfo.name() + " " + this.toString() + " " + key;
	}
}

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServiceInfo {

	public enum ServiceType {DIR_SERVICE, AUTH_SERVICE, FILE_SERVICE, LOCK_SERVICE}


	public String ip;
	public int port;
	public ServiceType serviceInfo;
	public String key;

	public ServiceInfo(String ip, int port, ServiceType serviceInfo, String key) {
		if(ip.equals("0.0.0.0")){
			try {
				this.ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}else {
			this.ip = ip;
		}
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

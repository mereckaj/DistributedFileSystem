package com.mereckaj.dfs.directory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServiceInfo {
	public enum ServiceType {DIR_SERVICE,AUTH_SERVICE,FILE_SERVICE};

	public String ip;
	public int port;
	public ServiceType serviceInfo;

	public ServiceInfo(String hostname, int port, ServiceType serviceInfo, boolean ignore){
		try {
			InetAddress addr = InetAddress.getByName(hostname);
			ip = addr.getHostAddress();
			this.port =port;
			this.serviceInfo = serviceInfo;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	public ServiceInfo(String ip, int port, ServiceType serviceInfo) {
		this.ip = ip;
		this.port = port;
		this.serviceInfo = serviceInfo;
	}
}

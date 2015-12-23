package com.mereckaj.dfs.directory;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class FileMapper {
	private static final String rootDir = "~";
	private static FileMapper instance = new FileMapper();
	private ConcurrentHashMap<String,ServiceInfo> map = new ConcurrentHashMap<>();
	private FileMapper(){
		if(instance==null){
			populateMap();
		}
	}
	public static FileMapper getInstance(){
		return instance;
	}

	public boolean exists(String dir){
		dir = validate(dir);
		if(map.containsKey(dir)){
		}
		return false;
	}

	private String validate(String dir) {
		System.out.println("Validating: " + dir);
		dir = dir.trim();
		dir = dir.replace(" ","\\ ");
		if(dir.startsWith("/")){
			if(!dir.endsWith("/")){
				dir = dir + "/";
			}
		}else{
			return validate("/" + dir);
		}
		System.out.println("After Validation: " + dir);
		return dir;
	}

	public String get(String dir){
		return "";
	}
	public void add(String dir){
		if(!exists(dir)){

		}
	}

	private void populateMap(){
		map.put("/etc/",new ServiceInfo("0.0.0.0",8081, ServiceInfo.ServiceType.FILE_SERVICE));
		map.put("/var/",new ServiceInfo("0.0.0.0",8082, ServiceInfo.ServiceType.FILE_SERVICE));
		map.put("/tmp/",new ServiceInfo("0.0.0.0",8083, ServiceInfo.ServiceType.FILE_SERVICE));
	}
}

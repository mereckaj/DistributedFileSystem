package com.mereckaj.dfs.test;

import com.mereckaj.dfs.directory.DirServer;

/**
 * Created by mereckaj on 22/12/15 21:06.
 */
public class DirServerTest {
	public static void main(String[] args){
		DirServer ds = new DirServer(8080,"0.0.0.0",5);
		ds.run();
	}
}

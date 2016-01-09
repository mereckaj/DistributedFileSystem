
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileManager {
	private static FileManager instance = new FileManager();
	private String root = "dfs";
	private static final int MAX_FILE_SIZE = 4069 * 1024;
	private Lock lock;

	private FileManager() {
		if (instance == null) {
			lock = new ReentrantLock();
			if(!Files.exists(Paths.get(root))) {
				try {
					Files.createDirectory(Paths.get(root));
					System.out.println("Created a new root fs");
				} catch (IOException e) {
					System.out.println("Could not create dir" );
					e.printStackTrace();
					System.exit(1);
				}
			}else{
				System.out.println("Found root fs");
			}
		}
	}

	public static FileManager getInstance() {
		return instance;
	}

	public synchronized boolean write(String dir, String file, String data) {
		try {
			System.out.println("Writing: " +root + dir + file);
			lock.lock();
			File f = new File(root + dir + file);
			Files.createDirectories(Paths.get(root+dir+file).getParent());
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(Base64.decode(data));
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			lock.unlock();
		}
		return true;
	}

	public synchronized String read(String dir, String file) {
		String result=null;
		try {
			System.out.println("Reading: " + root + dir + file);
			lock.lock();
			File f = new File(root + dir + file);
			System.out.println("Opened file");
			byte[] data = new byte[(int) f.length()];
			System.out.println("Created buffer");
			FileInputStream fis = new FileInputStream(f);
			System.out.println("Created FIS");
			BufferedInputStream bis = new BufferedInputStream(fis);
			System.out.println("Created BIS");
			bis.read(data,0,data.length);
			System.out.println("Read in data");
			result = Base64.encode(data);
			System.out.println("Encoded data");
			bis.close();
			fis.close();
			System.out.println("Closed Streams");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return result;
	}
	public boolean exists(String dir, String file){
		System.out.println("Checking for file: " + root + dir + file);
		return new File(root + dir + file).exists();
	}


}

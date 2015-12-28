
import com.mereckaj.dfs.fileservice.FileServer;

public class FileServerTest {
	public static void main(String[] args){
		FileServer fs = new FileServer(8081,"0.0.0.0",5);
		fs.start();
		System.out.println("fs1 started");
		FileServer fs2 = new FileServer(8082,"188.166.36.191",5);
		fs2.start();
		System.out.println("fs2 started");
	}
}

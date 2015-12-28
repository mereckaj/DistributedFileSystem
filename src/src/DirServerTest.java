
import com.mereckaj.dfs.directory.DirServer;

public class DirServerTest {
	public static void main(String[] args) {
		DirServer ds = new DirServer(8080, "0.0.0.0", 5);
		ds.run();
	}
}

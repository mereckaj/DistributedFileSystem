import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ID {
	private String data;
	private String hash;
	private String hashBase64;
	private MessageDigest messageDigest;

	public ID(String ip, int port){
		data = ip+":"+port;
		try {
			messageDigest = MessageDigest.getInstance("SHA256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		hash = new String(messageDigest.digest(data.getBytes()));
		hashBase64 = DatatypeConverter.printBase64Binary(hash.getBytes());
	}

	public String getHash(){
		return hash;
	}

	public String getHashBase64(){
		return hashBase64;
	}

	public static String getHashFromBase64(String h){
		return new String(DatatypeConverter.parseBase64Binary(h));
	}


}

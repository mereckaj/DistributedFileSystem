import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

public class AuthServerWorker implements Runnable {
	private Socket socket;
	private InputStreamReader isr;
	private OutputStreamWriter osw;
	private MessageQueueWorkerThread msqw;
	private BufferedReader br;
	private boolean running;
	private static final int RECEIVE_BUFFER_SIZE = 65536;
	private static FileMapper fm = FileMapper.getInstance();
	private static SecureRandom secureRandom;
	private int taskID = 0;
	private static ConcurrentHashMap<String, SecretKey> keyStore = new ConcurrentHashMap<>();

	private static KeyGenerator keyGenerator;

	public AuthServerWorker(Socket s) {
		try {
			isr = new InputStreamReader(s.getInputStream());
			osw = new OutputStreamWriter(s.getOutputStream());
			msqw = new MessageQueueWorkerThread(osw);
			br = new BufferedReader(isr);
			running = true;
			socket = s;
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
//			preregisteredKeys();
			msqw.start();
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/*
	Add any keys that should be in the keystore at startup e.g TG,DIR,LOCK,FILE services
	 */
	private void preregisteredKeys() {
	}

	@Override
	public void run() {
		System.out.println("Started task: " + taskID);
		String m = readMessage();
		dealWithMessage(m);
		System.out.println("Finished task: " + taskID);
	}

	private void dealWithMessage(String message) {
		String[] commands;
		if (message != null && message.length() > 1) {
			commands = message.split("\n");
		} else {
			return;
		}
		try {
			if (commands[0].contains("AUTH_AS")) {
				doAuthASCommand(commands);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Invalid command: " + message);
		}
	}

	private void doAuthASCommand(String[] commands) {
		String clientID = AuthMessage._get(commands, "CLIENT_ID");
		String tgsID = AuthMessage._get(commands, "TGS_ID");

		SecretKey keyA = getKeyIfExists(clientID);
		SecretKey keyTGS = getKeyIfExists(tgsID);

		if (keyA == null) {
			keyA = keyGenerator.generateKey();
			keyStore.put(clientID, keyA);
		}
		if (keyTGS != null) {

			/*
			Prepare Client Data
			 */

			SecretKey keyAtoTGS = keyGenerator.generateKey();
			int t = secureRandom.nextInt();
			String message = tgsID + "\n"
					+ keyAtoTGS.getEncoded() + "\n"
					+ t + "\n";
			String clientData = encrypt(new String(keyA.getEncoded()), new String(keyA.getEncoded()), message);


			/*
			Prepare TGS data
			 */


		} else {
			System.out.println(">> NO SUCH TGS");
		}

	}

	private String readMessage() {
		String line = "";
		String response = "";
		try {
			while ((line = br.readLine()) != null) {
				response = response + line + "\n";
				if (!br.ready()) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	private SecretKey getKeyIfExists(String id) {
		if (keyStore.containsKey(id)) {
			return keyStore.get(id);
		}
		return null;
	}

	public static String encrypt(String key, String initVector, String value) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());

			return DatatypeConverter.printBase64Binary(encrypted);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static String decrypt(String key, String initVector, String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(DatatypeConverter.parseBase64Binary(encrypted));

			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) {
		String key = "Bar12345Bar12345"; // 128 bit key
		String initVector = "RandomInitVector"; // 16 bytes IV

		System.out.println(decrypt(key, initVector,
				encrypt(key, initVector, "Hello World")));
	}
}

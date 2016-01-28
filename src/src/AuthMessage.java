
public class AuthMessage {
	public static String prepareAUTH_AS(ID client, ID tgs) {
		return "AUTH_AS:\n" +
				"CLIENT_ID:" + client.getHashBase64() + "\n" +
				"TGS_ID:" + tgs.getHashBase64() + "\n";
	}

	public static String prepareAUTH_AS_RESPONSE(String clientData, String tgsData) {
		return "AUTH_AS_RESPONSE:\n" +
				"CLIENT_DATA:" + clientData + "\n" +
				"TGS_DATA:" + tgsData + "\n";
	}

	public static String prepareAUTH_TGS(ID target, String clientToTgsData, String tgsData) {
		return "AUTH_TGS:\n" +
				"TARGET_ID:" + target.getHashBase64() + "\n" +
				"CLIENT_TGS_DATA:" + clientToTgsData + "\n" +
				"TGS_DATA:" + tgsData + "\n";
	}

	public static String prepareAUTH_TGS_REPONSE(String clientData, String targetData) {
		return "AUTH_TGS_RESPONSE:\n" +
				"CLIENT_DATA:" + clientData + "\n" +
				"TARGET_DATA:" + targetData + "\n";
	}

	public static String prepareAUTH_TARGET(String sharedData, String targetData) {
		return "AUTH_TARGET:\n" +
				"DATA_SHARED:" + sharedData + "\n" +
				"TARGET_DATA:" + targetData + "\n";
	}

	public static String prepareAUTH_TARGET_RESPONSE(String data) {
		return "AUTH_TARGET_RESPONSE:\n" +
				"DATA:" + data + "\n";
	}

	public static String get(String data, String key) {
		String[] lines = data.split("\n");
		String result = null;
		return _get(lines,key);
	}

	public static String _get(String[] lines,String key) {
		for (String s : lines) {
			if (s.contains(key)) {
				return s.split(":")[1].trim();
			}
		}
		return null;
	}

}

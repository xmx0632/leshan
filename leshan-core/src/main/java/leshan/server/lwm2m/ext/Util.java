package leshan.server.lwm2m.ext;

public class Util {
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public   String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	public static void main(String[] args) {
		
		String hexStringUdp = "4002DDB436646F6D61696E82726411283665703D6E7463076C743D333630300C65743D506F7765724E6F646508643D646F6D61696EFF3C2F6E772F706970616464723E3B63743D2230223B72743D226E733A763661646472223B69663D22636F72652373222C3C2F6465762F6D66673E3B63743D2230223B72743D226970736F3A6465762D6D6667223B69663D22222C3C2F7077722F302F72656C3E3B6F62733B63743D2230223B72743D226970736F3A7077722D72656C223B69663D22222C3C2F6465762F6D646C3E3B63743D2230223B72743D226970736F3A6465762D6D646C223B69663D22222C3C2F6465762F6261743E3B6F62733B63743D2230223B72743D226970736F3A6465762D626174223B69663D22222C3C2F7077722F302F773E3B6F62733B63743D2230223B72743D226970736F3A7077722D77223B69663D22222C3C2F6E772F6970616464723E3B63743D2230223B72743D226E733A763661646472223B69663D22636F72652373222C3C2F73656E2F74656D703E3B6F62733B63743D2230223B72743D227563756D3A43656C223B69663D22222C3C2F6E772F65726970616464723E3B63743D2230223B72743D226E733A763661646472223B69663D22636F72652373222C3C2F6E772F70727373693E3B63743D2230223B72743D226E733A72737369223B69663D22636F7265237322";
		byte[] bytes = Util.hexStringToBytes(hexStringUdp);
		
		String newString = Util.bytesToHex(bytes);
		
		System.out.println(hexStringUdp);
		System.out.println(newString);
		
	}
}

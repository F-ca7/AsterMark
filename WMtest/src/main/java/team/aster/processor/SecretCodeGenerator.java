package team.aster.processor;
import java.util.Random;

/****
 * 生成MAC计算时，所需要的密钥Ks
 * 包含静态方法getSecretCode，生成字符串类型密钥
 * @author kun
 * 
 */
public class SecretCodeGenerator {
	/****
	 * CODES 默认的密钥字符选择范围
	 */
	private static char[] CODES=new char[]{'1','2','3','4','5','6','7','8','9','0',
			'q','w','e','r','t','y','u','i','o','p','a','s','d','f','g','h','j','k','l','z','x','c','v','b','n','m'};
	
	/****
	 * 根据默认的密钥字符选择范围，指定长度生成密钥
	 * @param length 指定的密钥长度
	 * @return 密钥
	 */
	static String getSecretCode(int length) {
		return getCodeString(length, CODES);
	}


	static String getSecretCode(String srcStr){
		// hashCode可能为负
		int n = Math.abs(srcStr.hashCode());
		int size = CODES.length;
		StringBuilder code= new StringBuilder();
		for (; n>0; n/=size){
			code.append(CODES[n%size]);
		}
		return code.toString();
	}

	private static String getCodeString(int length, char[] codes) {
		StringBuilder code= new StringBuilder();
		Random r = new Random();
		int index=0;
		int len= codes.length;
		for(int i=0;i<length;i++) {
			index = r.nextInt(len);
			code.append(codes[index]);
		}
		return code.toString();
	}

	/****
	 * 
	 * 根据指定的密钥字符选择范围，指定长度生成密钥
	 * @param codes 指定的密钥生成范围
	 * @param length 指定的密钥长度
	 * @return 密钥
	 */
	public static String getSecretCode(int length, char[] codes) {
		return getCodeString(length, codes);
	}

	public static double getSecretKey(String str){
		int len = str.length();
		double d = len%60;
		return d/100;
	}
}

package team.aster.processor;

import team.aster.model.WaterMark;
import team.aster.model.WatermarkException;
import team.aster.utils.BinaryUtils;

import java.util.ArrayList;
import java.util.Random;
/**
 * Get WaterMark
 * @author kun
 *
 */
class WaterMarkGenerator {

	private static final int MAX_TIMES = 1<<16;
	private static Random r = new Random();
	// 水印位数
	private static final int WATERMARK_SIZE = 16;

	// 越低越严格
	private static final double SAME_THRESHOLD = 0.7;


	private static ArrayList<Integer> toBinary(String string){
		char[] tmp = string.toCharArray();
		ArrayList<Integer> binary = new ArrayList<Integer>();
		for(int i=0;i<string.length();i++) {
			if(tmp[i]=='1') {
				binary.add(1);
			}else if(tmp[i]=='0'){
				binary.add(0);
			}
		}
		return binary;
	}


	private static String getRandomBinary() {
		StringBuffer watermark = new StringBuffer();
		for(int i = 0; i< WATERMARK_SIZE; i++) {
			if(r.nextBoolean()) {
				watermark.append('1');
			}else {
				watermark.append('0');
			}
		}
		return watermark.toString();
	}

	/**
	 * get resultdata from model which is the set of existed watermark information
	 * return WaterMark type
	 * if random time is more than MAX_TIMES, return null
	 * @param result dataset from watermark database
	 * @return
	 */
	public static WaterMark getWaterMark( ArrayList<String> result) throws WatermarkException {
		for(int new_secret=1; new_secret<MAX_TIMES; new_secret++){
			ArrayList<Integer> binary = new ArrayList<Integer>();
			int bits = WATERMARK_SIZE;
			int x = new_secret;
			while(bits!=0){
				int y = x%2;
				binary.add(y);
				x/=2;
				bits--;
			}
			if(result.isEmpty()) {
				return new WaterMark(WATERMARK_SIZE,  binary);
			}
			boolean ok = true;
			for(String existedString:result) {
				ArrayList<Integer> existedBinary = toBinary(existedString);
				if(BinaryUtils.getSimilarity(binary, existedBinary)>SAME_THRESHOLD) {
					ok=false;
					break;
				}
			}
			if(ok) {
				WaterMark w = new WaterMark(WATERMARK_SIZE, binary);
				return w;
			}
		}

		// 已有水印数量太多
		throw new WatermarkException("该数据表已经存在"+result.size()+"次水印嵌入");
	}

}

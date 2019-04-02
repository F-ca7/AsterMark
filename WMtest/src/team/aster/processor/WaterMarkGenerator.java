package team.aster.processor;

import team.aster.model.WaterMark;

import java.util.ArrayList;
import java.util.Random;
/**
 * Get WaterMark
 * @author kun
 *
 */
public class WaterMarkGenerator {
	private static int MAX_TIMES = 100;
	private static Random r = new Random();
	private static int WATERMARK_SIZE = 16;

	private static double SAME_THRESHOLD = 0.2;


	public static double getSimilarity(ArrayList<Integer> checkedBinary,ArrayList<Integer> existedBinary) {
		double length,sameNum=0;
		if((length=checkedBinary.size())!=existedBinary.size()) {
			return 0;
		}
		for(int i=0;i<length;i++) {
			if(checkedBinary.get(i)==existedBinary.get(i)) {
				sameNum++;
			}
		}
		return sameNum/length;
	}

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
	
	//Wrong!
//	public String toString(ArrayList<Integer> binary) {
//		StringBuffer string = new StringBuffer();
//		for(int i=0;i<binary.size();i++) {
//			if(binary.get(i)==1) {
//				string.append('1');
//			}else if(binary.get(i)==0) {
//				string.append('0');
//			}
//		}
//		return string.toString();
//	}

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
	public static WaterMark getWaterMark( ArrayList<String> result) {
		int times = 0;
		while(times < MAX_TIMES) {
			ArrayList<Integer> binary = new ArrayList<Integer>();
			for(int i=0; i<WATERMARK_SIZE; i++) {
				binary.add(r.nextInt(2));
			}
			if(result.isEmpty()) {
				return new WaterMark(WATERMARK_SIZE,  binary);
			}
			boolean ok = true;
			for(String existedString:result) {
				ArrayList<Integer> existedBinary = toBinary(existedString);
				if(getSimilarity(binary, existedBinary)>SAME_THRESHOLD) {
					ok=false;
					break;
				}
			}
			if(ok) {
				WaterMark w = new WaterMark(WATERMARK_SIZE, binary);
				return w;

				}
			times++;
		}
		return null;
	}

}

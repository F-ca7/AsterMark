package team.aster.model;

import java.util.ArrayList;
/**
 * define watermark type
 * @author kun
 * @修改人 fcat
 * @修改内容 移除origin
 */
public class WaterMark {

	private int length;


	//String origin;
	ArrayList<Integer> binary;

	/**
	 *
	 * @param length the length of watermark
	 * @param binary ArrayList<Integer> type watermark
	 */
	public WaterMark(int length, ArrayList<Integer> binary){
		this.length=length;
		this.binary = binary;
	}


	public int getLength() {
		return length;
	}

	public ArrayList<Integer> getBinary() {
		return binary;
	}
}

package team.aster.utils;

import java.util.ArrayList;

public class BinaryUtils {
    private BinaryUtils(){}

    public static String parseBinaryToString(ArrayList<Integer> binary){
        StringBuffer string = new StringBuffer();
        for (Integer integer : binary) {
            if (integer == 1) {
                string.append('1');
            } else if (integer == 0) {
                string.append('0');
            }
        }
        return string.toString();
    }


    public static double getSimilarity(ArrayList<Integer> checkedBinary,ArrayList<Integer> existedBinary) {
        double length,sameNum=0;
        if((length=checkedBinary.size())!=existedBinary.size()) {
            //todo 用异常取代
            return 0;
        }
        for(int i=0;i<length;i++) {
            if(checkedBinary.get(i)==existedBinary.get(i)) {
                sameNum++;
            }
        }
        return sameNum/length;
    }

    public static double getStringSimilarity(String checkedBinary,String existedBinary) {
        if (checkedBinary.length() != existedBinary.length()){
            return -1;
        }
        double length = checkedBinary.length(), sameNum = 0;
        char[] checkedChars = checkedBinary.toCharArray();
        char[] existedChars = existedBinary.toCharArray();
        for (int i=0; i<checkedChars.length; i++){
            if (checkedChars[i] == existedChars[i]){
                sameNum++;
            }
        }
        return sameNum/length;
    }


}

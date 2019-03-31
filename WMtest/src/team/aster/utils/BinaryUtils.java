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
}

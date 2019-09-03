package team.aster.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.algorithm.Divider;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.StoredKey;
import team.aster.utils.Constants;

import java.util.ArrayList;
import java.util.Map;

public class SpaceDecoder implements IDecoder {
    private static Logger logger = LoggerFactory.getLogger(SpaceDecoder.class);
    private int partitionCount;
    private int wmLength;
    private String secretCode;
    private int minLength;
    // todo 这个地方应该自定义
    private static final int COL_INDEX = Constants.EmbedDbInfo.EMBED_COL_INDEX-1;

    @Override
    public String decode(DatasetWithPK datasetWithPK) {
        String decodedWatermark;
        logger.info("解码使用的元组数有 {}", datasetWithPK.getDataset().size());
        decodedWatermark = detectWatermark(Divider.divide(partitionCount, datasetWithPK, secretCode));
        logger.info("解码出来的水印为: {}" , decodedWatermark);
        return decodedWatermark;
    }

    @Override
    public void setStoredKeyParams(StoredKey storedKey) {
        setMinLength(storedKey.getMinLength());
        setPartitionCount(storedKey.getPartitionCount());
        setSecretCode(storedKey.getSecretCode());
        setWmLength(storedKey.getWmLength());
        logger.debug("使用secretCode为 {}", secretCode);
    }



    private String detectWatermark(PartitionedDataset partitionedDataset) {
        int[] ones = new int[wmLength];
        int[] zeros = new int[wmLength];

        Map<Integer, ArrayList<ArrayList<String>>> map = partitionedDataset.getPartitionedDataset();
        map.forEach((k, v)->{
            if(v.size() >= minLength){
                ArrayList<String> colValues = new ArrayList<>();
                int index = k%wmLength;
                v.forEach(strValues->{
                    colValues.add(strValues.get(COL_INDEX));
                });
                if(decodeSpace(colValues)) {
                    ones[index]++;
                }else {
                    zeros[index]++;
                }
            }
        });

        StringBuilder wm = new StringBuilder();
        for(int i=0;i<wmLength;i++){
            logger.debug("第{}位 0的个数{}，1的个数{}", i,zeros[i],ones[i]);
            if(ones[i]>zeros[i]){
                wm.append("1");
            }else if(ones[i]<zeros[i]){
                wm.append("0");
            }else{
                wm.append("x");
            }
        }

        return wm.toString();
    }


    private boolean decodeSpace(ArrayList<String> colValues) {
        int zeros=0;
        int len=colValues.size();
        for(String s:colValues) {
            StringBuffer tmp = new StringBuffer(s);
            int front = getFrontSpace(tmp);
            int back = getBackSpace(tmp);
            if(front==2 && back%2==0)
                zeros++;
        }
        if(zeros>len/2) {
            return false;
        }else
            return true;
    }

    private int getFrontSpace(StringBuffer s) {
        int cnt=0;
        int len = s.length();
        for(int i=0;i<len;i++) {
            if(s.charAt(i)!=' ') break;
            cnt++;
        }
        return cnt;
    }

    private int getBackSpace(StringBuffer s) {
        int cnt=0;
        int len = s.length();
        for(int i=len-1;i>=0;i--) {
            if(s.charAt(i)!=' ') break;
            cnt++;
        }
        return cnt;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public void setWmLength(int wmLength) {
        this.wmLength = wmLength;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }


}

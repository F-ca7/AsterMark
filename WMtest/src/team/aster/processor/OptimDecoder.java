package team.aster.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.algorithm.Divider;
import team.aster.algorithm.GenericOptimization;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.StoredKey;
import team.aster.utils.Constants;

import java.util.ArrayList;
import java.util.Map;

public class OptimDecoder implements IDecoder {
    private static Logger logger = LoggerFactory.getLogger(OptimDecoder.class);

    //这些变量是通过数据库读入的
    private int partitionCount;
    private int wmLength;
    private double threshold;
    private double secretKey;
    private String secretCode;
    private int minLength;
    //先只对一列进行嵌入水印解码，这里是最后一列FLATLOSE 转让盈亏(已扣税)
    //但是由于列之间的约束，这里还是不太科学
    // todo 这个地方应该自定义
    private static final int COL_INDEX = Constants.EmbedDbInfo.EMBED_COL_INDEX-1;

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public void setWmLength(int wmLength) {
        this.wmLength = wmLength;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setSecretKey(double secretKey) {
        this.secretKey = secretKey;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }


    @Override
    public void setStoredKeyParams(StoredKey storedKey){
        setMinLength(storedKey.getMinLength());
        setPartitionCount(storedKey.getPartitionCount());
        setSecretCode(storedKey.getSecretCode());
        setThreshold(storedKey.getThreshold());
        setSecretKey(storedKey.getSecretKey());
        setWmLength(storedKey.getWmLength());
        logger.debug("使用secretCode为 {}", secretCode);
    }

    @Override
    public String decode(DatasetWithPK datasetWithPK) {
        String decodedWatermark;
        logger.info("解码使用的元组数有 {}", datasetWithPK.getDataset().size());
        decodedWatermark = detectWatermark(Divider.divide(partitionCount, datasetWithPK, secretCode));
        logger.info("解码出来的水印为: {}" , decodedWatermark);
        return decodedWatermark;
    }


    private String detectWatermark(PartitionedDataset partitionedDataset){
        int[] ones = new int[wmLength];
        int[] zeros = new int[wmLength];
        Map<Integer, ArrayList<ArrayList<String>>> map = partitionedDataset.getPartitionedDataset();
        map.forEach((k, v)->{
            if(v.size() >= minLength){
                ArrayList<Double> colValues = new ArrayList<>();
                int index = k%wmLength;
                v.forEach(strValues->{
                    colValues.add(Double.parseDouble(strValues.get(COL_INDEX)));
                });
                double hidingValue = GenericOptimization.getOHidingValue(colValues, secretKey);
                if (hidingValue > this.threshold) {
                    ones[index]++;
                } else{
                    zeros[index]++;
                }
            }
        });

        //据ones和zeros生成水印
        StringBuilder wm = new StringBuilder();
        for(int i=0;i<wmLength;i++){
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
}

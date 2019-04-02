package team.aster.processor;

import team.aster.algorithm.GenericOptimization;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.StoredKey;

import java.util.ArrayList;
import java.util.Map;

public class OptimDecoder implements IDecoder {
    //这些变量是通过数据库读入的
    private int partitionCount;
    private int wmLength;
    private double threshold;
    private double secretKey;
    private String secretCode;
    private int minLength;
    //先只对一列进行嵌入水印解码，这里是最后一列FLATLOSE 转让盈亏(已扣税)
    //但是由于列之间的约束，这里还是不太科学
    private static final int COL_INDEX = 14;

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

    public void setStoredKeyParams(StoredKey storedKey){
        setMinLength(storedKey.getMinLength());
        setPartitionCount(storedKey.getPartitionCount());
        setSecretCode(storedKey.getSecretCode());
        setThreshold(storedKey.getThreshold());
        setSecretKey(storedKey.getSecretKey());
        setWmLength(storedKey.getWmLength());
    }

    @Override
    public String decode(DatasetWithPK datasetWithPK) {
        String decodedWatermark;
        decodedWatermark = detectWatermark(Divider.divide(partitionCount, datasetWithPK, secretCode));
        System.out.println("解码出来的水印为：" + decodedWatermark);
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
                double hidingValue = GenericOptimization.getHidingValue(colValues, secretKey);
                if (hidingValue >= this.threshold) {
                    ones[index]++;
                } else{
                    zeros[index]++;
                }
            }
        });

        //据ones和zeros生成水印
        StringBuffer wm = new StringBuffer();
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

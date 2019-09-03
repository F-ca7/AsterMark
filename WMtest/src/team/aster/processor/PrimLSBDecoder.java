package team.aster.processor;

import team.aster.algorithm.Divider;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.StoredKey;
import team.aster.utils.Constants;

import java.util.ArrayList;
import java.util.Map;

public class PrimLSBDecoder implements IDecoder {

    private String secretCode;
    private int partitionCount;
    private int wmLength;
    private final int COL_INDEX = Constants.EmbedDbInfo.EMBED_COL_INDEX-1;
    private final int PK_INDEX = Constants.EmbedDbInfo.PK_COL_INDEX-1;
    private DatasetWithPK originDatasetWithPK;

    @Override
    public void setStoredKeyParams(StoredKey storedKey){
        setPartitionCount(storedKey.getPartitionCount());
        setSecretCode(storedKey.getSecretCode());
        setWmLength(storedKey.getWmLength());
    }

    @Override
    public String decode(DatasetWithPK datasetWithPK) {
        String decodedWatermark;
        decodedWatermark = detectWatermark(Divider.divide(partitionCount, datasetWithPK, secretCode));
        System.out.println("解码出来的水印为：" + decodedWatermark);
        return decodedWatermark;
    }

    private String detectWatermark(PartitionedDataset partitionedDataset) {
        int[] ones = new int[wmLength];
        int[] zeros = new int[wmLength];

        Map<Integer, ArrayList<ArrayList<String>>> map = partitionedDataset.getPartitionedDataset();
        Map<Integer, ArrayList<ArrayList<String>>> originMap = Divider.divide(partitionCount, originDatasetWithPK, secretCode).getPartitionedDataset();
        map.forEach((k, v)->{
            int index = k%wmLength;
            //对应原数据集的划分
            ArrayList<ArrayList<String>> originV  = originMap.get(k);
            v.forEach(strValues->{
                String PK = strValues.get(PK_INDEX);
                //todo 问题很大 必改
                for (ArrayList<String> strings : originV) {
                    if (PK.equals(strings.get(PK_INDEX))) {
                        double originValue = Double.parseDouble(strings.get(COL_INDEX));
                        double curValue = Double.parseDouble(strValues.get(COL_INDEX));

                        if (originValue < curValue) {
                            //System.out.printf("原数据为%f, 现在为%f, ones++", originValue, curValue);
                            ones[index]++;
                        } else {
                            //System.out.printf("原数据为%f, 现在为%f, zeros++", originValue, curValue);
                            zeros[index]++;
                        }
                    }
                }

            });
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

    PrimLSBDecoder setSecretCode(String secretCode) {
        this.secretCode = secretCode;
        return this;
    }

    PrimLSBDecoder setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
        return this;
    }

    PrimLSBDecoder setWmLength(int wmLength) {
        this.wmLength = wmLength;
        return this;
    }

    public PrimLSBDecoder setOriginDatasetWithPK(DatasetWithPK originDatasetWithPK) {
        this.originDatasetWithPK = originDatasetWithPK;
        return this;
    }
}

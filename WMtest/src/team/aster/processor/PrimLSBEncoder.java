package team.aster.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.database.SecretKeyDbController;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.StoredKey;
import team.aster.model.WaterMark;
import team.aster.utils.BinaryUtils;
import team.aster.utils.Constants;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PrimLSBEncoder extends IEncoderNumericImpl {
    private static Logger logger = LoggerFactory.getLogger(PrimLSBDecoder.class);

    private static final int COL_INDEX = Constants.EmbedDbInfo.EMBED_COL_INDEX-1;
    private static final int PARTITION_COUNT = Constants.EmbedDbInfo.PARTITION_COUNT;
    private static final double VAR_BOUND_RATIO = 0.1;     //一个数值的最大变化比例
    private static final double MIN_VAR = 0.01;            //一个数值的最小变化量

    @Override
    public void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList) {
        logger.info(this.toString()+"开始工作");
        String secreteCode = SecretCodeGenerator.getSecretCode(10);
        //对datasetWithPK进行划分
        PartitionedDataset partitionedDataset = Divider.divide(PARTITION_COUNT, datasetWithPK, secreteCode);

        //生成水印
        WaterMark waterMark = WaterMarkGenerator.getWaterMark(watermarkList);

        assert waterMark != null;
        encodeAllBits(partitionedDataset, waterMark.getBinary());
        logger.info("嵌入水印为 " + BinaryUtils.parseBinaryToString(waterMark.getBinary()));

        // 补充完秘钥信息
        completeStoredKey(secreteCode, 0, waterMark);
        StoredKey storedKey = storedKeyBuilder.build();
        // 保存水印信息
        SecretKeyDbController.getInstance().saveStoredKeysToDB(storedKey);

        //更新数据
        Map<String, ArrayList<String>> ds = datasetWithPK.getDataset();
        //先清空原来的datasetWithPK
        ds.clear();
        //把partitionedDataset更新回datasetWithPK，主键为每行的第一列(id)
        for(ArrayList<ArrayList<String>> rowSet: partitionedDataset.getPartitionedDataset().values()){
            for (ArrayList<String> row : rowSet){
                ds.put(row.get(0), row);
            }
        }
    }

    private void encodeAllBits(PartitionedDataset partitionedDataset, ArrayList<Integer> watermark) {
        logger.info("开始嵌入水印所有位");
        Map<Integer, ArrayList<ArrayList<String>>> datasetWithIndex = partitionedDataset.getPartitionedDataset();
        int wmLength = watermark.size();
        datasetWithIndex.forEach((k,v)->{
            int index = k%wmLength;
            //System.out.printf("正在处理第%d个划分...\n嵌入水印位为第%d位\n", k, index);
            encodeSingleBit(v, index, watermark.get(index));
        });
    }

    private void encodeSingleBit(ArrayList<ArrayList<String>> partition, int bitIndex, int bit) {
        ArrayList<Double> colValues = new ArrayList<>();
        for(ArrayList<String> row: partition){
            double value = Double.valueOf(row.get(COL_INDEX));
            colValues.add(value);
        }
        double currentValue = 0;
        double varBound = 0;
        double change = 0;
        Random random = new Random();
        //0则减少原来的数，1则增大原来的数
        switch (bit){
            case 0:
                for (int i=0; i<colValues.size(); i++){
                    currentValue = colValues.get(i);
                    varBound = VAR_BOUND_RATIO * currentValue;
                    change = random.nextDouble()*varBound;
                    if (change < MIN_VAR){
                        change = MIN_VAR;
                    }
                    colValues.set(i, colValues.get(i)-change);
                }
                break;
            case 1:
                for (int i=0; i<colValues.size(); i++){
                    currentValue = colValues.get(i);
                    varBound = VAR_BOUND_RATIO * currentValue;
                    change = random.nextDouble()*varBound;
                    if (change < MIN_VAR){
                        change = MIN_VAR;
                    }
                    colValues.set(i, colValues.get(i)+change);
                }
                break;
            default:
                logger.error("水印出错! ");
                break;
        }

        //写回partition
        int rowIndex = 0;
        String resultStr;
        for(ArrayList<String> row: partition){
            resultStr = String.format("%.2f", colValues.get(rowIndex));
            //System.out.println("将原来的" + row.get(COL_INDEX )+ "改为" + resultStr);
            row.set(COL_INDEX, resultStr);
            rowIndex++;
        }
    }


}

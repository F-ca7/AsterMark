package team.aster.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.algorithm.Divider;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.WaterMark;
import team.aster.model.WatermarkException;
import team.aster.utils.Constants;

import java.util.ArrayList;
import java.util.Map;

public class PunctuationEncoder extends IEncoderTextImpl{
    private static Logger logger = LoggerFactory.getLogger(PunctuationEncoder.class);

    private int COL_INDEX;
    private int PK_INDEX;

    private int PARTITION_COUNT;
    @Override
    public void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList) throws WatermarkException {
        logger.debug("{} 开始工作", this.toString());

        // 根据元组数设置划分数
        PARTITION_COUNT = datasetWithPK.getDataset().size()/100;
        //根据数据库表名生成secretCode
        String secreteCode = SecretCodeGenerator.getSecretCode(storedKeyBuilder.getDbTable());
        logger.debug("secreteCode为 {}", secreteCode);
        //对datasetWithPK进行划分
        PartitionedDataset partitionedDataset = Divider.divide(PARTITION_COUNT, datasetWithPK, secreteCode);

        logger.debug("预期划分数为{}，实际划分数为{}", PARTITION_COUNT, partitionedDataset.getPartitionedDataset().keySet().size());

        //生成水印
        WaterMark waterMark = WaterMarkGenerator.getWaterMark(watermarkList);
        //嵌入水印所有位
        assert waterMark != null;
        encodeAllBits(partitionedDataset, waterMark.getBinary());

        completeStoredKey(PARTITION_COUNT,secreteCode, waterMark);

//        StoredKey storedKey = storedKeyBuilder.build();
//        // 保存水印信息
//        SecretKeyDbController.getInstance().saveStoredKeysToDB(storedKey);
//        logger.info("秘钥信息为 {}", storedKey.toString());

    }




    private void encodeAllBits(PartitionedDataset partitionedDataset, ArrayList<Integer> watermark){
        logger.debug("开始嵌入水印所有位");
        Map<Integer, ArrayList<ArrayList<String>>> datasetWithIndex = partitionedDataset.getPartitionedDataset();

        final int wmLength = watermark.size();
        datasetWithIndex.forEach((k,v)->{
            int index = k%wmLength;
            encodeSingleBit(v, index, watermark.get(index));
        });

    }

    private void encodeSingleBit(ArrayList<ArrayList<String>> partition, int bitIndex, int bit) {
        for (ArrayList<String> row : partition) {
            switch (bit) {
                case 0:
                    row.set(COL_INDEX, getExecutedString(row.get(COL_INDEX), true));
                    break;
                case 1:
                    row.set(COL_INDEX, getExecutedString(row.get(COL_INDEX), false));
                    break;
            }
        }
    }


    @Override
    public void setPKIndex(int pkIndex) {
        PK_INDEX = pkIndex;
    }

    @Override
    public void setEmbedColIndex(int embedColIndex) {
        COL_INDEX = embedColIndex;
    }

    static boolean isEnPunc(char ch){
        if (0x21 <= ch && ch <= 0x22) return true;
        if (ch == 0x27 || ch == 0x2C) return true;
        if (ch == 0x2E || ch == 0x3A) return true;
        if (ch == 0x3B || ch == 0x3F) return true;

        return false;
    }
    static boolean isCjkPunc(char ch){
        if (0x3001 <= ch && ch <= 0x3003) return true;
        if (0x301D <= ch && ch <= 0x301F) return true;

        return false;
    }
    static boolean isPunctuation(char ch){
        if(isCjkPunc(ch)) return true;
        if(isEnPunc(ch)) return true;

        if(0x2018 <= ch && ch <= 0x201F) return true;
        if(ch == 0xFF01 || ch == 0xFF02) return true;
        if(ch == 0xFF07 || ch == 0xFF0C) return true;
        if(ch == 0xFF1A || ch == 0xFF1B) return true;
        if(ch == 0xFF1F || ch == 0xFF61) return true;
        if(ch == 0xFF0E) return true;
        if(ch == 0xFF65) return true;

        return false;
    }


    private String getExecutedString(String origin, boolean isZero) {
        StringBuffer origin_b = new StringBuffer(origin);
        int len = origin_b.length();
        int cnt=0;
        boolean isPunc = true;
        for(int i=0;i<len;i++) {
            //count the sum of punctuation.
            if(isPunctuation(origin_b.charAt(i))) {
                cnt++;
                isPunc = true;
            }else {
                isPunc = false;
            }
        }
        if(isPunc) {
            if((isZero&&cnt%2==1) || (!isZero&&cnt%2==0)) {
                origin_b.deleteCharAt(len-1);
            }
        }else {
            if((isZero&&cnt%2==1) || (!isZero&&cnt%2==0)) {
                origin_b.append('.');
            }
        }
        return origin_b.toString();
    }

}

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

public class SpaceEncoder extends IEncoderTextImpl {
    private static Logger logger = LoggerFactory.getLogger(SpaceEncoder.class);

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

        completeStoredKey(PARTITION_COUNT, secreteCode, waterMark);

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


    private String getExecutedString(String origin, boolean isZero) {
        StringBuffer origin_b = new StringBuffer(origin);

        if(isZero) {
            logger.debug("嵌入0");
            // 嵌入位为0
            // 句前2空格, 句后2空格
            origin_b.insert(0,"  ");
            origin_b.append("  ");
        }else {
            // 嵌入位为1
            // 句前4空格, 句后1空格
            logger.debug("嵌入1");
            origin_b.insert(0,"    ");
            origin_b.append(" ");
        }
        logger.debug("原来为 {}", origin);
        logger.debug("修改为 {}", origin_b.toString());
        return origin_b.toString();
    }


    @Override
    public void setPKIndex(int pkIndex) {
        PK_INDEX = pkIndex;
    }

    @Override
    public void setEmbedColIndex(int embedColIndex) {
        COL_INDEX = embedColIndex;
    }
}

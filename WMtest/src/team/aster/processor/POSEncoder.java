package team.aster.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.algorithm.Divider;
import team.aster.database.SecretKeyDbController;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.StoredKey;
import team.aster.model.WaterMark;
import team.aster.utils.Constants;

import java.util.ArrayList;
import java.util.Map;

import static team.aster.algorithm.POSInversionAlgorithm.encodeTextByPOS;

/**
 * 基于NLP词性逆序数的
 * 文本嵌入
 */
public class POSEncoder extends IEncoderTextImpl {
    private static Logger logger = LoggerFactory.getLogger(POSEncoder.class);

    private static final int COL_INDEX = Constants.EmbedDbInfo.EMBED_COL_INDEX-1;
    private static final int PARTITION_COUNT = Constants.EmbedDbInfo.PARTITION_COUNT;
    @Override
    public void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList) {
        logger.debug("{} 开始工作", this.toString());

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

        completeStoredKey(secreteCode, waterMark);

        StoredKey storedKey = storedKeyBuilder.build();
        // 保存水印信息
        SecretKeyDbController.getInstance().saveStoredKeysToDB(storedKey);
        logger.info("秘钥信息为 {}", storedKey.toString());
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
                    row.set(COL_INDEX, encodeTextByPOS(row.get(COL_INDEX), 0));
                    break;
                case 1:
                    row.set(COL_INDEX, encodeTextByPOS(row.get(COL_INDEX), 1));
                    break;
            }
        }

    }


}

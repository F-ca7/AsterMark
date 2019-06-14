package team.aster;

import sun.misc.FpUtils;
import team.aster.database.SubDbController;
import team.aster.model.AbstractInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.database.MainDbController;
import team.aster.model.DatasetWithPK;
import team.aster.model.StoredKey;
import team.aster.model.TargetSimilarity;
import team.aster.processor.*;
import team.aster.utils.Constants.EmbedDbInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PriorityQueue;

public class AbstractingExecutor {
    // 日志输出
    private static Logger logger = LoggerFactory.getLogger(AbstractingExecutor.class);
    // 提取信息
    private AbstractInfo abstractInfo;

    // 源（主）数据库
    private MainDbController mainDbController;
    // 源数据表名
    private String originTableName;
    // 目标相似度优先队列
    private PriorityQueue<TargetSimilarity> similarityPriorityQueue;

    // 解出来的水印
    String resultWatermark;
    //记录过程耗时
    private static long startTime;
    private static long endTime;

    public AbstractingExecutor(AbstractInfo abstractInfo) {
        this.abstractInfo = abstractInfo;
    }

    public void startAbstracting(MainDbController mainDbController, SubDbController subDbController){
        // 供原始数据对比使用
        this.mainDbController = mainDbController;
        this.originTableName = getOriginTableName(abstractInfo.getSourceDataTable());


        //获取对应的各个秘钥、秘参
        StoredKey storedKey = mainDbController.getStoredKeyByDbTable(abstractInfo.getSourceDataTable());
        // 取得主键所在的列
        subDbController.setPK(abstractInfo.getKeyColumnName());
        mainDbController.setPK_COL(subDbController.getPK_COL());
        // 设置嵌入列的位置
        int colIndex = subDbController.setEmbeddingCol(abstractInfo.getEmbeddingColumnName());

        // 初始化带主键的数据集
        subDbController.initDatasetWithPK();

        logger.info("开始初始化密钥配置...");
        IDecoder decoder = parseDecoder();

        DatasetWithPK datasetWithPK = subDbController.getDatasetWithPK();
        assert decoder != null;
        decoder.setStoredKeyParams(storedKey);
        decoder.setEmbedColIndex(colIndex-1);
        logger.info("密钥配置为 {}", storedKey);
        logger.info("初始化密钥配置完毕，开始解码...");

        startTime = System.currentTimeMillis();

        resultWatermark = decoder.decode(datasetWithPK);
        endTime = System.currentTimeMillis();
        logger.info("解码水印耗时：{} ms", (endTime - startTime));

        logger.info("解得水印位：{} ", (resultWatermark));
        similarityPriorityQueue = mainDbController.getTargetSimilarityRank(abstractInfo.getSourceDataTable(), resultWatermark);
        for (TargetSimilarity similarity:similarityPriorityQueue){
            logger.info("{}", similarity);
        }
    }

    /**
     * 形式如 数据库::表名
     * @return 表名
     */
    private String getOriginTableName(String dbTable) {
        String[] strs = dbTable.split("::");
        if (strs.length < 2) {
            return dbTable;
        }
        return strs[1];
    }

    /**
     * 根据提取信息构造对应解码器
     * @return 对应类型的解码器
     */
    private IDecoder parseDecoder() {
        switch (abstractInfo.getEmbeddingMethod()) {
            case EmbedDbInfo.NUMERIC_METHOD_LSB:
                PrimLSBDecoder primLSBDecoder = new PrimLSBDecoder();
                mainDbController.setTableName(originTableName);
                mainDbController.initDatasetWithPK();
                primLSBDecoder.setOriginDatasetWithPK(mainDbController.getDatasetWithPK());
                return primLSBDecoder;
            case EmbedDbInfo.NUMERIC_METHOD_PATTERN_SEARCH:
                return new OptimDecoder();
            case EmbedDbInfo.TEXT_METHOD_SPACE:
                return new SpaceDecoder();
            case EmbedDbInfo.TEXT_METHOD_PUNCTUATION:
                return new PunctuationDecoder();
            case EmbedDbInfo.TEXT_METHOD_POS:
                return new POSDecoder();
            default:
                return null;
        }
    }


    public PriorityQueue<TargetSimilarity> getSimilarityPriorityQueue() {
        return similarityPriorityQueue;
    }

    public boolean publishDecodingResultToFile(String path){
        boolean success = true;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");//设置日期格式
        String timestamp = df.format(new Date());// new Date()为获取当前系统时间
        String filepath = path + "\\水印提取结果 "+timestamp+".txt";
        File file = new File(filepath);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            success =false;
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("水印提取结果如下:\r\n");
            fileWriter.write("解得水印为: "+resultWatermark+"\r\n");
            fileWriter.write("溯源结果如下:\r\n");
            int i=1;
            for (TargetSimilarity targetSimilarity:similarityPriorityQueue){
                fileWriter.write(i+". ");
                fileWriter.write("目标信息: "+targetSimilarity.getTargetInfo()+"\t\t");
                fileWriter.write("相似度: "+targetSimilarity.getSimilarity());
                fileWriter.write("\r\n");
                ++i;
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            success =false;
        }

        return success;
    }
}

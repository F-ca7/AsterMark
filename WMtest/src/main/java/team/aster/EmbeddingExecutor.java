package team.aster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.database.MainDbController;
import team.aster.model.*;
import team.aster.processor.*;

import static team.aster.processor.SecretCodeGenerator.getSecretKey;
import static team.aster.utils.Constants.*;

/**
 * 执行嵌入操作的类
 * 与gui进行交互
 */
public class EmbeddingExecutor {
    // 日志输出
    private static Logger logger = LoggerFactory.getLogger(EmbeddingExecutor.class);
    // 嵌入信息
    private EmbeddingInfo embeddingInfo;

    // 是否为数值型

    // 原统计量
    private double preMean;
    private double preVar;
    // 新统计量
    private double newMean;
    private double newVar;

    // 记录过程耗时
    private static long startTime;
    private static long endTime;

    public EmbeddingExecutor(EmbeddingInfo embeddingInfo) {
        this.embeddingInfo = embeddingInfo;
    }

    public void startEmbedding(MainDbController mainDbController) throws WatermarkException {
        if (embeddingInfo==null){
            return;
        }
        ColumnDataConstraint dataConstraint = parseDataConstraint();
        //boolean isNumeric = dataConstraint.getConstraintType().isNumeric();
        // 取得主键所在的列
        int pkIndex = mainDbController.setPK(embeddingInfo.getKeyColumnName());
        // 初始化带主键的数据集
        mainDbController.initDatasetWithPK();
        // 取到嵌入列的位置
        int embedIndex = mainDbController.setEmbeddingCol(embeddingInfo.getEmbeddingColumnName());

        // 获取嵌入前统计量
        if (dataConstraint!=null){
            setPreStatistics(mainDbController);
        }


        IEncoder encoder = parseEncoder();
        // 生成密参信息
        StoredKey.Builder storedKeyBuilder = new StoredKey.Builder().setTarget(embeddingInfo.getEmbeddingMessage())
                .setMinLength(StoredKeyDbInfo.PARTITION_MIN_LENGTH).setDbTable(mainDbController.getDbTableName())
                .setSecretKey(getSecretKey(mainDbController.getDbTableName()));

        logger.info("开始嵌入水印...");


        startTime = System.currentTimeMillis();
        // 向带有主键的数据嵌入水印
        assert encoder != null;
        encoder.setDataConstraint(dataConstraint);
        encoder.setEmbedColIndex(embedIndex-1);
        encoder.setPKIndex(pkIndex-1);
        encoder.setStoredKeyBuilder(storedKeyBuilder);
        encoder.encode(mainDbController.getDatasetWithPK(), mainDbController.getWatermarkListByDbTable());
        endTime = System.currentTimeMillis();

        // 保存密参信息到数据库
        StoredKey storedKey = encoder.getStoredKey();
        mainDbController.saveStoredKeysToDB(storedKey);

        // 获取嵌入后统计量
        if (dataConstraint!=null){
            setNewStatistics(mainDbController);
        }

        logger.info("嵌入水印完成");
        logger.info("嵌入水印耗时：{} ms", (endTime - startTime));
    }

    private void setNewStatistics(MainDbController mainDbController) {
        newMean = mainDbController.getEmbeddingColStats().getMean();
        newVar = mainDbController.getEmbeddingColStats().getVariance();
    }

    private void setPreStatistics(MainDbController mainDbController) {
        preMean = mainDbController.getEmbeddingColStats().getMean();
        preVar = mainDbController.getEmbeddingColStats().getVariance();
    }


    private ColumnDataConstraint parseDataConstraint() {
        switch (embeddingInfo.getDataType()){
            case EmbedDbInfo.DATATYPE_INTEGER:
                return new ColumnDataConstraint(ConstraintType.INTEGER,
                        embeddingInfo.getLowerBound(), embeddingInfo.getUpperBound(), embeddingInfo.getPrecision());
            case EmbedDbInfo.DATATYPE_FLOAT:
                return new ColumnDataConstraint(ConstraintType.DOUBLE,
                        embeddingInfo.getLowerBound(), embeddingInfo.getUpperBound(), embeddingInfo.getPrecision());
            case EmbedDbInfo.DATATYPE_TEXT:
            default:
                    return null;
        }
    }

    /**
     * 提取嵌入信息中的encoder类型
     * @return
     */
    private IEncoder parseEncoder(){
        switch (embeddingInfo.getEmbeddingMethod()){
            case EmbedDbInfo.NUMERIC_METHOD_LSB:
                return new PrimLSBEncoder();
            case EmbedDbInfo.NUMERIC_METHOD_PATTERN_SEARCH:
                return new OptimEncoder();
            case EmbedDbInfo.TEXT_METHOD_SPACE:
                return new SpaceEncoder();
            case EmbedDbInfo.TEXT_METHOD_PUNCTUATION:
                return new PunctuationEncoder();
            case EmbedDbInfo.TEXT_METHOD_POS:
                return new POSEncoder();
            default:
                return null;
        }
    }

    public double getPreMean() {
        return preMean;
    }

    public double getPreVar() {
        return preVar;
    }

    public double getNewMean() {
        return newMean;
    }

    public double getNewVar() {
        return newVar;
    }
}

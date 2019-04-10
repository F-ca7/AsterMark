package team.aster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.database.MainDbController;
import team.aster.database.SecretKeyDbController;
import team.aster.model.ColumnDataConstraint;
import team.aster.model.ConstraintType;
import team.aster.model.DatasetWithPK;
import team.aster.model.StoredKey;
import team.aster.processor.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static team.aster.utils.Constants.*;

enum Attack {
    INSERTION,
    DELETION,
    ALTERNATION
}

public class Simulator {
    //日志输出
    private static Logger logger = LoggerFactory.getLogger(Simulator.class);

    //配置信息
    private final static String DB_NAME = MysqlDbConfig.EMBED_DB_NAME;
    private final static String EMBED_TABLE_NAME = EmbedDbInfo.EMBED_TABLE_NAME;
    private final static String PUBLISHED_TABLE_NAME = PublishDbInfo.PUBLISH_TABLE_NAME;
    private final static int FETCH_COUNT = EmbedDbInfo.FETCH_COUNT;

    //使用的水印处理器类型
    private final static WatermarkProcessorType WATERMARK_PROCESSOR_TYPE = WatermarkProcessorType.OPTIMIZATION;

    //记录过程耗时
    private static long startTime;
    private static long endTime;

    //暂时模拟的共享公司名称
    private static ArrayList<String> targetList = new ArrayList<>(
            Arrays.asList("Tencent", "Alibaba", "Google", "Apple", "Banana", "Cherry", "Pineapple"));

    private final static String TARGET_NAME = getRandomTargetName();

    public static void main(String[] args){
        // 初始化数据库
        MainDbController dbController = initDatabase();
        // 初始化水印处理器
        WatermarkProcessor wmProcessor = initProcessor();

        // 初始化水印处理器配置
        initEncoderConfig(wmProcessor.getEncoder());

        // 嵌入水印
        embedWatermark(dbController, wmProcessor.getEncoder());
        // 导出数据集到文件
        exportEmbeddedDataset(dbController);
        // 通过csv文件导入数据库
        //publishTableFromFile(dbController);
        // 发布数据集
        //publishTable(dbController);
        // 模拟攻击
        simulateAttack(dbController, Attack.DELETION);
        // 提取水印
        //String extractedWatermark = extractWatermark(dbController, wmProcessor.getDecoder());
        String extractedWatermark = extractWatermark(dbController, wmProcessor.getDecoder(), dbController.getDatasetWithPK());

        //对提取水印溯源
        String target = identifyOrigin(getDbTableName(), extractedWatermark);
        logger.info("溯源得到的目标为 {}", target);

    }

    // 初始化水印处理器配置
    private static void initEncoderConfig(IEncoderNumericImpl encoder) {
        // 初始化秘钥信息
        StoredKey.Builder storedKeyBuilder = generateStoredKey();
        encoder.setStoredKeyBuilder(storedKeyBuilder);

        // 初始化约束条件, 约束条件由客户自定义
        ColumnDataConstraint dataConstraint = new ColumnDataConstraint(ConstraintType.DOUBLE, -300, 300, 2);
        logger.info("约束条件为: {}", dataConstraint.toString());
        encoder.setDataConstraint(dataConstraint);
    }

    // 模拟生成秘钥信息
    private static StoredKey.Builder generateStoredKey() {
        StoredKey.Builder storedKeyBuilder = new StoredKey.Builder()
                .setDbTable(getDbTableName()).setMinLength(StoredKeyDbInfo.PARTITION_MIN_LENGTH)
                .setSecretKey(StoredKeyDbInfo.SECRET_KEY)
                .setTarget(TARGET_NAME).setPartitionCount(EmbedDbInfo.PARTITION_COUNT);
        return storedKeyBuilder;
    }


    // 从文件导入到数据库
    private static void publishTableFromFile(MainDbController dbController) {
        startTime = System.currentTimeMillis();
        boolean publishSuccess = dbController.publishDatasetFromFile(getAbsoluteProjPath() +"\\"+ getExportCSVFileName());
        if (publishSuccess){
            endTime = System.currentTimeMillis();
            logger.info("从csv文件导入{}条数据集到数据库耗时： {} ms", dbController.getFetchCount(), (endTime - startTime));
        } else {
            logger.error("从csv文件导入到数据库失败");
        }

    }

    // 获取项目绝对路径
    private static String getAbsoluteProjPath() {
        File directory = new File("");// 参数为空
        String projPath = directory.getAbsolutePath();
        //System.out.println(projPath);
        return projPath;
    }

    // 导出已嵌入水印的数据集
    private static void exportEmbeddedDataset(MainDbController dbController) {
        startTime = System.currentTimeMillis();
        File file = new File(getExportCSVFileName());
        try {
            file.createNewFile();
            boolean flag = dbController.getDatasetWithPK().exportToCSV(file);
            if (flag){
                endTime = System.currentTimeMillis();
                logger.info("导出到csv文件成功");
                logger.info("导出{}条数据集到csv文件耗时： {} ms", dbController.getFetchCount(), (endTime - startTime));
            } else {
                logger.error("导出到csv文件失败");
            }
        } catch (IOException e) {
            logger.error("导出到csv文件失败");
            e.printStackTrace();
        }

    }


    /**
     * @Description 不写回数据库直接解码
     *              调试用
     * @author Fcat
     * @date 2019/4/7 8:30
     * @param dbController
     * @param decoder	对应编码器的解码器
     * @param datasetWithPK	 已嵌入水印的数据集
     * @return java.lang.String 水印
     */
    private static String extractWatermark(MainDbController dbController, IDecoder decoder, DatasetWithPK datasetWithPK) {
        System.out.println("开始提取水印...");
        //获取对应的各个秘钥、秘参
        StoredKey storedKey = SecretKeyDbController.getInstance().getStoredKeyByDbTable(getDbTableName());
        //设置解码时的参数
        OptimDecoder optimDecoder = (OptimDecoder) decoder;
        optimDecoder.setStoredKeyParams(storedKey);
        return optimDecoder.decode(datasetWithPK);
    }


    private static WatermarkProcessor initProcessor() {
        WatermarkFactory factory = new WatermarkFactory();
        WatermarkProcessor wmProcessor = factory.getWatermarkProcessor(WATERMARK_PROCESSOR_TYPE);
        logger.info("初始化{}完成", wmProcessor.toString());
        return wmProcessor;
    }


    private static void publishTable(MainDbController dbController) {
        System.out.println("正在发布数据表");
        startTime = System.currentTimeMillis();
        dbController.publishDataset();
        endTime = System.currentTimeMillis();
        logger.info("数据表发布成功！");
        logger.info("发布{}条数据所用耗时： {} ms",  dbController.getFetchCount(),(endTime - startTime) );
    }


    private static MainDbController initDatabase(){
        logger.info("开始初始化数据库连接...");
        //初始化数据库
        MainDbController dbController = new MainDbController(DB_NAME, EMBED_TABLE_NAME);
        logger.info("设置元组数为 {}", FETCH_COUNT);
        dbController.setFetchCount(FETCH_COUNT);        //设置获取元组数
        logger.info("设置发布表名为 {}", PUBLISHED_TABLE_NAME);
        dbController.setPublishTableName(PUBLISHED_TABLE_NAME); //设置发布表名
        logger.info("连接数据库成功");

        logger.info("开始获取数据集");
        startTime = System.currentTimeMillis();
        dbController.fetchDataset();
        endTime = System.currentTimeMillis();

        logger.info("获取{}条数据集耗时： {}ms", dbController.getFetchCount(), (endTime - startTime));
        return dbController;
    }


    private static void embedWatermark(MainDbController dbController, IEncoderNumericImpl encoder){

        logger.info("开始嵌入水印...");

        startTime = System.currentTimeMillis();
        // 向带有主键的数据嵌入水印
        encoder.encode(dbController.getDatasetWithPK(), SecretKeyDbController.getInstance().getWatermarkListByDbTable(getDbTableName()));
        endTime = System.currentTimeMillis();

        logger.info("嵌入水印完成");
        logger.info("嵌入水印耗时：{} ms", (endTime - startTime));
    }

    private static String getDbTableName() {
        return DB_NAME + "::" + EMBED_TABLE_NAME;
    }

    private static void simulateAttack(MainDbController dbController, Attack attack){
        switch (attack){
            case INSERTION:
                break;
            case DELETION:
                double deletionPercent = PublishDbInfo.DELETION_PERCENT;
                performDeletion(dbController, deletionPercent);
                break;
            case ALTERNATION:
                performLSBAlternation(dbController);
                break;
        }
    }

    private static void performLSBAlternation(MainDbController dbController) {
    }

    private static void performDeletion(MainDbController dbController, double deletionPercent) {
        logger.info("开始模拟删除攻击...");

        startTime = System.currentTimeMillis();
        dbController.randomDeletionInDataset(deletionPercent);
        endTime = System.currentTimeMillis();

        logger.info("删除完毕");
        logger.info("删除攻击耗时: {}", (endTime - startTime));
    }


    // 从发布数据库中
    // 得到发布的数据集并提取水印
    private static String extractWatermark(MainDbController dbController, IDecoder decoder){
        logger.info("开始提取水印...");

        //获取对应的各个秘钥、秘参
        StoredKey storedKey = SecretKeyDbController.getInstance().getStoredKeyByDbTable(getDbTableName());

        if (decoder instanceof OptimDecoder){
            //设置解码时的参数
            OptimDecoder optimDecoder = (OptimDecoder) decoder;
            optimDecoder.setStoredKeyParams(storedKey);
            return optimDecoder.decode(dbController.getPublishedDatasetWithPK());

        }else if(decoder instanceof PrimLSBDecoder){
            PrimLSBDecoder primLSBDecoder = (PrimLSBDecoder) decoder;
            primLSBDecoder.setStoredKeyParams(storedKey);
            primLSBDecoder.setOriginDatasetWithPK(dbController.getOriginDatasetWithPK());
            return primLSBDecoder.decode(dbController.getPublishedDatasetWithPK());

        }
        logger.error("提取水印为空!");
        return "empty!";
    }

    private static String identifyOrigin(String dbTable, String watermark){
        logger.info("开始溯源...");

        startTime = System.currentTimeMillis();
        String orign = SecretKeyDbController.getInstance().getMostLikelyTarget(dbTable, watermark);
        endTime = System.currentTimeMillis();

        logger.info("溯源耗时: {}", (endTime-startTime));
        return orign;
    }

    /*
     * 随机返回一个共享目标方的名字
     */
    private static String getRandomTargetName(){
        Random random = new Random();
        int index = random.nextInt(targetList.size());
        return targetList.get(index);
    }


    private static String getExportCSVFileName() {
        return EMBED_TABLE_NAME+"_publish.csv";
    }


}

package team.aster;

import team.aster.database.MainDbController;
import team.aster.database.SecretKeyDbController;
import team.aster.model.StoredKey;
import team.aster.processor.*;
import team.aster.utils.Constants;

enum Attack {
    INSERTION,
    DELETION,
    ALTERNATION
}

public class Simulator {

    private final static String DB_NAME = Constants.MysqlDbConfig.EMBED_DB_NAME;
    private final static String EMBED_TABLE_NAME = Constants.EmbedDbInfo.EMBED_TABLE_NAME;
    private final static String PUBLISHED_TABLE_NAME = Constants.PublishDbInfo.EMBED_TABLE_NAME;
    private final static int FETCH_COUNT = Constants.EmbedDbInfo.FETCH_COUNT;

    private static long startTime;
    private static long endTime;


    public static void main(String[] args){
        //初始化数据库
        MainDbController dbController = initDatabase();

        //使用基于最优化算法的水印嵌入
        WatermarkFactory factory = new WatermarkFactory();
        WatermarkProcessor wmProcessor = factory.getWatermarkProcessor(WatermarkProcessorType.OPTIMIZATION);
        System.out.printf("初始化%s完成%n", wmProcessor.toString());

        //嵌入水印
        embedWatermark(dbController, wmProcessor.getEncoder());

        //发布数据集
        publishTable(dbController);

        //模拟攻击
        //simulateAttack(dbController, Attack.DELETION);

        //提取水印
        String extractedWatermark = extraWatermark(dbController, wmProcessor.getDecoder());
        System.out.printf("提取到的水印为%s%n", extractedWatermark);

        //对提取水印溯源
        System.out.println("开始溯源...");
        String target = identifyOrigin(getDbTableName(), extractedWatermark);
        System.out.println("溯源得到的目标为" + target);

    }



    private static void publishTable(MainDbController dbController) {
        System.out.println("正在发布数据表");
        startTime = System.currentTimeMillis();
        dbController.publishDataset();
        endTime = System.currentTimeMillis();
        System.out.println("数据表发布成功！");
        System.out.printf("发布%d条数据所用耗时： %d ms%n",  dbController.getFetchCount(),(endTime - startTime) );
    }


    private static MainDbController initDatabase(){
        System.out.println("开始初始化数据库...");
        //初始化数据库
        MainDbController dbController = new MainDbController(DB_NAME, EMBED_TABLE_NAME);
        dbController.setFetchCount(FETCH_COUNT);
        dbController.setPublishTableName(PUBLISHED_TABLE_NAME);
        System.out.println("连接数据库成功");

        System.out.println("开始获取数据集");
        startTime = System.currentTimeMillis();
        dbController.fetchDataset();
        endTime = System.currentTimeMillis();

        System.out.printf("获取%d条数据集耗时： %d ms%n", dbController.getFetchCount(), (endTime - startTime));
        //dbController.printDatasetWithPK();
        return dbController;
    }


    private static void embedWatermark(MainDbController dbController, IEncoder encoder){
        System.out.println("开始嵌入水印...");
        startTime = System.currentTimeMillis();
        //向带有主键的数据嵌入水印
        encoder.encode(dbController.getDatasetWithPK(), SecretKeyDbController.getInstance().getWatermarkListByDbTable(getDbTableName()));
        endTime = System.currentTimeMillis();
        System.out.println("嵌入水印完成");
        System.out.printf("嵌入水印耗时：%d ms%n", (endTime - startTime));
    }

    private static String getDbTableName() {
        return DB_NAME + "::" + EMBED_TABLE_NAME;
    }

    private static void simulateAttack(MainDbController dbController, Attack attack){
        switch (attack){
            case INSERTION:
                break;
            case DELETION:
                double deletionPercent = 0.1;
                performDeletion(dbController, deletionPercent);
                break;
            case ALTERNATION:
                break;
        }
    }

    private static void performDeletion(MainDbController dbController, double deletionPercent) {
        System.out.println("开始模拟删除攻击...");
        startTime = System.currentTimeMillis();
        dbController.randomDeletion(deletionPercent);
        endTime = System.currentTimeMillis();
        System.out.println("删除完毕");
        System.out.printf("删除攻击耗时：%d%n", (endTime - startTime));
    }

    private static String extraWatermark(MainDbController dbController, IDecoder decoder){
        System.out.println("开始提取水印...");
        //获取对应的各个秘钥、秘参
        StoredKey storedKey = SecretKeyDbController.getInstance().getStoredKeyByDbTable(getDbTableName());

        //设置解码时的参数
        OptimDecoder optimDecoder = (OptimDecoder) decoder;
        optimDecoder.setStoredKeyParams(storedKey);
        return optimDecoder.decode(dbController.getPublishedDatasetWithPK());
    }

    private static String identifyOrigin(String dbTable, String watermark){

        return "";
    }



}

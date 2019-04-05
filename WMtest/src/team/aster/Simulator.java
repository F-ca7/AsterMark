package team.aster;

import team.aster.database.MainDbController;
import team.aster.database.SecretKeyDbController;
import team.aster.model.StoredKey;
import team.aster.processor.*;

import static team.aster.utils.Constants.*;

enum Attack {
    INSERTION,
    DELETION,
    ALTERNATION
}

public class Simulator {

    private final static String DB_NAME = MysqlDbConfig.EMBED_DB_NAME;
    private final static String EMBED_TABLE_NAME = EmbedDbInfo.EMBED_TABLE_NAME;
    private final static String PUBLISHED_TABLE_NAME = PublishDbInfo.PUBLISH_TABLE_NAME;
    private final static int FETCH_COUNT = EmbedDbInfo.FETCH_COUNT;

    //使用的水印处理器类型
    private final static WatermarkProcessorType WATERMARK_PROCESSOR_TYPE = WatermarkProcessorType.OPTIMIZATION;

    //记录过程耗时
    private static long startTime;
    private static long endTime;


    public static void main(String[] args){
        //初始化数据库
        MainDbController dbController = initDatabase();
        //初始化水印处理器
        WatermarkProcessor wmProcessor = initProcessor();

        //嵌入水印
        //embedWatermark(dbController, wmProcessor.getEncoder());
        //发布数据集
        //publishTable(dbController);
        //模拟攻击
        //simulateAttack(dbController, Attack.DELETION);
        //提取水印
        String extractedWatermark = extraWatermark(dbController, wmProcessor.getDecoder());

        //对提取水印溯源
        String target = identifyOrigin(getDbTableName(), extractedWatermark);
        System.out.println("溯源得到的目标为" + target);

    }

    private static WatermarkProcessor initProcessor() {
        WatermarkFactory factory = new WatermarkFactory();
        WatermarkProcessor wmProcessor = factory.getWatermarkProcessor(WATERMARK_PROCESSOR_TYPE);
        System.out.printf("初始化%s完成%n", wmProcessor.toString());
        return wmProcessor;
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
        dbController.setFetchCount(FETCH_COUNT);        //设置获取元组数
        dbController.setPublishTableName(PUBLISHED_TABLE_NAME); //设置发布表名
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
                double deletionPercent = PublishDbInfo.DELETION_PERCENT;
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
        return "empty!";
    }

    private static String identifyOrigin(String dbTable, String watermark){
        System.out.println("开始溯源...");
        return SecretKeyDbController.getInstance().getMostLikelyTarget(dbTable, watermark);
    }



}

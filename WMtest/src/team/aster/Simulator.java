package team.aster;

import team.aster.database.DbController;
import team.aster.processor.*;

enum Attack {
    INSERTION,
    DELETION,
    ALTERNATION
}

public class Simulator {
    private final static String DB_NAME = "wm_exp";
    private final static String EMBED_TABLE_NAME = "transaction_2013";
    private final static String PUBLISHED_TABLE_NAME = "test_publish";
    private final static String KEYSTORE_TABLE_NAME = "keystore";
    private static long startTime;
    private static long endTime;


    public static void main(String[] args){
        System.out.println("开始初始化数据库...");
        DbController dbController = initDatabase();

        WatermarkFactory factory = new WatermarkFactory();
        //使用基于最优化算法的水印嵌入
        WatermarkProcessor wmProcessor = factory.getWatermarkProcessor(WatermarkProcessorType.OPTIMIZATION);
        System.out.printf("初始化%s完成\n", wmProcessor.toString());

        System.out.println("开始嵌入水印...");
        embedWatermark(dbController, wmProcessor.getEncoder());

        dbController.setTableName(PUBLISHED_TABLE_NAME);
        System.out.println("开始模拟删除攻击...");
        //simulateAttack(dbController, Attack.DELETION);
        System.out.println("删除完毕");

        System.out.println("开始提取水印...");
        String extractedWatermark = extraWatermark(dbController, wmProcessor.getDecoder());
        System.out.printf("提取到的水印为%s", extractedWatermark);
        System.out.println("开始溯源...");


    }

    private static DbController initDatabase(){
        DbController dbController = new DbController(DB_NAME, EMBED_TABLE_NAME);
        System.out.println("连接数据库成功");
        System.out.println("开始获取数据集");

        startTime = System.currentTimeMillis();
        dbController.fetchDataset();
        endTime = System.currentTimeMillis();

        System.out.printf("获取%d条数据集耗时： %d ms\n", dbController.getFETCH_COUNT(), (endTime - startTime));
        //dbController.printDatasetWithPK();
        return dbController;
    }


    private static void embedWatermark(DbController dbController, IEncoder encoder){
        startTime = System.currentTimeMillis();
        //向带有主主键的数据嵌入水印
        encoder.encode(dbController.getDatasetWithPK());
        endTime = System.currentTimeMillis();
        System.out.println("嵌入水印完成");
        System.out.printf("嵌入水印耗时：%d ms\n", (endTime - startTime));
    }

    private static void simulateAttack(DbController dbController, Attack attack){
        switch (attack){
            case INSERTION:
                break;
            case DELETION:
                double deletionPercent = 0.2;
                performDeletion(dbController, deletionPercent);
                break;
            case ALTERNATION:
                break;
        }
    }

    private static void performDeletion(DbController dbController, double deletionPercent) {
        dbController.randomDeletion(deletionPercent);
    }

    private static String extraWatermark(DbController dbController, IDecoder decoder){

        return "";
    }

    private static String identifyOrigin(String watermark){

        return "";
    }



}

package team.aster;

import team.aster.database.DbController;
import team.aster.processor.WatermarkFactory;
import team.aster.processor.WatermarkProcessor;
import team.aster.processor.WatermarkProcessorType;

enum Attack {
    INSERTION,
    DELETION,
    ALTERNATION
}

public class Simulator {
    private final static String DB_NAME = "wm_exp";
    private final static String TABLE_NAME = "transaction_2013";
    private static long startTime;
    private static long endTime;


    public static void main(String[] args){
        System.out.println("开始初始化数据库...");
        DbController dbController = initDatabase();

        System.out.println("开始嵌入水印...");
        embedWatermark(dbController);

//        System.out.println("开始模拟删除攻击...");
//        simulateAttack(team.aster.Attack.DELETION);
//        System.out.println("开始提取水印...");
//        String extractedWatermark = extraWatermark();
//        System.out.printf("提取到的水印为%s", extractedWatermark);
//        System.out.println("开始溯源...");


    }

    private static DbController initDatabase(){
        DbController dbController = new DbController(DB_NAME, TABLE_NAME);
        System.out.println("连接数据库成功");
        System.out.println("开始获取数据集");

        startTime = System.currentTimeMillis();
        dbController.fetchDataset();
        endTime = System.currentTimeMillis();

        System.out.printf("获取%d条数据集耗时： %d ms\n", dbController.getFETCH_COUNT(), (endTime - startTime));
        //dbController.printDatasetWithPK();
        return dbController;
    }


    private static void embedWatermark(DbController dbController){
        WatermarkFactory factory = new WatermarkFactory();
        WatermarkProcessor wmProcessor = factory.getWatermarkProcessor(WatermarkProcessorType.OPTIMIZATION);
        System.out.printf("初始化%s完成\n", wmProcessor.toString());
        //向带有主主键的数据嵌入水印
        wmProcessor.encodeDB(dbController.getDatasetWithPK());
        System.out.println("嵌入水印完成");
    }

    private static void simulateAttack(Attack attack){
        switch (attack){
            case INSERTION:
                break;
            case DELETION:
                performDeletion();
                break;
            case ALTERNATION:
                break;
        }
    }

    private static void performDeletion() {
    }

    private static String extraWatermark(){

        return "";
    }

    private static String identifyOrigin(String watermark){

        return "";
    }



}

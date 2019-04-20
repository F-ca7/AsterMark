package team.aster.utils;

import java.io.File;
import java.io.IOException;

public class Constants {
    private static final String CONFIG_NAME = "config.properties";
    //private static Properties configProperties;

    private Constants(){
    }

//
//    static {
//        getConfigPath();
//        System.out.println("test");
//        MysqlDbConfig.CONN_PARAM = "ms";
//    }

    private static String getConfigPath() {
        File directory = new File("");// 参数为空
        String projPath = null;
        try {
            projPath = directory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(projPath);
        System.out.println(projPath+CONFIG_NAME);
        return projPath+"\\"+CONFIG_NAME;
    }

    public static class MysqlDbConfig {
        public static String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";  //驱动名
        public static String URL = "jdbc:mysql://localhost:3306/";    //mysql的url
        public static String URL_FORAMTTED = "jdbc:mysql://%s:%s/";    //mysql的url
        public static String USERNAME = "root";    //mysql的用户名
        public static String PASSWORD = "0000";    //mysql的密码 todo 后期加密
        public static String CONN_PARAM = "?useUnicode=true&characterEncoding=UTF-8" +
                "&serverTimezone=Asia/Shanghai&useSSL=false" +
                "&rewriteBatchedStatements=true";    //mysql的连接参数
        public static String EMBED_DB_NAME = "wm_exp";    //嵌入的数据库
        public static String PUBLISH_DB_NAME = "wm_exp";  //发布的数据库，理论上可以与嵌入的数据库相同
        public static String STORED_KEY_DB_NAME = "wm_exp";  //保存秘钥的数据库，可能在别的数据库中，权限不同
    }

    public static class EmbedDbInfo {
        /*
         *   农业数据
         */
//        public final static int FETCH_COUNT = 500;    //要获取的元组数
//        public final static int PARTITION_COUNT = 50;    //分组数
//        public final static String EMBED_TABLE_NAME = "transaction_2013_all";     //要嵌入的表名
//        public final static int PK_COL_INDEX = 1;   //主键所在的列
//        public final static int EMBED_COL_INDEX = 15;   //要嵌入的属性所在的列

        /*
         *   评论文本
         */
        public final static int FETCH_COUNT = 1000;    //要获取的元组数
        public final static int PARTITION_COUNT = 80;    //分组数
        public final static String EMBED_TABLE_NAME = "amazon_reviews";     //要嵌入的表名
        public final static int PK_COL_INDEX = 1;   //主键所在的列
        public final static int EMBED_COL_INDEX = 10;   //要嵌入的属性所在的列
    }

    public static class PublishDbInfo {
        /*
         *   农业数据
         */
//        public final static String PUBLISH_TABLE_NAME = "transaction_2013_published"; //发布的表名
//        public final static double DELETION_PERCENT = 0.1;

        /*
         *   评论文本
         */
        public final static String PUBLISH_TABLE_NAME = "amazon_reviews_published"; //发布的表名
        public final static double DELETION_PERCENT = 0.0;
    }

    public static class StoredKeyDbInfo {
        public final static String STORED_KEY_TABLE_NAME = "stored_key"; //保存秘钥的表名
        public final static double SECRET_KEY = 0.3;        //SECRET_KEY
        public final static int PARTITION_MIN_LENGTH = 10;        //解码划分的最小长度
    }
}

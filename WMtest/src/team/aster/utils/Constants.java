package team.aster.utils;

public interface Constants {
    class MysqlDbConfig {
        public final static String DRIVER_NAME = "com.mysql.jdbc.Driver";  //驱动名
        public final static String URL = "jdbc:mysql://localhost:3306/";    //mysql的url
        public final static String USERNAME = "root";    //mysql的用户名
        public final static String PASSWORD = "0000";    //mysql的密码 todo 后期加密
        public final static String CONN_PARAM = "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";    //mysql的连接参数
        public final static String EMBED_DB_NAME = "wm_exp";    //嵌入的数据库
        public final static String PUBLISH_DB_NAME = "wm_exp";  //发布的数据库
        public final static String STORED_KEY_DB_NAME = "wm_exp";  //保存秘钥的数据库

    }
    class EmbedDbInfo {
        public final static int FETCH_COUNT = 10000;    //要获取的元组数
        public final static String EMBED_TABLE_NAME = "transaction_2013";     //要嵌入的表名
        public final static int PK_COL_INDEX = 1;   //主键所在的列
        public final static int EMBED_COL_INDEX = 15;   //要嵌入的属性所在的列
    }

    class PublishDbInfo {
        public final static String EMBED_TABLE_NAME = "transaction_2013_published"; //发布的表名

    }

    class StoredKeyDbInfo {
        public final static String STORED_KEY_TABLE_NAME = "transaction_2013_published"; //保存秘钥的表名
    }
}

package team.aster.database;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.model.DatasetWithPK;
import team.aster.model.StoredKey;
import team.aster.model.TargetSimilarity;
import team.aster.utils.BinaryUtils;
import team.aster.utils.Constants;
import team.aster.utils.Constants.MysqlDbConfig;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static team.aster.utils.Constants.StoredKeyDbInfo.STORED_KEY_TABLE_NAME;


public class MainDbController {
    private static Logger logger = LoggerFactory.getLogger(MainDbController.class);

    private Connection conn;        //主连接，必须有


    private String dbName;              //操作的数据库名
    private String originTableName;     //源表名


    private int PK_COL;     //主键所在的列
    private int EMBED_COL;     //嵌入字段所在的列

    private String storeKeyTableName = STORED_KEY_TABLE_NAME;   //保存水印的表名
    private String publishTableName;    //发布表名


    // 数据集，二维表
    private ArrayList<ArrayList<String>> dataset;

    //数据库对应的所有表名
    private ArrayList<String> tableNameList;
    //表对应的字段名
    private ArrayList<String> fieldNameList;

    //原数据集
    //private DatasetWithPK originDatasetWithPK;
    private DatasetWithPK datasetWithPK;


    public MainDbController(){
        this.fieldNameList = new ArrayList<>();
        this.tableNameList = new ArrayList<>();
        this.dataset = new ArrayList<>();
        this.datasetWithPK = new DatasetWithPK();
        //this.originDatasetWithPK = new DatasetWithPK();

    }

    /**
     * @Description 将要嵌入水印的表放入内存中
     * @author Fcat
     * @date 2019/3/23
     */
    private void fetchDataset(){
        dataset.clear();
        //todo 后期再扩展
        if(conn == null){
            System.out.println("尚未连接数据库!");
            return;
        }
        String querySql = String.format("SELECT * FROM %s", originTableName);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            //获取该表的总列数
            ResultSetMetaData rsMetadata = rs.getMetaData();
            int colCount = rsMetadata.getColumnCount();
            System.out.printf("表%s共有%d个字段%n", originTableName, colCount);
            while (rs.next()){
                ArrayList<String> tmpList = new ArrayList<>(colCount);
                //ArrayList<String> tmpList2 = new ArrayList<>(colCount);
                for(int i=0; i<colCount; i++){
                    //这里的index从1开始，包括主键
                    String data = rs.getString(i+1);
                    tmpList.add(data);
                    //tmpList2.add(data);
                }
                //String pk = rs.getString(PK_COL);
                dataset.add(tmpList);
                //datasetWithPK.addRecord(pk, tmpList);
                //todo 这里留存了副本 考虑去掉
                //originDatasetWithPK.addRecord(pk, tmpList2);
            }
            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (pstmt!=null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化带主键的数据集
     */
    public void initDatasetWithPK(){
        Map<String, ArrayList<String>> dsMap = datasetWithPK.getDataset();
        dsMap.clear();
        String pk;
        for (ArrayList<String> row:dataset){
            pk = row.get(PK_COL-1);
            dsMap.put(pk, row);
        }
    }


    public void randomDeletionInDataset(double deletionPercent){
        datasetWithPK.randomDelete(deletionPercent);
    }


    /**
     * @Description 发布嵌入水印的数据集到数据库中
     * @author Fcat
     * @date 2019/4/1 21:44
     */
    public boolean publishDataset(){
        boolean success;
        // 先创建发布表
        if (!createPublishTableInDatabase()){
            return false;
        }
        int columns = dataset.get(0).size();
        String[] placeholders = new String[columns];
        //动态拼接占位符
        Arrays.fill(placeholders, "?");
        String insertSql = String.format("INSERT INTO `%s` VALUES (%s)", publishTableName, String.join( ",",placeholders));
        PreparedStatement pstmt =null;
        try {
            pstmt = conn.prepareStatement(insertSql);
            logger.info("发布数据集共有 {}行", datasetWithPK.getDataset().values().size());
            Map<String, ArrayList<String>> dataset = datasetWithPK.getDataset();
            PreparedStatement finalPstmt = pstmt;
            dataset.forEach((k, v)->{
                try {
                    finalPstmt.setString(1, k);
                    for (int i=0; i<v.size(); i++){
                        finalPstmt.setString(i+1, v.get(i));
                    }
                    finalPstmt.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            // 批处理操作
            pstmt.executeBatch();
            pstmt.close();
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            success =false;
        } finally {
            if (pstmt!=null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }


    private boolean createPublishTableInDatabase() {
        boolean isSuccess;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String timestamp = df.format(new Date());// new Date()为获取当前系统时间
        String uuid = UUID.randomUUID().toString().replaceAll("-","").substring(0,10);
        publishTableName = originTableName + "_publish_"+timestamp+"_"+uuid;
        String createNewTableSql = String.format("CREATE TABLE `%s` LIKE `%s`", publishTableName, originTableName);

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(createNewTableSql);
            pstmt.executeUpdate();
            isSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            if (pstmt != null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    /**
     * 读取文件到数据库中保存
     * @param filepath  文件路径
     * @return  保存是否成功
     */
    public boolean publishDatasetFromFile(String filepath){
        boolean isSuccess;
        String uuid = UUID.randomUUID().toString().replaceAll("-","").substring(0,10);
        String publishName = publishTableName + "_"+uuid;
        String createNewTableSql = String.format("CREATE TABLE `%s` LIKE `%s`", publishName, originTableName);
        // 在regex中"\\"表示一个"\"，在java中一个"\"也要用"\\"表示。这样，前一个"\\"代表regex中的"\"，后一个"\\"代表java中的"\"
        filepath = filepath.replaceAll("\\\\","\\\\\\\\");  //必须要把\替换成\\，否则sql无法解析
        String loadSql = String.format("load data infile '%s' into table %s fields terminated by',' lines terminated by '\\r\\n';", filepath, publishName);
        //System.out.println(loadSql);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(createNewTableSql);
            pstmt.executeUpdate();
            pstmt = conn.prepareStatement(loadSql);
            pstmt.executeUpdate();
            isSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            if (pstmt != null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    /**
     * @Description 随机删除数据表中
     *              一定比例的元组
     * @param deletionPercent 删除比例
     */
    public void randomDeletion(double deletionPercent){
        String queryCountSql = String.format("SELECT COUNT(*) FROM %s", publishTableName);

        int rowCount = 0;
        try {
            PreparedStatement pstmt = conn.prepareStatement(queryCountSql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                rowCount = rs.getInt(1);
            }
            System.out.printf("表%s共%d行%n", originTableName, rowCount);
            int deletionCount = (int)((double)rowCount*deletionPercent);
            System.out.printf("随机删除%d条数据%n", deletionCount);
            String deletionSql = String.format("DELETE FROM `%s` ORDER BY rand() LIMIT %d", publishTableName, deletionCount);
            pstmt = conn.prepareStatement(deletionSql);
            pstmt.executeUpdate();

            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description 连接数据库操作
     *              在构造类后调用
     * @param dbName 数据库名
     * @param ip 本地localhost或者远程
     * @param port 端口，默认3306
     * @param username 用户名
     * @param pwd 密码
     * @return 连接是否成功
     */
    public boolean connectDB(String dbName, String ip, String port, String username, String pwd) {
        boolean connectSuccess = false;
        // mysql配置信息
        try {
            String DRIVER_NAME = MysqlDbConfig.DRIVER_NAME;
            String CONN_PARAM = MysqlDbConfig.CONN_PARAM;
            String url = String.format(MysqlDbConfig.URL_FORMATTED, ip, port);
            Class.forName(DRIVER_NAME);
            conn = DriverManager.getConnection(url + dbName + CONN_PARAM, username, pwd);
            connectSuccess = true;
            // 成功则设置成员变量
            this.dbName = dbName;
            // 获取所有表名
            initTableNameList();
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL错误");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return connectSuccess;
    }


    /**
     * @Description 通过数据库::表名获取对应的秘钥、秘参信息
     *              用于解码
     * @author Fcat
     * @date 2019/4/2 11:33
     * @param dbTable
     * @return team.aster.model.StoredKey
     */
    public StoredKey getStoredKeyByDbTable(String dbTable){
        String querySql = String.format("SELECT * FROM `%s` WHERE db_table='%s'",
                storeKeyTableName, dbTable);
        PreparedStatement pstmt;
        StoredKey storedKey = new StoredKey.Builder().build();
        try {
            pstmt = conn.prepareStatement(querySql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY);
            ResultSet rs = pstmt.executeQuery();
            //移动cursor到最后一行
            rs.absolute(-1);
            storedKey = new StoredKey.Builder().
                    setSecretKey(rs.getDouble("secretKey")).
                    setSecretCode(rs.getString("secretCode")).
                    setWmLength(rs.getInt("wm_length")).
                    setMinLength(rs.getInt("min_length")).
                    setPartitionCount(rs.getInt("partition_count")).
                    setThreshold(rs.getDouble("threshold")).build();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storedKey;
    }

    /**
     * @Description 通过数据库::表名找到已嵌入并发布过的水印集合
     *              用于生成新水印时降低相似度
     * @author Fcat
     * @date 2019/4/2 11:31
     * @return java.util.ArrayList<java.lang.String>
     */
    public ArrayList<String> getWatermarkListByDbTable(){
        ArrayList<String> wmList = new ArrayList<>();
        try {
            // 查询已存在的水印
            String querySql = String.format("SELECT watermark FROM `%s` WHERE db_table='%s'",
                    storeKeyTableName, getDbTableName());
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                wmList.add(rs.getString(1));
            }
            pstmt.close();
            rs.close();
            logger.info("已存在水印集为 {}", wmList);
            return wmList;
        } catch (SQLException e) {
            System.out.println("SQL错误");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return wmList;
    }


    /**
     * @Description 根据表和解码水印计算出数据库中存在相似度最大的水印
     *              并且返回对应的目标公司
     * @author Fcat
     * @date 2019/4/5 21:14
     * @param dbTable	对应的数据库::表
     * @param watermark	解码的得到的水印
     * @return java.lang.String 最有可能的目标公司
     */
    public String getMostLikelyTarget(String dbTable, String watermark){
        //k:watermark, v:target
        Map<String, String> wmListMap = new HashMap<>();
        try {
            String querySql = String.format("SELECT watermark,target FROM `%s` WHERE db_table='%s'",
                    storeKeyTableName, dbTable);
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                wmListMap.put(rs.getString(1), rs.getString(2));
            }
            pstmt.close();
            rs.close();
            logger.info("已存在水印集为 {}", wmListMap);
        } catch (SQLException e) {
            logger.error("SQL错误 {}", e.getSQLState());
            e.printStackTrace();
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        AtomicReference<Double> similarity = new AtomicReference<>(0.0);
        AtomicReference<String> result = new AtomicReference<>("");
        wmListMap.forEach((k,v)->{
            double tmpSimilarity = BinaryUtils.getStringSimilarity(k, watermark);
            if (tmpSimilarity > similarity.get()){
                similarity.set(tmpSimilarity);
                result.set(v);
            }
        });
        logger.info("相似度为 {}, 结果为 {}", similarity, result);
        return result.get();
    }


    /**
     * 根据已经解码的水印
     * 获取同一个数据库表的目标信息-相似度有序列表
     * @return 目标相似度的优先队列
     */
    public PriorityQueue<TargetSimilarity> getTargetSimilarityRank(String dbTable, String watermark){
        PriorityQueue<TargetSimilarity> queue = new PriorityQueue<>();
        Map<String, String> wmListMap = new HashMap<>();
        try {
            String querySql = String.format("SELECT watermark,target FROM `%s` WHERE db_table='%s'",
                    storeKeyTableName, dbTable);
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                wmListMap.put(rs.getString(1), rs.getString(2));
            }
            pstmt.close();
            rs.close();
            logger.info("已存在水印集为 {}", wmListMap);
        } catch (SQLException e) {
            logger.error("SQL错误 {}", e.getSQLState());
            e.printStackTrace();
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        wmListMap.forEach((k, info)->{
            double tmpSimilarity = BinaryUtils.getStringSimilarity(k, watermark);
            TargetSimilarity targetSimilarity = new TargetSimilarity(info, tmpSimilarity);
            queue.add(targetSimilarity);
        });
        return queue;
    }


    /**
     * @Description 把密参信息保存到数据库当中
     * @author Fcat
     * @date 2019/4/5 21:14
     * @param storedKey	密参信息
     * @return java.lang.String 最有可能的目标公司
     */
    public void saveStoredKeysToDB(StoredKey storedKey){
        PreparedStatement pstmt = null;
        try {
            String insertSql = "INSERT INTO `" + storeKeyTableName
                    +"` (watermark,wm_length,threshold,secretKey,secretCode,min_length,db_table,partition_count,target) " +
                    "VALUES (?,?,?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, BinaryUtils.parseBinaryToString(storedKey.getWaterMark().getBinary()));
            pstmt.setInt(2, storedKey.getWmLength());
            pstmt.setDouble(3, storedKey.getThreshold());
            pstmt.setDouble(4, storedKey.getSecretKey());
            pstmt.setString(5, storedKey.getSecretCode());
            pstmt.setInt(6, storedKey.getMinLength());
            pstmt.setString(7, storedKey.getDbTable());
            pstmt.setInt(8, storedKey.getPartitionCount());
            pstmt.setString(9, storedKey.getTarget());
            pstmt.executeUpdate();

            logger.info("保存水印参数数据成功！");
            pstmt.close();
        } catch (SQLException e) {
            logger.error("SQL错误 {}", e.getSQLState());
            e.printStackTrace();
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 发布数据库到csv文件
     * @param path 文件名
     */
    public void exportEmbeddedDataset(String path) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");//设置日期格式
        String timestamp = df.format(new Date());// new Date()为获取当前系统时间
        String pathname;
        pathname = path + "\\publish " +timestamp+".csv";
        File file = new File(pathname);
        try {
            file.createNewFile();
            boolean flag = datasetWithPK.exportToCSV(file);
            if (flag){
                logger.info("导出到csv文件成功");
                logger.info("导出{}条数据集到csv文件",  datasetWithPK.getDataset().size());
            } else {
                logger.error("导出到csv文件失败");
            }
        } catch (IOException e) {
            logger.error("导出到csv文件失败");
            e.printStackTrace();
        }

    }

    /**
     * 判断主数据库中密钥表是否存在
     * @return  true-存在
     *          false-不存在
     */
    public boolean isStoredKeyTableExist() {
        String[] storedKeyColumnNames = new String[]{
                Constants.StoredKeyDbInfo.FIELD_NAME_1,
                Constants.StoredKeyDbInfo.FIELD_NAME_2,
                Constants.StoredKeyDbInfo.FIELD_NAME_3,
                Constants.StoredKeyDbInfo.FIELD_NAME_4,
                Constants.StoredKeyDbInfo.FIELD_NAME_5,
                Constants.StoredKeyDbInfo.FIELD_NAME_6,
                Constants.StoredKeyDbInfo.FIELD_NAME_7,
                Constants.StoredKeyDbInfo.FIELD_NAME_8,
                Constants.StoredKeyDbInfo.FIELD_NAME_9,
                Constants.StoredKeyDbInfo.FIELD_NAME_10,
        };
        boolean existed = false;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String querySql = String.format("select COLUMN_NAME " +
                    " from information_schema.columns where table_schema" +
                    "='%s' and table_name='%s'; ", dbName,STORED_KEY_TABLE_NAME);
            pstmt = conn.prepareStatement(querySql);
            rs = pstmt.executeQuery();
            int rowCount = 0;
            if (rs.next()){
                do {
                    rowCount++;
                    if (rowCount > storedKeyColumnNames.length){
                        break;
                    }
                    if (!rs.getString(1).equals(storedKeyColumnNames[rowCount-1])){
                        break;
                    }
                }while (rs.next());
                if (rowCount == storedKeyColumnNames.length){
                    // 只有字段数目匹配且名字都匹配时
                    // 才判定存在
                    existed = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (pstmt != null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return existed;
    }


    /**
     * 获取主数据库所有表名
     */
    private void initTableNameList() {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String querySql = "select table_name tableName from information_schema.tables where table_schema = (select database());";
            pstmt = conn.prepareStatement(querySql);
            rs = pstmt.executeQuery();
            while (rs.next()){
                tableNameList.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (pstmt != null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 在主数据库中创建密钥表
     */
    public void createStoredKeyTable() {
        PreparedStatement pstmt = null;
        try {
            String createSql = "CREATE TABLE `stored_key` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `watermark` varchar(255) DEFAULT NULL,\n" +
                    "  `wm_length` int(11) DEFAULT NULL,\n" +
                    "  `threshold` double DEFAULT NULL,\n" +
                    "  `secretKey` double DEFAULT NULL,\n" +
                    "  `secretCode` varchar(255) DEFAULT NULL,\n" +
                    "  `min_length` int(11) DEFAULT NULL,\n" +
                    "  `db_table` varchar(255) DEFAULT NULL,\n" +
                    "  `partition_count` int(11) DEFAULT NULL,\n" +
                    "  `target` text,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;";
            System.out.println(createSql);
            pstmt = conn.prepareStatement(createSql);
            pstmt.executeUpdate();
            logger.info("创建密钥表成功！");
            pstmt.close();
        } catch (SQLException e) {
            logger.error("SQL错误 {}", e.getSQLState());
            e.printStackTrace();
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置表名同时加载数据
     * @param tableName 源表名
     */
    public void setTableName(String tableName) {
        this.originTableName = tableName;
        loadFieldNames(conn);
        fetchDataset();
    }

    public void setPublishTableName(String publishTableName) {
        this.publishTableName = publishTableName;

    }

    /**
     * 载入字段名
     * 并存储起来
     */
    private void loadFieldNames(Connection conn) {
        startLoadingFields(conn, fieldNameList, originTableName);
    }


    static void startLoadingFields(Connection conn, ArrayList<String> fieldNameList, String originTableName) {
        fieldNameList.clear();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String querySql = String.format("select column_name columnName from information_schema.columns" +
                    " where table_name = '%s' and table_schema = (select database()) order by ordinal_position;", originTableName);
            pstmt = conn.prepareStatement(querySql);
            rs = pstmt.executeQuery();
            while (rs.next()){
                fieldNameList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (pstmt != null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//    public DatasetWithPK getOriginDatasetWithPK() {
//        return originDatasetWithPK;
//    }

    public String getDbTableName(){
        return dbName+"::"+originTableName;
    }

    public int setPK(String keyColumnName) {
        int i = 0;
        for (String colName : fieldNameList){
            i++;
            if (colName.equals(keyColumnName))
                break;

        }
        PK_COL = i;
        return i;
    }

    public int setEmbeddingCol(String embeddingColumnName) {
        int i = 0;
        for (String colName : fieldNameList){
            i++;
            if (colName.equals(embeddingColumnName))
                break;

        }
        EMBED_COL = i-1;
        return i;
    }
    public String getDbName() {
        return dbName;
    }


    public ArrayList<ArrayList<String>> getDataset() {
        return dataset;
    }

    public ArrayList<String> getDbTableNameList() {
        ArrayList<String> nameList =new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String querySql = "select distinct db_table from stored_key;";
            pstmt = conn.prepareStatement(querySql);
            rs = pstmt.executeQuery();
            while (rs.next()){
                nameList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (pstmt != null){
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return nameList;
    }

    public DatasetWithPK getDatasetWithPK(){
        return datasetWithPK;
    }

    public ArrayList<String> getFieldNameList(){
        return fieldNameList;
    }

    public ArrayList<String> getTableNameList() {
        return tableNameList;
    }


    public DescriptiveStatistics getEmbeddingColStats() {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        dataset.forEach(row->{
            double value = Double.parseDouble(row.get(EMBED_COL));
            stats.addValue(value);
        });
        return stats;
    }

    public void setPK_COL(int PK_COL) {
        this.PK_COL = PK_COL;
    }


}

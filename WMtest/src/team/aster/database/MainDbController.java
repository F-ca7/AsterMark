package team.aster.database;

import team.aster.model.DatasetWithPK;
import team.aster.utils.Constants;
import team.aster.utils.Constants.MysqlDbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;


public class MainDbController {
    private Connection conn;

    private String dbName;              //操作的数据库名
    private String originTableName;     //源表名
    private String publishTableName;    //发布表名

    private int fetchCount = Constants.EmbedDbInfo.FETCH_COUNT;
    private ArrayList<ArrayList<String>> dataset;
    //原数据集
    private DatasetWithPK originDatasetWithPK;
    private DatasetWithPK datasetWithPK;


    public MainDbController(String dbName, String tableName){
        this.dbName = dbName;
        this.originTableName = tableName;
        this.dataset = new ArrayList<>();
        this.datasetWithPK = new DatasetWithPK();
        this.originDatasetWithPK = new DatasetWithPK();
        connectDB();
    }

    /**
     * @Description 将要嵌入水印的表放入内存中
     * @author Fcat
     * @date 2019/3/23
     */
    public void fetchDataset(){
        //todo 后期再扩展
        //String pkField1 = "FDATE";
        //String pkField2 = "FTIME";
        if(conn == null){
            System.out.println("尚未连接数据库!");
            return;
        }
        String querySql = String.format("SELECT * FROM %s LIMIT %d", originTableName, fetchCount);
        try {
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            //获取该表的总列数
            ResultSetMetaData rsMetadata = rs.getMetaData();
            int colCount = rsMetadata.getColumnCount();
            System.out.printf("表%s共有%d个字段%n", originTableName, colCount);
            while (rs.next()){
                ArrayList<String> tmpList = new ArrayList<>(colCount);
                ArrayList<String> tmpList2 = new ArrayList<>(colCount);
                for(int i=0; i<colCount; i++){
                    //这里的index从1开始，包括id
                    String data = rs.getString(i+1);
                    tmpList.add(data);
                    tmpList2.add(data);

                }
                String pk = rs.getString(1);
                dataset.add(tmpList);
                datasetWithPK.addRecord(pk, tmpList);
                //todo 这里留存了副本 考虑去掉
                originDatasetWithPK.addRecord(pk, tmpList2);
            }
            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DatasetWithPK getDatasetWithPK(){
        return datasetWithPK;
    }


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
            String deletionSql = String.format("DELETE FROM %s ORDER BY rand() LIMIT %d", publishTableName, deletionCount);
            pstmt = conn.prepareStatement(deletionSql);
            pstmt.executeUpdate();

            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description 发布嵌入水印的数据集到数据库中
     * @author Fcat
     * @date 2019/4/1 21:44
     */
    public void publishDataset(){
        String insertSql = "INSERT INTO "+ publishTableName +
                " VALUES (?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ? ," +
                "?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(insertSql);
            System.out.println("发布数据集共有" + datasetWithPK.getDataset().values().size()+"行");
            Map<String, ArrayList<String>> dataset = datasetWithPK.getDataset();
            dataset.forEach((k,v)->{
                try {
                    pstmt.setString(1, k);
                    for (int i=0; i<v.size(); i++){
                        pstmt.setString(i+1, v.get(i));
                    }
                    //System.out.println(pstmt);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public DatasetWithPK getPublishedDatasetWithPK(){
        DatasetWithPK pubDatasetWithPK = new DatasetWithPK();
        if(conn == null){
            System.out.println("尚未连接数据库!");
            return pubDatasetWithPK;
        }
        String querySql = String.format("SELECT * FROM %s", publishTableName);
        try {
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            //获取该表的总列数
            ResultSetMetaData rsMetadata = rs.getMetaData();
            int colCount = rsMetadata.getColumnCount();
            System.out.printf("表%s共有%d个字段%n", publishTableName, colCount);
            while (rs.next()){
                ArrayList<String> tmpList = new ArrayList<>(colCount);
                for(int i=0; i<colCount; i++){
                    //这里的index从1开始，包括id
                    String data = rs.getString(i+1);
                    tmpList.add(data);

                }
                String pk = rs.getString(1);
                pubDatasetWithPK.addRecord(pk, tmpList);
            }
            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pubDatasetWithPK;
    }


    public boolean publishDatasetFromFile(String filepath){
        boolean isSuccess;
        String uuid = UUID.randomUUID().toString().replaceAll("-","").substring(0,10);
        String publishName = publishTableName + "_"+uuid;
        String createNewTableSql = String.format("CREATE TABLE %s LIKE %s", publishName, originTableName);
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


    //仅用于小规模调试
    public void printDataset(){
        for (ArrayList<String> row:dataset) {
            for (String value : row) {
                System.out.printf("%s  ", value);
            }
            System.out.println();
        }
        System.out.println();
    }

    //仅用于小规模调试
    public void printDatasetWithPK(){
        for(String key: datasetWithPK.getDataset().keySet()){
            System.out.println("主键为 "+key);
        }
    }



    private void connectDB() {
        //mysql配置信息
        try {
            String DRIVER_NAME = MysqlDbConfig.DRIVER_NAME;
            String URL = MysqlDbConfig.URL;
            String USERNAME = MysqlDbConfig.USERNAME;
            String PASSWORD = MysqlDbConfig.PASSWORD;
            String CONN_PARAM = MysqlDbConfig.CONN_PARAM;

            Class.forName(DRIVER_NAME);
            conn = DriverManager.getConnection(URL + dbName + CONN_PARAM, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            //todo 后期改用日志log打印
            System.out.println("找不到驱动");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL错误");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }





    public void setTableName(String tableName) {
        this.originTableName = tableName;
    }

    public void setPublishTableName(String publishTableName) {
        this.publishTableName = publishTableName;
    }


    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }

    public int getFetchCount() {
        return fetchCount;
    }

    public DatasetWithPK getOriginDatasetWithPK() {
        return originDatasetWithPK;
    }

}

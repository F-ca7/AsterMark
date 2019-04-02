package team.aster.database;

import team.aster.model.DatasetWithPK;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

public class MainDbController {

    private Connection conn;
    //要操作的表
    private String dbName;

    private String originTableName;


    private String publishTableName;

    private ArrayList<ArrayList<String>> dataset;
    private DatasetWithPK datasetWithPK;

    private static final int FETCH_COUNT = 1000;
    private static final String CONN_PARAM = "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";

    public int getFETCH_COUNT() {
        return FETCH_COUNT;
    }

    public void setTableName(String tableName) {
        this.originTableName = tableName;
    }


    public void setPublishTableName(String publishTableName) {
        this.publishTableName = publishTableName;
    }

    public MainDbController(String dbName, String tableName){
        this.dbName = dbName;
        this.originTableName = tableName;
        this.dataset = new ArrayList<>();
        this.datasetWithPK = new DatasetWithPK();
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
        String querySql = String.format("SELECT * FROM %s LIMIT %d", originTableName, FETCH_COUNT);
        try {
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            //获取该表的总列数
            ResultSetMetaData rsMetadata = rs.getMetaData();
            int colCount = rsMetadata.getColumnCount();
            System.out.printf("表%s共有%d个字段%n", originTableName, colCount);
            while (rs.next()){
                ArrayList<String> tmpList = new ArrayList<>(colCount);
                for(int i=0; i<colCount; i++){
                    //这里的index从2开始，跳过id
                    String data = rs.getString(i+1);
                    tmpList.add(data);

                }
                String pk = rs.getString(1);
                dataset.add(tmpList);
                datasetWithPK.addRecord(pk, tmpList);
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
        String queryCountSql = String.format("SELECT COUNT(*) FROM %s", originTableName);

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
            String deletionSql = String.format("DELETE FROM %s ORDER BY rand() LIMIT %d", originTableName, deletionCount);
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
        final String DRIVER_NAME = "com.mysql.jdbc.Driver";
        final String URL = "jdbc:mysql://localhost:3306/";

        final String USERNAME = "root";
        final String PASSWORD = "0000";

        try {
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




}

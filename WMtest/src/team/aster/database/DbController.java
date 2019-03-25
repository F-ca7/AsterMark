package team.aster.database;

import team.aster.model.DatasetWithPK;

import java.sql.*;
import java.util.ArrayList;

public class DbController {

    private Connection conn;
    //要操作的表
    private String dbName;
    private String tableName;
    private ArrayList<ArrayList<String>> dataset;
    private DatasetWithPK datasetWithPK;

    private final int FETCH_COUNT = 10;


    public int getFETCH_COUNT() {
        return FETCH_COUNT;
    }



    public DbController(String dbName, String tableName){
        this.dbName = dbName;
        this.tableName = tableName;
        this.dataset = new ArrayList<>();
        this.datasetWithPK = new DatasetWithPK();
        connectDB();
    }

    /**
     * @Title: fetchDataset
     * @Description: 将要嵌入水印的表放入内存中
     * @author Fcat
     * @date 2019/3/23
     * @param
     * @return void
     */
    public void fetchDataset(){
        //todo 后期再扩展
        String pkField1 = "FDATE";
        String pkField2 = "FTIME";
        if(conn == null){
            System.out.println("尚未连接数据库!");
            return;
        }
        String querySql = String.format("SELECT * FROM %s LIMIT %d", tableName, FETCH_COUNT);
        try {
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            //获取该表的总列数
            ResultSetMetaData rsMetadata = rs.getMetaData();
            int colCount = rsMetadata.getColumnCount();
            System.out.printf("表%s共有%d个字段\n", tableName, colCount);
            while (rs.next()){
                ArrayList<String> tmpList = new ArrayList<>(colCount);
                for(int i=0; i<colCount; i++){
                    //这里的index从1开始
                    String data = rs.getString(i+1);
                    tmpList.add(rs.getString(i+1));

                }
                String pk = rs.getString(pkField1) + rs.getString(pkField2);
                dataset.add(tmpList);
                datasetWithPK.addRecord(pk, tmpList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DatasetWithPK getDatasetWithPK(){
        return datasetWithPK;
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
            conn = DriverManager.getConnection(URL + dbName, USERNAME, PASSWORD);

        } catch (ClassNotFoundException e) {
            //todo 后期改用日志log打印
            System.out.println("找不到驱动");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }





}

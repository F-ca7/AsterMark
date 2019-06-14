package team.aster.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.model.DatasetWithPK;
import team.aster.model.StoredKey;
import team.aster.utils.BinaryUtils;
import team.aster.utils.Constants;
import team.aster.utils.Constants.MysqlDbConfig;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

public class SubDbController {
    private static Logger logger = LoggerFactory.getLogger(MainDbController.class);

    private Connection subConn;        //二级连接，必须有

    private String dbName;              //操作的数据库名
    private String originTableName;     //源表名

    public int getPK_COL() {
        return PK_COL;
    }

    private int PK_COL;     //主键所在的列

    private String publishTableName;    //发布表名


    public ArrayList<ArrayList<String>> getDataset() {
        return dataset;
    }

    // 数据集，二维表
    private ArrayList<ArrayList<String>> dataset;

    //数据库对应的所有表名
    private ArrayList<String> tableNameList;
    //表对应的字段名
    private ArrayList<String> fieldNameList;

    //原数据集
    private DatasetWithPK originDatasetWithPK;
    private DatasetWithPK datasetWithPK;


    public SubDbController(){
        this.fieldNameList = new ArrayList<>();
        this.tableNameList = new ArrayList<>();
        this.dataset = new ArrayList<>();
        this.datasetWithPK = new DatasetWithPK();
        this.originDatasetWithPK = new DatasetWithPK();

    }

    /**
     * @Description 将要嵌入水印的表放入内存中
     * @author Fcat
     * @date 2019/3/23
     */
    public void fetchDataset(){
        dataset.clear();
        //todo 后期再扩展
        if(subConn == null){
            System.out.println("尚未连接数据库!");
            return;
        }
        String querySql = String.format("SELECT * FROM `%s`", originTableName);
        PreparedStatement pstmt = null;
        try {
            pstmt = subConn.prepareStatement(querySql);
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

    public void initDatasetWithPK(){
        Map<String, ArrayList<String>> dsMap = datasetWithPK.getDataset();
        dsMap.clear();
        String pk;
        for (ArrayList<String> row:dataset){
            pk = row.get(PK_COL-1);
            dsMap.put(pk, row);
        }
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



    // 获取所有表名
    private void initTableNameList() {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String querySql = "select table_name tableName from information_schema.tables where table_schema = (select database());";
            pstmt = subConn.prepareStatement(querySql);
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

    public void setTableName(String tableName) {
        if (subConn != null){
            this.originTableName = tableName;
            loadFieldNames(subConn);
            fetchDataset();
        }
    }

    public void setPublishTableName(String publishTableName) {
        this.publishTableName = publishTableName;

    }

    /**
     * 载入字段名
     * 并存储起来
     */
    private void loadFieldNames(Connection conn) {
        MainDbController.startLoadingFields(conn, fieldNameList, originTableName);
    }



    public DatasetWithPK getOriginDatasetWithPK() {
        return originDatasetWithPK;
    }

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
        return i;
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
    public boolean connectDB(String dbName, String ip, String port, String username, String pwd)
    {
        boolean connectSuccess = false;
        // mysql配置信息
        try {
            String DRIVER_NAME = MysqlDbConfig.DRIVER_NAME;
            String CONN_PARAM = MysqlDbConfig.CONN_PARAM;
            String url = String.format(MysqlDbConfig.URL_FORMATTED, ip, port);
            Class.forName(DRIVER_NAME);
            subConn = DriverManager.getConnection(url + dbName + CONN_PARAM, username, pwd);
            connectSuccess = true;
            // 成功则设置成员变量
            this.dbName = dbName;
            // 清空原来所有数据结构
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
}

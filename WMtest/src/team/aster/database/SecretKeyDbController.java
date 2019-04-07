package team.aster.database;

import team.aster.model.StoredKey;
import team.aster.utils.BinaryUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static team.aster.utils.Constants.MysqlDbConfig;
import static team.aster.utils.Constants.StoredKeyDbInfo;


/**
 * @ClassName SecretKeyDbController
 * @Description 操作秘钥、秘参的管理器，用单例类实现
 * @author Fcat
 * @date 2019/4/2 10:54
 */
public class SecretKeyDbController {

    private final static String DRIVER_NAME = MysqlDbConfig.DRIVER_NAME;
    private final static String DB_NAME = MysqlDbConfig.STORED_KEY_DB_NAME;
    private final static String TABLE_NAME = StoredKeyDbInfo.STORED_KEY_TABLE_NAME;
    private final static String URL = MysqlDbConfig.URL;
    private final static String CONN_PARAM = MysqlDbConfig.CONN_PARAM;
    private final static String USERNAME = MysqlDbConfig.USERNAME;
    private final static String PASSWORD = MysqlDbConfig.PASSWORD;
    private Connection conn = null;

    //饿汉式加载
    private static SecretKeyDbController secretKeyDbController = new SecretKeyDbController();

    public static SecretKeyDbController getInstance(){
        return secretKeyDbController;
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
        String querySql = String.format("SELECT * FROM %s WHERE db_table='%s'", TABLE_NAME, dbTable);
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
     * @param dbTable
     * @return java.util.ArrayList<java.lang.String>
     */
    public ArrayList<String> getWatermarkListByDbTable(String dbTable){
        ArrayList<String> wmList = new ArrayList<>();
        try {
            //todo 查询已存在的水印
            String querySql = String.format("SELECT watermark FROM %s WHERE db_table='%s'", TABLE_NAME, dbTable);
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                wmList.add(rs.getString(1));
            }
            pstmt.close();
            rs.close();
            System.out.println("已存在水印集为");
            System.out.println(wmList);
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
            //todo 查询已存在的水印
            String querySql = String.format("SELECT watermark,target FROM %s WHERE db_table='%s'", TABLE_NAME, dbTable);
            PreparedStatement pstmt = conn.prepareStatement(querySql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                wmListMap.put(rs.getString(1), rs.getString(2));
            }
            pstmt.close();
            rs.close();
            System.out.println("已存在水印集为");
            System.out.println(wmListMap);
        } catch (SQLException e) {
            System.out.println("SQL错误");
            e.printStackTrace();
        } catch (Exception e){
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
        System.out.println("相似度为"+similarity+", 结果为"+result);
        return result.get();
    }


    public void saveStoredKeysToDB(StoredKey storedKey){
        try {
            String insertSql = String.format("INSERT INTO " + TABLE_NAME
                    +" (watermark,wm_length,threshold,secretKey,secretCode,min_length,db_table,partition_count,target) " +
                    "VALUES (?,?,?,?,?,?,?,?,?)");
            PreparedStatement pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, BinaryUtils.parseBinaryToString(storedKey.getWaterMark().getBinary()));
            pstmt.setInt(2, storedKey.getWmLength());
            pstmt.setDouble(3, storedKey.getThreshold());
            pstmt.setDouble(4, storedKey.getSecretKey());
            pstmt.setString(5, storedKey.getSecretCode());
            pstmt.setInt(6, storedKey.getMinLength());
            pstmt.setString(7, storedKey.getDbTable());
            pstmt.setInt(8, storedKey.getPartitionCount());
            pstmt.setString(9, storedKey.getTarget());
            //System.out.println(pstmt);
            pstmt.executeUpdate();

            System.out.println("保存水印参数数据成功！");
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("SQL错误");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private SecretKeyDbController(){
        try {
            Class.forName(DRIVER_NAME);
            conn = DriverManager.getConnection(URL +DB_NAME + CONN_PARAM, USERNAME, PASSWORD);
            System.out.println("初始化SecretKeyDbController成功");
        }  catch (ClassNotFoundException e) {
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

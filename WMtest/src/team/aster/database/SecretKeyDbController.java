package team.aster.database;

import team.aster.model.StoredKey;
import team.aster.utils.BinaryUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class SecretKeyDbController {

    private final static String DRIVER_NAME = "com.mysql.jdbc.Driver";
    private final static String DB_NAME = "wm_exp";
    private final static String URL = "jdbc:mysql://localhost:3306/";
    private final static String CONN_PARAM = "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    private final static String USERNAME = "root";
    private final static String PASSWORD = "0000";


    public static ArrayList<String> getWatetmarkListByDbTable(){
        Connection conn = null;
        ArrayList<String> wmList = new ArrayList<>();
        try {
            Class.forName(DRIVER_NAME);
            conn = DriverManager.getConnection(URL +DB_NAME + CONN_PARAM, USERNAME, PASSWORD);
            //todo 查询已存在的水印
            return wmList;
        } catch (ClassNotFoundException e) {
            //todo 后期改用日志log打印
            System.out.println("找不到驱动");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL错误");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return wmList;
    }


    public static void saveStoredKeysToDB(StoredKey storedKey){
        //mysql配置信息

        Connection conn = null;
        try {
            Class.forName(DRIVER_NAME);
            conn = DriverManager.getConnection(URL +DB_NAME + CONN_PARAM, USERNAME, PASSWORD);
            String insertSql = String.format("INSERT INTO stored_key (watermark,wm_length,threshold,secretKey,secretCode,min_length,db_table,partition_count,target) " +
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
            pstmt.executeUpdate();
            System.out.printf("保存水印参数数据成功！");

        } catch (ClassNotFoundException e) {
            //todo 后期改用日志log打印
            System.out.println("找不到驱动");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL错误");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

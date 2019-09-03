package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import team.aster.AbstractingExecutor;
import team.aster.database.MainDbController;
import team.aster.database.SubDbController;
import team.aster.model.TargetSimilarity;

import java.util.Optional;
import java.util.PriorityQueue;

public class InstantInfo {
    private static int loginType;
    private static boolean isTextDataType;
    private static String[] embeddingResultInfo;
    //private static PriorityQueue<TargetSimilarity> abstractResultInfo;
    private static MainDbController mainDbController;   //连接主密钥数据库的controller
    private static boolean isFirst;
    private static String file;
    private static SubDbController subDbController;     //连接次数据库


    private static AbstractingExecutor abstractingExecutor;
    final public static int EMBEDDING = 1;
    final public static int ABSTRACT = 2;



    static MainDbController getMainDbController() {
        return mainDbController;
    }

    public static void setMainDbController(MainDbController mainDbController) {
        InstantInfo.mainDbController = mainDbController;
    }


    public static void showErrorMessage(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText("检测到错误");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showMessage(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText("成功！");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean confirmCreateSecretTable(MainDbController mainDbController){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("创建密钥表");
        alert.setHeaderText("提示");
        alert.setContentText("检测到密钥表尚未创建，将自动建立密钥表。");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            mainDbController.createStoredKeyTable();
            return true;
        } else {
            return false;
        }
    }

    public static boolean isIsTextDataType() {
        return isTextDataType;
    }

    public static void setIsTextDataType(boolean isTextDataType) {
        InstantInfo.isTextDataType = isTextDataType;
    }

    public static String[] getEmbeddingResultInfo() {
        return embeddingResultInfo;
    }

    public static void setEmbeddingResultInfo(String[] embeddingResultInfo) {
        InstantInfo.embeddingResultInfo = embeddingResultInfo;
    }
//
//    public static PriorityQueue<TargetSimilarity> getAbstractResultInfo() {
//        return abstractResultInfo;
//    }
//
//    public static void setAbstractResultInfo(PriorityQueue<TargetSimilarity> abstractResultInfo) {
//        InstantInfo.abstractResultInfo = abstractResultInfo;
//    }

    public static int getLoginType() {
        return loginType;
    }

    public static void setLoginType(int LOGIN_TYPE) {
        loginType = LOGIN_TYPE;
    }

    public static boolean isIsFirst() {
        return isFirst;
    }

    public static void setIsFirst(boolean isFirst) {
        InstantInfo.isFirst = isFirst;
    }

    public static String getFile() {
        return file;
    }

    public static void setFile(String file) {
        InstantInfo.file = file;
    }

    public static SubDbController getSubDbController() {
        return subDbController;
    }

    public static void setSubDbController(SubDbController subDbController) {
        InstantInfo.subDbController = subDbController;
    }
    public static AbstractingExecutor getAbstractingExecutor() {
        return abstractingExecutor;
    }

    public static void setAbstractingExecutor(AbstractingExecutor abstractingExecutor) {
        InstantInfo.abstractingExecutor = abstractingExecutor;
    }

}

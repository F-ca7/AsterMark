package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectController extends Stage {
    public TextField tf_conn_ip;
    public TextField tf_conn_port;
    public TextField tf_conn_username;
    public PasswordField tf_conn_pwd;
    public Button btn_jumpTest;

//    public void testConnection(){
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle("");
//        alert.setHeaderText("连接失败");
//        alert.setContentText("请检查用户名密码是否正确，或mysql服务是否已启动。");
//        alert.showAndWait();
//        tf_conn_ip.setText("123123");
//
//    }
    @FXML
    protected void jumpToChoosingPage(ActionEvent event) throws IOException{
//        Parent Operation_Parent = FXMLLoader.load(getClass().getResource("/ChoosingFunction.fxml"));
//        Scene Operation_Creating_Scene = new Scene(Operation_Parent, 720, 500);
//        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        CreateOperation_Stage.setResizable(false);
//        CreateOperation_Stage.setTitle("功能选择");
//        CreateOperation_Stage.hide();
//        CreateOperation_Stage.setScene(Operation_Creating_Scene);
//        CreateOperation_Stage.show();
    }

    public boolean connectDB(){
        return false;
    }


}

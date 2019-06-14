package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ImportDataSourceController {
    @FXML
    public void getDataFromFile(ActionEvent event)throws IOException{
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV file", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog((Stage) ((Node) event.getSource()).getScene().getWindow());
        if(file == null){
            InstantInfo.showErrorMessage("请选择csv文件");
        }else{

            InstantInfo.setFile(file.toString());
            jumpToCSVSetPage(event);
        }
    }


    @FXML
    public void getDataFromDataBase(ActionEvent event) throws IOException{
        InstantInfo.setLoginType(InstantInfo.ABSTRACT);
        jumpToLoginPage(event);
    }

    public void jumpToCSVSetPage(ActionEvent event)throws IOException {
        Parent Operation_Parent = FXMLLoader.load(getClass().getResource("/SetCSVHead.fxml"));
        Scene Operation_Creating_Scene = new Scene(Operation_Parent, 500, 251);
        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CreateOperation_Stage.setResizable(false);
        CreateOperation_Stage.setTitle("CSV文件设置");
        CreateOperation_Stage.hide();
        CreateOperation_Stage.setScene(Operation_Creating_Scene);
        CreateOperation_Stage.show();
    }
    public void jumpToLoginPage(ActionEvent event)throws IOException {
        Parent Operation_Parent = FXMLLoader.load(getClass().getResource("/LoginPage.fxml"));
        Scene Operation_Creating_Scene = new Scene(Operation_Parent, 448, 423);
        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CreateOperation_Stage.setResizable(false);
        CreateOperation_Stage.setTitle("登录数据库");
        CreateOperation_Stage.hide();
        CreateOperation_Stage.setScene(Operation_Creating_Scene);
        CreateOperation_Stage.show();
    }

}

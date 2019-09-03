package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class ChoosingFunctionController extends Stage {
    public Button btn_helpDoc;

    @FXML
    public void jumpToEmbeddingPage(ActionEvent event) throws IOException {
        Parent Operation_Parent = FXMLLoader.load(getClass().getResource("/Embedding.fxml"));
        Scene Operation_Creating_Scene = new Scene(Operation_Parent, 1250, 780);
        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CreateOperation_Stage.setResizable(false);
        CreateOperation_Stage.setTitle("嵌入水印");
        CreateOperation_Stage.hide();
        CreateOperation_Stage.setScene(Operation_Creating_Scene);
        CreateOperation_Stage.show();
    }

    @FXML
    public void jumpToImportDataSource(ActionEvent event) throws IOException {
        Parent Operation_Parent = FXMLLoader.load(getClass().getResource("/ImportDataSource.fxml"));
        Scene Operation_Creating_Scene = new Scene(Operation_Parent, 720, 500);
        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CreateOperation_Stage.setResizable(false);
        CreateOperation_Stage.setTitle("导入数据源");
        CreateOperation_Stage.hide();
        CreateOperation_Stage.setScene(Operation_Creating_Scene);
        CreateOperation_Stage.show();
    }
//
    @FXML
    public void jumpToHelpDoc(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText("详细操作请参考软件使用文档");
        alert.showAndWait();
    }
}

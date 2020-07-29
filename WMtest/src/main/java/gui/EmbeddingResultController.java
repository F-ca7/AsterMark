package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import team.aster.database.MainDbController;

import java.io.File;
import java.io.IOException;

public class EmbeddingResultController extends Stage {
    public Label lbl_numberEmbeddingInfo;
    public Label lbl_numberEmbeddingInfoMean;
    public Label lbl_numberEmbeddingInfoVar;
    public RadioButton rbtn_currentDataBase;
    public RadioButton rbtn_csvFile;

    @FXML
    public void initialize(){
        lbl_numberEmbeddingInfo.setText(InstantInfo.getEmbeddingResultInfo()[0]);
        lbl_numberEmbeddingInfoMean.setText(InstantInfo.getEmbeddingResultInfo()[1]);
        lbl_numberEmbeddingInfoVar.setText(InstantInfo.getEmbeddingResultInfo()[2]);
    }

    @FXML
    public void beginPublish(ActionEvent event) throws IOException {
        MainDbController mainDbController = InstantInfo.getMainDbController();
        if(checkInputRight()) {
            // 开始发布，发布位置根据btn_currentDataBase和btn_csvFile的值确定
            if (rbtn_csvFile.isSelected()){
                // 发布到文件
                // 设置保存路径
                DirectoryChooser directoryChooser=new DirectoryChooser();
                File file = directoryChooser.showDialog((Stage)((Node)event.getSource()).getScene().getWindow());
                if(file==null){
                    InstantInfo.showErrorMessage("请选择有效路径！");
                    return;
                }
                String path = file.getPath();//选择的文件夹路径
                mainDbController.exportEmbeddedDataset(path);
            }
            if (rbtn_currentDataBase.isSelected()){
                mainDbController.publishDataset();
            }
            //发布完成，弹出信息提示框，给用户消息提醒
            InstantInfo.showMessage("发布完成！");
            jumpToChoosingPage(event);
        }
    }

    private boolean checkInputRight() {
        if(!rbtn_csvFile.isSelected()&&!rbtn_currentDataBase.isSelected()){
            InstantInfo.showErrorMessage("必须选择数据发布方式");
            return false;
        }
        return true;
    }

    private void jumpToChoosingPage(javafx.event.ActionEvent event) throws IOException {
        Parent operationParent = FXMLLoader.load(getClass().getResource("/ChoosingFunction.fxml"));
        Scene operationCreatingScene = new Scene(operationParent, 720, 500);
        Stage createOperationStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        createOperationStage.setResizable(false);
        createOperationStage.setTitle("功能选择");
        createOperationStage.hide();
        createOperationStage.setScene(operationCreatingScene);
        createOperationStage.show();
    }
}

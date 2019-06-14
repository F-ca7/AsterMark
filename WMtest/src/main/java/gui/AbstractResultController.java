package gui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import team.aster.model.TargetSimilarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class AbstractResultController extends Stage{
    public TableView tbv_abstractInfo;
    public Button btn_save;
    public Button btn_cancel;

    @FXML
    public void save(ActionEvent event)throws IOException{
        // 保存提取信息
        DirectoryChooser directoryChooser=new DirectoryChooser();
        File file = directoryChooser.showDialog((Stage)((Node)event.getSource()).getScene().getWindow());
        if(file==null){
            InstantInfo.showErrorMessage("请选择有效路径！");
            return;
        }
        String path = file.getPath();//选择的文件夹路径
        InstantInfo.getAbstractingExecutor().publishDecodingResultToFile(path);
        InstantInfo.showMessage("保存成功！");
        //跳转返回功能选择界面 还是返回提取界面？
        jump(event, "/ChoosingFunction.fxml",720, 500,"功能选择");
    }
    @FXML
    public void cancel(ActionEvent event)throws IOException{
        jump(event, "/ChoosingFunction.fxml",720, 500,"功能选择");
    }

    private void jump(ActionEvent event,String fxmlName,double width,double height, String title) throws IOException {
        Parent Operation_Parent = FXMLLoader.load(getClass().getResource(fxmlName));
        Scene Operation_Creating_Scene = new Scene(Operation_Parent, width,height);
        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CreateOperation_Stage.setResizable(false);
        CreateOperation_Stage.setTitle(title);
        CreateOperation_Stage.hide();
        CreateOperation_Stage.setScene(Operation_Creating_Scene);
        CreateOperation_Stage.show();
    }

    public void initialize(){
        // 添加解码信息
        PriorityQueue<TargetSimilarity> queue = InstantInfo.getAbstractingExecutor().getSimilarityPriorityQueue();
        ArrayList<String> columnNames = new ArrayList<>();
        columnNames.add("嵌入信息");
        columnNames.add("相似度");
        //add colName
        for (int i = 0; i < columnNames.size(); i++) {
            final int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(
                    columnNames.get(i)
            );
            column.setCellValueFactory(param ->
                    new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx))
            );
            column.setMaxWidth(400);
            tbv_abstractInfo.getColumns().add(column);
        }

        for (TargetSimilarity similarity:queue){
            System.out.println(similarity);
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(similarity.getTargetInfo());
            tmp.add(Double.toString(similarity.getSimilarity()));
            tbv_abstractInfo.getItems().add(
                    FXCollections.observableArrayList(tmp)
            );
        }

    }
}

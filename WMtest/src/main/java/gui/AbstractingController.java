package gui;

import javafx.application.Platform;
import team.aster.model.AbstractInfo;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import team.aster.AbstractingExecutor;
import team.aster.database.MainDbController;
import team.aster.database.SubDbController;
import team.aster.model.TargetSimilarity;
import team.aster.utils.Constants.EmbedDbInfo;
import team.aster.utils.PageUtils;

import java.io.IOException;
import java.util.*;

public class AbstractingController extends Stage {
    private final String DATATYPE_INTEGER= EmbedDbInfo.DATATYPE_INTEGER;
    private final String DATATYPE_FLOAT= EmbedDbInfo.DATATYPE_FLOAT;
    private final String DATATYPE_TEXT= EmbedDbInfo.DATATYPE_TEXT;
    private final int PAGE_SIZE = 1000;
    private int curPage;
    public TableView tbv_dataTable;

    public ComboBox chb_embeddingColumnName;
    public ComboBox chb_keyColumnName;
    public ComboBox chb_dataType;
    public ComboBox chb_embeddingMethod;
    public ComboBox chb_sourceDataTable;
    public ComboBox chb_originDataTable;
    public Button btn_beginEmbedding;
    public Button btn_prePage;
    public Button btn_nextPage;
    public Label lbl_pageNumber;
    public ProgressIndicator pgs_indicator;

    //设置数据表
    private void setTBVDataTable(){
        tbv_dataTable.getItems().clear();
        tbv_dataTable.getColumns().clear();
        SubDbController subDbController = InstantInfo.getSubDbController();
        ArrayList<String> columnNames = subDbController.getFieldNameList();
        ArrayList<ArrayList<String>> data = subDbController.getDataset();
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
            tbv_dataTable.getColumns().add(column);
        }

        // add data
        setTbv_tableData(data);
    }
    private void setTbv_tableData(ArrayList<ArrayList<String>> data){
        tbv_dataTable.getItems().clear();
        for (int i = 0; i <data.size(); i++) {
            tbv_dataTable.getItems().add(
                    FXCollections.observableArrayList(data.get(i))
            );
        }
    }

    @FXML
    public void nextPage(){
        SubDbController subDbController = InstantInfo.getSubDbController();
        curPage++;
        lbl_pageNumber.setText("第"+curPage+"页");
        ArrayList<ArrayList<String>> data = PageUtils.getPagedDataset(subDbController.getDataset(), PAGE_SIZE,curPage);
        setTbv_tableData(data);
        if(PageUtils.hasNextPage(subDbController.getDataset(), PAGE_SIZE,curPage)){
            btn_nextPage.setDisable(false);
        }else btn_nextPage.setDisable(true);
        btn_prePage.setDisable(false);
    }

    @FXML
    public void prePage(){
        SubDbController subDbController = InstantInfo.getSubDbController();
        curPage--;
        lbl_pageNumber.setText("第"+curPage+"页");
        ArrayList<ArrayList<String>> data = PageUtils.getPagedDataset(subDbController.getDataset(), PAGE_SIZE,curPage);
        setTbv_tableData(data);
        if(curPage==1){
            btn_prePage.setDisable(true);
        }else{
            btn_prePage.setDisable(false);
        }
        btn_nextPage.setDisable(false);
    }

    //Input Selection Set
    public void setCHBSourceDataTable() {
        chb_sourceDataTable.getItems().clear();
        // 获取该数据库中的所有表名
        SubDbController subDbController = InstantInfo.getSubDbController();
        ArrayList<String> tableNames = subDbController.getTableNameList();
        chb_sourceDataTable.getItems().addAll(tableNames);
        chb_sourceDataTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                pgs_indicator.setVisible(true);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        curPage=1;
                        SubDbController subDbController = InstantInfo.getSubDbController();
                        subDbController.setTableName(newValue.toString());
                        ArrayList<ArrayList<String>> data = PageUtils.getPagedDataset(subDbController.getDataset(), PAGE_SIZE,curPage);
                        Platform.runLater(()->{
                            reSetCHB();
                            lbl_pageNumber.setText("第"+curPage+"页");
                            setTbv_tableData(data);
                            if(PageUtils.hasNextPage(subDbController.getDataset(), PAGE_SIZE,curPage)){
                                btn_nextPage.setDisable(false);
                            }else btn_nextPage.setDisable(true);

                            if(curPage==1){
                                btn_prePage.setDisable(true);
                            }else{
                                btn_prePage.setDisable(false);
                            }
                            pgs_indicator.setVisible(false);
                        });
                    }
                });
                thread.start();
            }
        });
    }
    private void setCHBEmbeddingColumnName(String tableName){
        chb_embeddingColumnName.getItems().clear();
        // 获取该数据表中的所有字段名
        SubDbController subDbController = InstantInfo.getSubDbController();
        ArrayList<String> columnNames = subDbController.getFieldNameList();

        chb_embeddingColumnName.getItems().addAll(columnNames);
    }
    private void setCHBKeyColumnName(String tableName){
        chb_keyColumnName.getItems().clear();
        // 获取该数据表中的主键字段名
        SubDbController subDbController = InstantInfo.getSubDbController();
        ArrayList<String> columnNames = subDbController.getFieldNameList();

        chb_keyColumnName.getItems().addAll(columnNames);
    }
    private void setCHBDataType(){
        chb_dataType.getItems().clear();
        chb_dataType.getItems().addAll(DATATYPE_INTEGER, DATATYPE_FLOAT, DATATYPE_TEXT);
        chb_dataType.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(DATATYPE_INTEGER.equals(chb_dataType.getValue())||DATATYPE_FLOAT.equals(chb_dataType.getValue())){
                    chb_embeddingMethod.setDisable(false);
                    setCHBNumEmbeddingMethod();
                }else if(DATATYPE_TEXT.equals(chb_dataType.getValue())){
                    chb_embeddingMethod.setDisable(false);
                    setCHBTxtEmbeddingMethod();
                }else{
                    chb_embeddingMethod.setDisable(true);
                }

            }
        });
    }
    private void setCHBNumEmbeddingMethod(){
        chb_embeddingMethod.getItems().clear();
        chb_embeddingMethod.getItems().addAll(EmbedDbInfo.NUMERIC_METHOD_LSB, EmbedDbInfo.NUMERIC_METHOD_PATTERN_SEARCH);
    }
    private void setCHBTxtEmbeddingMethod(){
        chb_embeddingMethod.getItems().clear();
        chb_embeddingMethod.getItems().addAll(EmbedDbInfo.TEXT_METHOD_SPACE,
                EmbedDbInfo.TEXT_METHOD_PUNCTUATION, EmbedDbInfo.TEXT_METHOD_POS);
    }


    //Start Embedding
    @FXML
    private void beginAbstracting(ActionEvent event) throws IOException{
        if(checkInputRight()) {
            pgs_indicator.setVisible(true);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    AbstractInfo abstractInfo = getAbstractInfo();
                    SubDbController subDbController = InstantInfo.getSubDbController();
                    AbstractingExecutor abstractingExecutor = new AbstractingExecutor(abstractInfo);
                    InstantInfo.setAbstractingExecutor(abstractingExecutor);

                    // 开始提取数据，获取提取信息和对应的相似度，用优先队列封装
                    abstractingExecutor.startAbstracting(InstantInfo.getMainDbController(), subDbController);
                    //PriorityQueue<TargetSimilarity> similarityPriorityQueue = abstractingExecutor.getSimilarityPriorityQueue();
                    //setAbstractResultInfo(similarityPriorityQueue);
                    Platform.runLater(()->{
                        pgs_indicator.setVisible(false);
                        try {
                            openResultInfo(event);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
            thread.start();
            //提取完成后调用该函数，弹出信息提示框
            openResultInfo(event);
        }
    }

//    private void setAbstractResultInfo(PriorityQueue<TargetSimilarity> result) {
//        InstantInfo.setAbstractResultInfo(result);
//    }

    private void openResultInfo(ActionEvent event) throws IOException{
        Parent Operation_Parent = FXMLLoader.load(getClass().getResource("/AbstractResult.fxml"));
        Scene Operation_Creating_Scene = new Scene(Operation_Parent, 500, 340);
        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CreateOperation_Stage.setResizable(false);
        CreateOperation_Stage.setTitle("提取完毕");
        CreateOperation_Stage.hide();
        CreateOperation_Stage.setScene(Operation_Creating_Scene);
        CreateOperation_Stage.show();
    }


    private boolean checkInputRight(){
        if(chb_sourceDataTable.getValue()==null){
            InstantInfo.showErrorMessage("待提取字段名不能为空");
            return false;
        }
        if(chb_embeddingColumnName.getValue()==null){
            InstantInfo.showErrorMessage("待提取字段名不能为空");
            return false;
        }
        if(chb_keyColumnName.getValue()==null){
            InstantInfo.showErrorMessage("指定主键字段不能为空");
            return false;
        }
        if(chb_dataType.getValue()==null){
            InstantInfo.showErrorMessage("数据类型不能为空");
            return false;
        }
        if(chb_embeddingMethod.getValue()==null){
            InstantInfo.showErrorMessage("嵌入方式不能为空");
            return false;
        }
        return true;
    }
    private AbstractInfo getAbstractInfo(){
        String s_sourceDataTable = chb_originDataTable.getValue().toString();
        String s_embeddingColumnName = chb_embeddingColumnName.getValue().toString();
        String s_keyColumnName = chb_keyColumnName.getValue().toString();
        String s_dataType = chb_dataType.getValue().toString();
        String s_embeddingMethod = chb_embeddingMethod.getValue().toString();
        return new AbstractInfo(s_sourceDataTable,s_embeddingColumnName,s_keyColumnName,s_dataType,s_embeddingMethod);

    }

    //ReUsed Function
    private void setAllClear(){
        chb_embeddingMethod.getItems().clear();
        chb_embeddingColumnName.getItems().clear();
        chb_keyColumnName.getItems().clear();
        chb_dataType.getItems().clear();
        chb_dataType.setDisable(false);
        chb_keyColumnName.setDisable(false);
        chb_embeddingColumnName.setDisable(false);
        chb_embeddingMethod.setDisable(false);
    }
    private void reSetCHB() {
        setAllClear();
        // 传递数据表名称
        String tableName = chb_sourceDataTable.getValue().toString();
        SubDbController subDbController = InstantInfo.getSubDbController();
        subDbController.setTableName(tableName);
        setCHBEmbeddingColumnName(tableName);
        setCHBKeyColumnName(tableName);
        setTBVDataTable();
        setCHBDataType();
    }

    @FXML
    public void initialize() {
        setCHBSourceDataTable();
        setCHBOriginDataTable();
        chb_embeddingColumnName.setDisable(true);
        chb_keyColumnName.setDisable(true);
        chb_dataType.setDisable(true);
        chb_embeddingMethod.setDisable(true);
        pgs_indicator.setVisible(false);
        pgs_indicator.setProgress(-1);
    }

    private void setCHBOriginDataTable() {
        MainDbController mainDbController = InstantInfo.getMainDbController();
        ArrayList<String> dbTableNames = mainDbController.getDbTableNameList();
        chb_originDataTable.getItems().addAll(dbTableNames);
    }
}

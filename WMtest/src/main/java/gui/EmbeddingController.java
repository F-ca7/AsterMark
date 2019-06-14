package gui;

import javafx.application.Platform;
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
import team.aster.EmbeddingExecutor;
import team.aster.database.MainDbController;
import team.aster.model.EmbeddingInfo;
import team.aster.model.WatermarkException;
import team.aster.utils.PageUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static team.aster.utils.Constants.*;

public class EmbeddingController extends Stage {
    private static final int PAGE_SIZE = 1000;
    private final String DATATYPE_INTEGER= EmbedDbInfo.DATATYPE_INTEGER;
    private final String DATATYPE_FLOAT= EmbedDbInfo.DATATYPE_FLOAT;
    private final String DATATYPE_TEXT= EmbedDbInfo.DATATYPE_TEXT;
    private int curPage;

    public TableView tbv_dataTable;

    public ComboBox chb_embeddingColumnName;
    public ComboBox chb_keyColumnName;
    public ComboBox chb_dataType;
    public ComboBox chb_embeddingMethod;
    public ComboBox chb_precision;
    public TextField txtf_upperBound;
    public TextField txtf_lowerBound;
    public TextField txtf_embeddingMessage;
    public ListView lstv_dataTable;
    public Button btn_beginEmbedding;
    public Button btn_prePage;
    public Button btn_nextPage;
    public Label lbl_pageNumber;
    public Label lbl_databaseName;
    public ProgressIndicator pgs_indicator;

    private void setTBVDataTable(ArrayList<String> columnNames, ArrayList<ArrayList<String>> data){
        tbv_dataTable.getColumns().clear();
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

    //Input Selection Set
    private void setCHBEmbeddingColumnName(){
        chb_embeddingColumnName.getItems().clear();
        // 获取该数据表中的所有字段名
        ArrayList<String> columnNames = InstantInfo.getMainDbController().getFieldNameList();
        chb_embeddingColumnName.getItems().addAll(columnNames);
    }
    private void setCHBKeyColumnName(){
        chb_keyColumnName.getItems().clear();
        // 获取该数据表中的主键字段名
        ArrayList<String> columnNames = InstantInfo.getMainDbController().getFieldNameList();

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
                    chb_precision.setDisable(false);
                    txtf_upperBound.setDisable(false);
                    txtf_lowerBound.setDisable(false);
                    setCHBNumEmbeddingMethod();
                    setCHBPrecision();
                }else if(DATATYPE_TEXT.equals(chb_dataType.getValue())){
                    chb_embeddingMethod.setDisable(false);
                    chb_precision.getItems().clear();
                    chb_precision.setDisable(true);
                    txtf_upperBound.setText("");
                    txtf_lowerBound.setText("");
                    txtf_upperBound.setDisable(true);
                    txtf_lowerBound.setDisable(true);
                    setCHBTxtEmbeddingMethod();
                }else{
                    chb_embeddingMethod.setDisable(true);
                    chb_precision.setDisable(true);
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

    private void setCHBPrecision(){
        chb_precision.getItems().clear();
        chb_precision.getItems().addAll("10","1","0.1","0.01","0.001");
    }

    //ListView Set
    private void setLSTVDataTable(){

        MainDbController mainDbController = InstantInfo.getMainDbController();
        // 获取该数据库中所有的表名
        ArrayList<String> columnNames = mainDbController.getTableNameList();
        String databaseName = mainDbController.getDbName();
        lbl_databaseName.setText(databaseName);

        lstv_dataTable.getItems().addAll(columnNames);
        lstv_dataTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                // 先设置选中的表名
                pgs_indicator.setVisible(true);
                Thread thread = new Thread(() -> {
                    mainDbController.setTableName(newValue.toString());
                    ArrayList<String> columnNames1 = mainDbController.getFieldNameList();
                    curPage=1;
                    ArrayList<ArrayList<String>> data = PageUtils.getPagedDataset(mainDbController.getDataset(), PAGE_SIZE, curPage);
                    Platform.runLater(() -> {
                        //更新JavaFX的主线程的代码放在此处
                        lbl_pageNumber.setText("第"+curPage+"页");
                        if(PageUtils.hasNextPage(mainDbController.getDataset(), PAGE_SIZE, curPage)){
                            btn_nextPage.setDisable(false);
                        }else btn_nextPage.setDisable(true);
                        if(curPage==1){
                            btn_prePage.setDisable(true);
                        }else{
                            btn_prePage.setDisable(false);
                        }
                        reSetCHB();
                        setTBVDataTable(columnNames1,data);
                        pgs_indicator.setVisible(false);
                    });
                });
                thread.start();

                // 根据此时选中的表名，加载数据表中的数据，需要以ArrayList<String>形式给出列名



            }
        });
    }

    //Start Embedding
    @FXML
    private void beginEmbedding(ActionEvent event) throws IOException{
        if(checkInputRight()) {
            EmbeddingInfo embeddingInfo = getEmbeddingInfo();
            MainDbController mainDbController = InstantInfo.getMainDbController();
            EmbeddingExecutor embeddingExecutor = new EmbeddingExecutor(embeddingInfo);
            pgs_indicator.setProgress(-1);
            pgs_indicator.setVisible(true);
            Thread thread = new Thread(() -> {
                try {
                    embeddingExecutor.startEmbedding(mainDbController);
                } catch (WatermarkException e) {
                    e.printStackTrace();
                    Platform.runLater(()->{showErrorMessage(e.getMessage()+"\n继续使用会影响检测的准确性！");});

                }
                Platform.runLater(()->{
                        boolean isTextDataType = embeddingInfo.getDataType().equals(DATATYPE_TEXT);
                        double preMean = embeddingExecutor.getPreMean();
                        double preVar = embeddingExecutor.getPreVar();
                        double nowMean = embeddingExecutor.getNewMean();
                        double nowVar = embeddingExecutor.getNewVar();
                        setEmbeddingResultInfo(isTextDataType, preMean, preVar, nowMean, nowVar);

                        pgs_indicator.setVisible(false);
                        //嵌入完成后调用该函数，弹出信息提示框
                        try {
                            openResultInfo(event);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                });

            });
            thread.start();
        }
    }

    private void openResultInfo(ActionEvent event) throws IOException{
        Parent Operation_Parent = FXMLLoader.load(getClass().getResource("/EmbeddingResult.fxml"));
        Scene Operation_Creating_Scene = new Scene(Operation_Parent, 500, 340);
        Stage CreateOperation_Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        CreateOperation_Stage.setResizable(false);
        CreateOperation_Stage.setTitle("嵌入成功");
        CreateOperation_Stage.hide();
        CreateOperation_Stage.setScene(Operation_Creating_Scene);
        CreateOperation_Stage.show();
    }

    private void setEmbeddingResultInfo(boolean isTextDataType, double preMean, double preVar, double nowMean, double nowVar) {
        InstantInfo.setIsTextDataType(isTextDataType);
        if(isTextDataType){
            String[] info={"","",""};
            InstantInfo.setEmbeddingResultInfo(info);
        }else{
            String meanChange=getMeanChange(preMean,nowMean);
            String varChange=getVarChange(preVar,nowVar);
            String[] info= {"嵌入后，被嵌入数据的统计量变化如下",meanChange,varChange};
            InstantInfo.setEmbeddingResultInfo(info);
        }
    }

    private String getMeanChange(double preMean, double nowMean) {
        StringBuffer s = new StringBuffer("均值由 ");
        s.append(String.format("%.5f", preMean));
        s.append(" 变为 ");
        s.append(String.format("%.5f", nowMean));
        DecimalFormat rateFormat = new DecimalFormat("#.00%");
        String changeRate = rateFormat.format((Math.abs(((nowMean-preMean))/preMean)));
        s.append("\n变化比例为");
        s.append(changeRate);
        return s.toString();
    }
    private String getVarChange(double preVar, double nowVar) {
        StringBuffer s = new StringBuffer("方差由 ");
        s.append(String.format("%.5f", preVar));
        s.append(" 变为 ");
        s.append(String.format("%.5f", nowVar));
        DecimalFormat rateFormat = new DecimalFormat("#.0000%");
        String changeRate = rateFormat.format(Math.abs(((nowVar-preVar)/preVar)));
        s.append("\n变化比例为");
        s.append(changeRate);
        return s.toString();
    }

    private boolean checkInputRight(){
        if(chb_embeddingColumnName.getValue()==null){
            showErrorMessage("待嵌入字段名不能为空");
            return false;
        }
        if(chb_keyColumnName.getValue()==null){
            showErrorMessage("指定主键字段不能为空");
            return false;
        }
        if(chb_dataType.getValue()==null){
            showErrorMessage("数据类型不能为空");
            return false;
        }
        if(chb_embeddingMethod.getValue()==null){
            showErrorMessage("嵌入方式不能为空");
            return false;
        }
        String s_dataType = chb_dataType.getValue().toString();
        String s_embeddingMessage = txtf_embeddingMessage.getText();
        if(DATATYPE_INTEGER.equals(s_dataType)||DATATYPE_FLOAT.equals(s_dataType)){
            if(chb_precision.getValue()==null){
                showErrorMessage("精度不能为空");
                return false;
            }
            String s_upperBound = txtf_upperBound.getText();
            String s_lowerBound = txtf_lowerBound.getText();
            if(!isNumeric(s_upperBound)){
                showErrorMessage("变化范围上界非数字");
                return false;
            }
            if(!isNumeric(s_lowerBound)){
                showErrorMessage("变化范围下界非数字");
                return false;
            }
            double d_upperBound = Double.parseDouble(s_upperBound);
            double d_lowerBound = Double.parseDouble(s_lowerBound);
            if((d_upperBound-d_lowerBound)<1) {
                showErrorMessage("变化上下界应相差至少为1且上界大于下界");
                return false;
            }
            if(Double.isInfinite(d_upperBound)||Double.isInfinite(d_lowerBound)){
                showErrorMessage("变化上下界超出范围");
                return false;
            }
        }
        if(s_embeddingMessage.trim().equals("")){
            showErrorMessage("嵌入信息为空");
            return false;
        }
        return true;
    }


    private EmbeddingInfo getEmbeddingInfo(){
        String s_embeddingColumnName = chb_embeddingColumnName.getValue().toString();
        String s_keyColumnName = chb_keyColumnName.getValue().toString();
        String s_dataType = chb_dataType.getValue().toString();
        String s_embeddingMethod = chb_embeddingMethod.getValue().toString();
        String s_precision;
        if (chb_precision.getValue()==null){
            s_precision = "";
        }else {
            s_precision = chb_precision.getValue().toString();
        }
        double d_upperBound,d_lowerBound;
        if(DATATYPE_TEXT.equals(s_dataType)){
            d_upperBound=0;
            d_lowerBound=0;
        }else{
            d_upperBound = Double.parseDouble(txtf_upperBound.getText());
            d_lowerBound = Double.parseDouble(txtf_lowerBound.getText());
        }

        String s_embeddingMessage = txtf_embeddingMessage.getText();
        return new EmbeddingInfo(s_embeddingColumnName,s_keyColumnName,s_dataType,
                s_embeddingMethod,s_precision,d_upperBound,d_lowerBound,s_embeddingMessage);

    }

    //ReUsed Function
    private void setAllDisable(){
        chb_precision.setDisable(true);
        chb_embeddingMethod.setDisable(true);
        txtf_lowerBound.setDisable(true);
        txtf_upperBound.setDisable(true);
    }
    private void reSetCHB() {
        chb_embeddingMethod.getItems().clear();
        chb_precision.getItems().clear();
        txtf_upperBound.setText("");
        txtf_lowerBound.setText("");
        setAllDisable();
        setCHBEmbeddingColumnName();
        setCHBDataType();
        setCHBKeyColumnName();
    }

    @FXML
    public void nextPage(){
        MainDbController mainDbController = InstantInfo.getMainDbController();
        curPage++;
        lbl_pageNumber.setText("第"+curPage+"页");
        ArrayList<ArrayList<String>> data = PageUtils.getPagedDataset(mainDbController.getDataset(), PAGE_SIZE,curPage);
        setTbv_tableData(data);
        if(PageUtils.hasNextPage(mainDbController.getDataset(), PAGE_SIZE,curPage)){
            btn_nextPage.setDisable(false);
        }else btn_nextPage.setDisable(true);
        btn_prePage.setDisable(false);
    }

    public void prePage(){
        MainDbController mainDbController = InstantInfo.getMainDbController();
        curPage--;
        lbl_pageNumber.setText("第"+curPage+"页");
        ArrayList<ArrayList<String>> data = PageUtils.getPagedDataset(mainDbController.getDataset(), PAGE_SIZE,curPage);
        setTbv_tableData(data);
        if(curPage==1){
            btn_prePage.setDisable(true);
        }else{
            btn_prePage.setDisable(false);
        }
        btn_nextPage.setDisable(false);
    }
    private boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    //Message Alert
    private void showErrorMessage(String message){
        InstantInfo.showErrorMessage(message);
    }
    @FXML
    public void initialize() {
        btn_prePage.setDisable(true);
        btn_nextPage.setDisable(true);
        pgs_indicator.setProgress(-1);
        pgs_indicator.setVisible(false);
        setLSTVDataTable();
        setAllDisable();
    }
}

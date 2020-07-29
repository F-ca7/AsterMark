package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import team.aster.database.SubDbController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public  class SetCSVHeadController{
    public void chooseYes(ActionEvent event) throws IOException{
        InstantInfo.setIsFirst(true);
        jump(event, "/Abstracting.fxml",1250,780,"提取水印");
    }
    public void chooseNo(ActionEvent event)throws IOException{
        InstantInfo.setIsFirst(false);
        jump(event, "/Abstracting.fxml",1250,780,"提取水印");
    }
    private void jump(ActionEvent event, String fxmlName, double width, double height, String title) throws IOException {
        SubDbController subDbController = new SubDbController();
        parseCSVtoDatasetInDb(subDbController, InstantInfo.getFile(), InstantInfo.isIsFirst());
        InstantInfo.setSubDbController(subDbController);

        Parent operationParent = FXMLLoader.load(getClass().getResource(fxmlName));
        Scene operationCreatingScene = new Scene(operationParent, width,height);
        Stage createOperationStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        createOperationStage.setResizable(false);
        createOperationStage.setTitle(title);
        createOperationStage.hide();
        createOperationStage.setScene(operationCreatingScene);
        createOperationStage.show();
    }
    private void parseCSVtoDatasetInDb(SubDbController subDbController, String filePath , boolean withHeader) {
        ArrayList<String> tableNames = subDbController.getTableNameList();
        // 设置表名为文件名
        String[] names = StringUtils.split(filePath, "\\");
        tableNames.add(names[names.length-1]);
        ArrayList<String> fieldNames = subDbController.getFieldNameList();
        ArrayList<ArrayList<String>> dataset = subDbController.getDataset();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath));
            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(isr);
            ArrayList<String> row;
            int i = 0;
            int len;
            for (CSVRecord record : records) {
                len = record.size();
                if (i==0){
                    fieldNames.ensureCapacity(len);
                    if (withHeader){
                        // 写到头部
                        for (String data : record) {
                            fieldNames.add(data);
                        }
                    } else {
                        // 默认头部
                        for (int j=0; j<len; j++){
                            fieldNames.add("field"+j);
                        }
                        row = new ArrayList<>(len);
                        for (String data : record) {
                            row.add(data);
                        }
                        dataset.add(row);
                    }
                } else {
                    row = new ArrayList<>(len);
                    for (String data : record) {
                        row.add(data);
                    }
                    dataset.add(row);
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
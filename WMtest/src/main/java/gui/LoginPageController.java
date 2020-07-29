package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import team.aster.database.MainDbController;
import team.aster.database.SubDbController;

import javax.sound.midi.Soundbank;
import java.io.IOException;

public class LoginPageController extends Stage{
    public TextField txtf_ipAddress;
    public TextField txtf_port;
    public TextField txtf_userName;
    public PasswordField txtf_password;
    public TextField txtf_databaseName;

//    final private String IP_ADDRESS="ip";
//    final private String PORT="port";
//    final private String USERNAME="username";
//    final private String PASSWORD="password";
//    final private String DATATABLE_NAME="datatable";

    @FXML
    public void jumpToChoosingPage(ActionEvent event)throws IOException{
        if(checkInputRight()){
//            Map<String,String> info = getInputStrings();
            // 连接数据库，若成功，执行跳转动作
            if(InstantInfo.getLoginType()==InstantInfo.EMBEDDING){
                //此时为第一次登陆，获取到数据库（包含待嵌入数据表、密钥表）跳转动作 跳转至选择页面
                MainDbController mainDbController = new MainDbController();
                if (mainDbController.connectDB(txtf_databaseName.getText(), txtf_ipAddress.getText(), txtf_port.getText(),
                        txtf_userName.getText(), txtf_password.getText())){

                    // 判断是否存在密钥表
                    if (!mainDbController.isStoredKeyTableExist()){
                        // todo 没有则创建提示用户是否密钥表；是则创建并jump
                        if(!InstantInfo.confirmCreateSecretTable(mainDbController)){
                            return;
                        }

                    }

                    // 登陆成功
                    InstantInfo.setMainDbController(mainDbController);
                    jump(event, "/ChoosingFunction.fxml",720, 500,"功能选择");
                } else {
                    // 登陆失败
                    InstantInfo.showErrorMessage("连接失败");
                }
            }else{
                SubDbController subDbController = new SubDbController();
                if (subDbController.connectDB(txtf_databaseName.getText(), txtf_ipAddress.getText(), txtf_port.getText(),
                        txtf_userName.getText(), txtf_password.getText())){
                    // 连接成功
                    InstantInfo.setSubDbController(subDbController);
                    jump(event, "/Abstracting.fxml",1250, 780,"提取水印");
                }else {
                    // 连接失败
                    InstantInfo.showErrorMessage("连接失败");
                }

            }
        }
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

    private boolean checkInputRight(){
        if("".equals(txtf_ipAddress.getText().trim())){
            InstantInfo.showErrorMessage("连接IP地址尚未填写！");
            return false;
        }
        if("".equals(txtf_port.getText().trim())){
            InstantInfo.showErrorMessage("连接端口号尚未填写！");
            return false;
        }
        if("".equals(txtf_userName.getText().trim())){
            InstantInfo.showErrorMessage("用户名尚未填写！");
            return false;
        }
        if("".equals(txtf_password.getText().trim())){
            InstantInfo.showErrorMessage("用户密码尚未填写！");
            return false;
        }
        if("".equals(txtf_databaseName.getText().trim())){
            InstantInfo.showErrorMessage("数据库名尚未填写！");
            return false;
        }
        return true;
    }
//    public Map<String,String> getInputStrings(){
//        Map<String,String> info = new HashMap<>();
//        info.put(IP_ADDRESS,txtf_ipAddress.getText().trim());
//        info.put(PORT,txtf_port.getText().trim());
//        info.put(USERNAME,txtf_userName.getText().trim());
//        info.put(PASSWORD,txtf_password.getText().trim());
//        info.put(DATATABLE_NAME,txtf_databaseName.getText().trim());
//        return info;
//    }
}

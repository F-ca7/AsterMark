package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            InstantInfo.setLoginType(InstantInfo.EMBEDDING);
            Parent root = FXMLLoader.load(getClass().getResource("/LoginPage.fxml"));
            primaryStage.setTitle("连接到数据库");
            primaryStage.setScene(new Scene(root, 448, 423));
            primaryStage.setResizable(false);
            //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/aster_icon.png")));
            primaryStage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

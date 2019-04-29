package fxIllustratorColorParser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class IllustratorColorParserStandaloneApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("IllustratorColorParserGUI.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("FXColor-Parser");
        primaryStage.setScene(scene);

        primaryStage.getIcons().add(new Image(IllustratorColorParserStandaloneApp.class.getResourceAsStream("images/illustratorColorParserIcon500.png")));
        primaryStage.getIcons().add(new Image(IllustratorColorParserStandaloneApp.class.getResourceAsStream( "images/illustratorColorParserIcon300.png" )));
        primaryStage.getIcons().add(new Image(IllustratorColorParserStandaloneApp.class.getResourceAsStream( "images/illustratorColorParserIcon100.png" )));
        primaryStage.getIcons().add(new Image(IllustratorColorParserStandaloneApp.class.getResourceAsStream( "images/illustratorColorParserIcon50.png" )));

        primaryStage.show();
    }
}

package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class BaseController {

    public Stage openStage(String resourceRelatedPath) throws IOException {
        Stage stage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(resourceRelatedPath);
        fxmlLoader.setLocation(url);
        AnchorPane root = fxmlLoader.load(url.openStream());
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();

        return stage;
    }
}

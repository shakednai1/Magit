
import com.fxgraph.graph.PannableCanvas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;


public class MyApp extends Application{

    public static Stage stage;

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        // The path is relative to the MyApp.class and not MyApp.java
        URL url = getClass().getResource("app.fxml");
        fxmlLoader.setLocation(url);
        InputStream urlStream = url.openStream();
        Parent root = fxmlLoader.load(urlStream);

        AppController appController = fxmlLoader.getController();

        Scene scene = new Scene(root, 1200, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

        stage = primaryStage;
    }

}

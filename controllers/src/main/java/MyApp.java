
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        String executePath = new File("").getAbsolutePath();
//        String resourcePath = Paths.get(executePath ,"/controllers/src/main/java/app.fxml").toAbsolutePath().toString();


        String resourcePath = "app.fxml";
        URL url = getClass().getResource(resourcePath);
        fxmlLoader.setLocation(url);
        InputStream urlStream = url.openStream();
        Parent root = fxmlLoader.load(urlStream);

        AppController appController = fxmlLoader.getController();

        Scene scene = new Scene(root, 1200, 600);

//        ScrollPane scrollPane = (ScrollPane) scene.lookup("#scrollpaneContainer");
        PannableCanvas canvas = appController.getCommitTree().getTree().getCanvas();
        //canvas.setPrefWidth(100);
        //canvas.setPrefHeight(100);
        appController.commitTreeScroll.setContent(canvas);

        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.runLater(() -> {
            appController.getCommitTree().getTree().getUseViewportGestures().set(false);
            appController.getCommitTree().getTree().getUseNodeGestures().set(false);
        });


        stage = primaryStage;
    }

}

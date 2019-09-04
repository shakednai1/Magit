package workingCopy;

import com.fxgraph.graph.Graph;
import exceptions.NoActiveRepositoryError;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;

public class WorkingCopyStage {

    WorkingCopyController controller;

    public void display() throws NoActiveRepositoryError {

        Stage stage = new Stage();
        stage.setTitle("Working Copy Status");

        try{
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("../wc.fxml");
            fxmlLoader.setLocation(url);
            AnchorPane root = fxmlLoader.load(url.openStream());
            root.autosize();
            Scene scene = new Scene(root, 300, 400);

            controller = fxmlLoader.getController();
            controller.filesListView.autosize();
            controller.setFilesDelta();

            stage.setScene(scene);
            stage.show();

        }
        catch (IOException e){
            System.out.println("Error at loading Working Copy : " );
            e.printStackTrace();
        }
    }
}

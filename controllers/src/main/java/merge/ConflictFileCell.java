package merge;
import core.Blob;
import core.Common;
import core.FileChanges;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ConflictFileCell extends ListCell<FileChanges> {

    class ConflictResolveEvent implements EventHandler<MouseEvent> {

        public FileChanges item;
        public ListCell cell;

        ConflictResolveEvent(FileChanges item, ListCell cell){
          this.item = item;
          this.cell = cell;
        }

        public void handle(MouseEvent e) {

            String resourceRelatedPath = "../conflictResolve.fxml";

            try {
                Stage stage = new Stage();

                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource(resourceRelatedPath);
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Scene scene = new Scene(root);

                MergeController mergeController = fxmlLoader.getController();
                mergeController.execute(item, stage);


                stage.setScene(scene);
                stage.showAndWait();

                if (mergeController.resolved) {
                    cell.getListView().getItems().remove(mergeController.conflict);
                }

            } catch (IOException ex) {
            }

        }
    }


    public ConflictFileCell() {
    }

    @Override
    protected void updateItem(FileChanges item, boolean empty) {
        // calling super here is very important - don't skip this!
        super.updateItem(item, empty);

        if (item == null) return;

        setText(item.getFullPath());

        setOnMouseClicked(new ConflictResolveEvent(item, this));
    }
}
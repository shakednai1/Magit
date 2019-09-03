package workingCopy;


import core.Blob;
import core.Commit;
import core.Common;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class WorkingCopyCell extends ListCell<Blob> {

    public WorkingCopyCell() {
    }

    @Override
    protected void updateItem(Blob item, boolean empty) {
        // calling super here is very important - don't skip this!
        super.updateItem(item, empty);

        if (item == null) return;

        setText(item.getFullPath());

        Common.FilesStatus state = item.getState();
        setTextFill(state == Common.FilesStatus.NEW ? Color.GREEN :
                state == Common.FilesStatus.DELETED ? Color.RED :
                        state == Common.FilesStatus.UPDATED ? Color.CADETBLUE : Color.WHITE);

}
}
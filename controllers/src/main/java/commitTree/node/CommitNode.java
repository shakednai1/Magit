package commitTree.node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import models.CommitModel;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommitNode extends AbstractCell implements Comparable<CommitNode> {

    private CommitModel commit;

    private CommitNodeController commitNodeController;

    public CommitNode(CommitModel commit) {
        this.commit = commit;
    }

    public CommitModel getCommit(){ return commit; }

    @Override
    public Region getGraphic(Graph graph) {

        try {

            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("../../commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());

            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(commit.getMessage());
            commitNodeController.setCommitter(commit.getCommitter());
            commitNodeController.setCommitTimeStamp(commit.getCommitTime());

            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;
        String commitTime = commit.getCommitTime();
        return commitTime != null ? commitTime.equals(that.getCommit().getCommitTime()) : that.getCommit().getCommitTime() == null;
    }

    @Override
    public int hashCode() {
        return commit.getCommitTime() != null ? commit.getCommitTime().hashCode() : 0;
    }

    @Override
    public int compareTo(CommitNode other) {

        final DateFormat commitDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");
        Date comTime = null, otherTime = null;

        try{
            comTime = commitDateFormat.parse(commit.getCommitTime());
            otherTime = commitDateFormat.parse(other.getCommit().getCommitTime());
        }
        catch (Exception e) {/*cant be*/}
        return comTime.compareTo(otherTime);
    }

}

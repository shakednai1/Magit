package commitTree.node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import core.Settings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import models.CommitData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

public class CommitNode extends AbstractCell implements Comparable<CommitNode> {


    class PointingBranchListener implements ListChangeListener {

        public void onChanged(Change change){
            commitNodeController.setPointingBranches(commitData.getPointingBranchNames());
        }
    }

    private CommitData commitData;

    private CommitNodeController commitNodeController;

    public CommitNode(CommitData commitData) {
        this.commitData = commitData;
        commitData.getPointingBranches().addListener(new PointingBranchListener());
    }

    public CommitData getCommitData(){ return commitData; }

    @Override
    public Region getGraphic(Graph graph) {

        try {

            FXMLLoader fxmlLoader = new FXMLLoader();

            URL url = null;
            try{
                url = getClass().getClassLoader().getResource("commitNode.fxml");
            }
            catch (NullPointerException e){
                System.out.println("Cannot find resource ");
            }

            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());
            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitData(commitData);

            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        try{
            final Region graphic = graph.getGraphic(this);
            return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
        }
        catch( NullPointerException e){
            System.out.println("source "+ edge.getSource().toString());
            System.out.println("target "+ edge.getTarget().toString());
            throw e;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;
        String commitTime = commitData.getCommitTime();
        return commitTime != null ? commitTime.equals(that.getCommitData().getCommitTime()) : that.getCommitData().getCommitTime() == null;
    }

    @Override
    public int hashCode() {
        return commitData.getCommitTime() != null ? commitData.getCommitTime().hashCode() : 0;
    }

    @Override
    public int compareTo(CommitNode other) {

        final DateFormat commitDateFormat = Settings.commitDateFormat;
        Date comTime = null, otherTime = null;

        try{
            comTime = commitDateFormat.parse(commitData.getCommitTime());
            otherTime = commitDateFormat.parse(other.getCommitData().getCommitTime());
        }
        catch (Exception e) {/*cant be*/}
        return comTime.compareTo(otherTime);
    }

    public String toString(){
        return commitData.getSha1() + ", " + commitData.getMessage();
    }

}

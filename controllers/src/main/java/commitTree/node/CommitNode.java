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
            commitNodeController.setPointingBranches(commit.getPointingBranchNames());
        }
    }

    private CommitData commit;

    private CommitNodeController commitNodeController;

    public CommitNode(CommitData commit) {
        this.commit = commit;
    }

    public CommitData getCommit(){ return commit; }

    @Override
    public Region getGraphic(Graph graph) {

        try {

            FXMLLoader fxmlLoader = new FXMLLoader();

            File resourceFile = new File(Settings.getRunningPath().getAbsolutePath(), "commitNode.fxml");
            System.out.println(String.format("resource file : %s", resourceFile.getAbsolutePath()));
            URL url = null;
            try{


                url = getClass().getClassLoader().getResource("commitNode.fxml");
                System.out.println("Resource path: "+url.getPath());


            }
            catch (NullPointerException e){
                System.out.println("Cannot file resource ");
            }

            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());
            commitNodeController = fxmlLoader.getController();


            commit.getPointingBranches().addListener(new PointingBranchListener());

            commitNodeController.setCommitMessage(commit.getMessage());
            commitNodeController.setCommitter(commit.getCommitter());
            commitNodeController.setCommitTimeStamp(commit.getCommitTime());
            commitNodeController.setPointingBranches(commit.getPointingBranchNames());

            commitNodeController.setContextMenu();
            commitNodeController.setCommitSha1(commit.getSha1());
            commitNodeController.setPrevCommitSha1(commit.getPreviousCommitSha1(), commit.getSecondPreviousCommitSha1());

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
        String commitTime = commit.getCommitTime();
        return commitTime != null ? commitTime.equals(that.getCommit().getCommitTime()) : that.getCommit().getCommitTime() == null;
    }

    @Override
    public int hashCode() {
        return commit.getCommitTime() != null ? commit.getCommitTime().hashCode() : 0;
    }

    @Override
    public int compareTo(CommitNode other) {

        final DateFormat commitDateFormat = Settings.commitDateFormat;
        Date comTime = null, otherTime = null;

        try{
            comTime = commitDateFormat.parse(commit.getCommitTime());
            otherTime = commitDateFormat.parse(other.getCommit().getCommitTime());
        }
        catch (Exception e) {/*cant be*/}
        return comTime.compareTo(otherTime);
    }

    public String toString(){
        return commit.getSha1() + ", " + commit.getMessage();
    }

}

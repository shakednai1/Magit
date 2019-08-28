package commitTree;

import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import commitTree.layout.CommitTreeLayout;
import commitTree.node.CommitNode;
import models.CommitModel;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CommitTree {

    private Graph tree;
    private Map<String , CommitNode> commitCells = new Hashtable<>();

    public CommitTree(){
        tree = new Graph();
    }

    public Graph getTree() {return tree;}

    public void setCommitsTree(Map<String, CommitModel> repoCommits) {
        final Model model = tree.getModel();

        tree.beginUpdate();

        for(Map.Entry<String, CommitModel> commitEntry: repoCommits.entrySet()){
            CommitNode cell = new CommitNode(commitEntry.getValue());
            commitCells.put(commitEntry.getKey(), cell);
            model.addCell(cell);
        }

        for(CommitNode commitCell: commitCells.values()){
            String prevCommitSha1 = commitCell.getCommit().getPreviousCommitSha1();
            if(prevCommitSha1 != null){
                Edge edge = new Edge(commitCell, commitCells.get(prevCommitSha1));
                model.addEdge(edge);
            }
        }

        tree.endUpdate();

        tree.layout(new CommitTreeLayout());

    }

    private void addMoreCommits(Graph graph) {
        final Model model = graph.getModel();
        //graph.beginUpdate();
        ICell lastCell = model.getAllCells().get(4);

        // TODO add new cells
        // TODO add edge for between new and old cells

        graph.endUpdate();

        graph.layout(new CommitTreeLayout());
    }

}

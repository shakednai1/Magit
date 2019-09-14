package commitTree;

import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import commitTree.layout.CommitTreeLayout;
import commitTree.node.CommitNode;
import commitTree.node.CommitNodeController;
import models.CommitData;

import java.util.Hashtable;
import java.util.Map;




public class CommitTree {

    private Graph tree;
    private Map<String , CommitNode> commitCells = new Hashtable<>();

    public CommitTree(){
        tree = new Graph();
    }

    public Graph getTree() {return tree;}

    public void setCommitsTree(Map<String, CommitData> repoCommits) {
        final Model model = tree.getModel();
        model.clear(); //     TODO MOVE FROM HERE

        tree.beginUpdate();

        for(Map.Entry<String, CommitData> commitEntry: repoCommits.entrySet()){
            ICell cell = __addCommitToTree(commitEntry.getValue());
            model.addCell(cell);
        }

        for(CommitNode commitCell: commitCells.values()){
            String prevCommitSha1 = commitCell.getCommit().getPreviousCommitSha1();
            if(prevCommitSha1 != null && !prevCommitSha1.isEmpty()){
                Edge edge = new Edge(commitCell, commitCells.get(prevCommitSha1));
                model.addEdge(edge);
            }
        }

        tree.endUpdate();

        tree.layout(new CommitTreeLayout());



    }

    private ICell __addCommitToTree(CommitData commitData){
        CommitNode cell = new CommitNode(commitData);
        commitCells.put(commitData.getSha1(), cell);
        return cell;
    }

    public void addCommit(CommitData commitData){
        final Model model = tree.getModel();
        model.clear(); //     TODO MOVE FROM HERE


        ICell secondPrevCommit = null;
        ICell prevCommit = commitCells.get(commitData.getPreviousCommitSha1());
        if(commitData.getSecondPreviousCommitSha1() != null){
            secondPrevCommit = commitCells.get(commitData.getSecondPreviousCommitSha1());
        }

        ICell cell = __addCommitToTree(commitData);
        model.addCell(cell);

        Edge edge = new Edge(cell, prevCommit);
        model.addEdge(edge);
        if(secondPrevCommit != null) {
            Edge edge2 = new Edge(cell, secondPrevCommit);
            model.addEdge(edge2);
        }

        tree.endUpdate();

        tree.layout(new CommitTreeLayout());
    }

}

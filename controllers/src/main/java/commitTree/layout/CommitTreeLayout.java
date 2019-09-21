package commitTree.layout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import commitTree.node.CommitNode;
import javafx.scene.layout.Region;
import models.CommitData;

import java.util.*;
import java.util.stream.Collectors;

public class CommitTreeLayout implements Layout {

    static boolean showStandAloneCommits = false;

    final static int masterXLayout = 50;
    final static int gapBetweenBranchesLayout = 50;

    Map<String, CommitData> commits = new HashMap<>();

    Map<String, TempBranch> commitsToBranches = new HashMap<>();
    List<CommitNode> orderedNodes;

    class TempBranch{
        UUID uid;
        int xAsix = -1;
        CommitData firstCommit;

        TempBranch(){
            uid = UUID.randomUUID();
        }

        void setXAsix(int x){ xAsix = x;}

        void setFirstCommit(CommitData commit){ firstCommit = commit; }
    }

    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 10;
        int startY = 50;

        setCommits(cells);
        setOrderedNodes(cells);
        setBranchesLayout();

        for (CommitNode cn : orderedNodes) {
            TempBranch branch = commitsToBranches.get(cn.getCommitData().getSha1());
            if (branch == null) continue;

            Region cnGraph = graph.getGraphic(cn);
//            commitsGraphic.put(cn.getCommitData().getSha1(), cnGraph);
            cnGraph.relocate(branch.xAsix, startY);

            startY += 50;
        }
    }

    void setCommits(List<ICell> cells ){
        commits.clear();

        for(ICell _commit: cells){
            CommitNode commit = (CommitNode) _commit;
            commits.put(commit.getCommitData().getSha1(), commit.getCommitData());
        }
    }

    void setOrderedNodes(List<ICell> cells ){
        orderedNodes = cells.stream().map(c ->((CommitNode) c)).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    void setBranchesLayout(){
        List<TempBranch> openBranches = new ArrayList<>();

        int currentY = masterXLayout;
        TempBranch tempMaster = new TempBranch();
        tempMaster.setXAsix(currentY);


        for(CommitNode cell: orderedNodes){
            CommitData commitData = cell.getCommitData();

            __closeTempBranchesByFirstCommit(currentY, openBranches, commitData);

            if(commitData.getIsInMasterChain()){
                commitsToBranches.put(commitData.getSha1(), tempMaster);
                continue;
            }

            if(commitsToBranches.get(commitData.getSha1()) != null){
                continue;
            }

//
//            if(!showStandAloneCommits && commitData.getPointingBranches().isEmpty())
//                continue;

            TempBranch newBranch = new TempBranch();
            openBranches.add(newBranch);

            addCommitChainToTempBranch(commitData, newBranch);
        }
    }

    void addCommitChainToTempBranch(CommitData branchHead, TempBranch branch){

        CommitData currentCommit = branchHead;

        while(true && currentCommit != null){

            if(currentCommit.getIsInMasterChain())
                break;

            if(commitsToBranches.get(currentCommit.getSha1()) != null)
                break;

            // TODO HOW DOES IT LOOKS WITH getMerge after getMerge

            commitsToBranches.put(currentCommit.getSha1(), branch);
            currentCommit = commits.get(currentCommit.getPreviousCommitSha1());
        }

        branch.setFirstCommit(currentCommit);
    }

    void __closeTempBranchesByFirstCommit(int currentY, List<TempBranch> openBranches, CommitData currentCommit){

        for (TempBranch branch: openBranches){
            if(branch.firstCommit.getSha1().equals(currentCommit.getSha1())){
                currentY += gapBetweenBranchesLayout;
                branch.setXAsix(currentY);
            }
        }
        openBranches.removeIf((b)->(b.xAsix > 0));
    }

}

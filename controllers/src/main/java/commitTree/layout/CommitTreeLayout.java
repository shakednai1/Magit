package commitTree.layout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import commitTree.node.CommitNode;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// simple test for scattering commits in imaginary tree, where every 3rd node is in a new 'branch' (moved to the right)
public class CommitTreeLayout implements Layout {

    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 10;
        int startY = 50;

        List<CommitNode> orderedNodes = cells.stream().map(c ->((CommitNode) c)).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        for (CommitNode cn : orderedNodes) {
            if (cn.getCommit().getIsInMasterChain()) {
                graph.getGraphic(cn).relocate(startX, startY);
            }
            else {
                graph.getGraphic(cn).relocate(startX + 50, startY);
            }
            startY += 50;
        }
    }
}

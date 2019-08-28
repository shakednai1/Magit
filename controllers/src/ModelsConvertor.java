import models.CommitData;
import models.CommitModel;

public class ModelsConvertor {

    static CommitModel convertCommit(CommitData commit){
        return new CommitModel(commit.getSha1(), commit.getMessage(), commit.getCommitter(),
                commit.getCommitTime(), commit.getPreviousCommitSha1(), commit.getIsInMasterChain());
    }
}

import core.MainEngine;
import core.Repository;
import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "checkoutBranch", urlPatterns = "/branch/checkout")
public class checkoutBranch extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String branchName = req.getParameter("branchName");

        MainEngine engine = WebUtils.getSessionUser(req).getEngine();
        Repository repository = engine.getActiveRepository();

        try {

            try{
                repository.isValidBranchName(branchName);
            }
            catch (InvalidBranchNameError e){
                // not local branch - maybe remote?
                if (repository.getRemoteRepository().getBranchByName(branchName) != null) {
                    String sha1 = engine.getSha1FromRemoteBranch(branchName);
                    engine.createNewBranchFromSha1(branchName, sha1, true);
                }
            }

            repository.checkoutBranch(branchName, false);
        } catch (UncommittedChangesError | InvalidBranchNameError e) {
            System.out.println("branchName "+ branchName);
            e.printStackTrace();
            // TODO return exception
        }
    }
}

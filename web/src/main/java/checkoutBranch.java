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
        Repository repository = WebUtils.getSessionUser(req).getEngine().getActiveRepository();
        try {
            repository.checkoutBranch(branchName, false);
        } catch (UncommittedChangesError | InvalidBranchNameError e) {
            // TODO return exception
        }
    }
}

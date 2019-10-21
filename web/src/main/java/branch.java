import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core.Branch;
import core.Repository;
import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;
import models.BranchData;
import models.CommitData;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "branch", urlPatterns = "/branch")
public class branch extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Repository repository = WebUtils.getSessionUser(req).getEngine().getActiveRepository();
        String branchName = req.getParameter("branchName");
        try {
            repository.createNewBranch(branchName, false);
        } catch (UncommittedChangesError | InvalidBranchNameError e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Repository repository = WebUtils.getSessionUser(req).getEngine().getActiveRepository();
        Map<String, CommitData> commitsHistory = repository.getBranchCommits(new BranchData(repository.getActiveBranch()));
        Gson gson = new Gson();
        String jsonStr = gson.toJson(commitsHistory);
        resp.getWriter().println(jsonStr);
    }
}
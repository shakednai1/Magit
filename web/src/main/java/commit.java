import com.google.gson.JsonArray;
import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import core.Blob;
import core.Commit;
import core.Repository;
import core.Settings;
import exceptions.NoActiveRepositoryError;
import exceptions.NoChangesToCommitError;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "commit", urlPatterns = "/commit")
public class commit extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String commitSha1 = req.getParameter("commitSha1");
        Settings settings = WebUtils.getSessionUser(req).getEngine().getRepositoryManager().getSettings();
        Set<String> files = Commit.getAllFilesOfCommit(commitSha1, settings).keySet();
        JsonArray jsonFiles = new JsonArray();
        files.stream().forEach(f -> jsonFiles.add(settings.extractFilePath(f)));
        resp.getWriter().println(jsonFiles);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            WebUtils.getSessionUser(req).getEngine().commit(req.getParameter("commitMsg"));
        } catch (NoActiveRepositoryError | NoChangesToCommitError e) {
            resp.setStatus(400);
            resp.setContentType("text/plain");
            resp.getWriter().println(e.getMessage());
        }

    }
}

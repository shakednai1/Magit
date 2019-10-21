import core.Repository;
import core.Settings;
import user.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@WebServlet(name = "repository", urlPatterns = "/repository")
public class repository extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String repoName = req.getParameter("repoName");
        User user = WebUtils.getSessionUser(req);
        //Path repoFullPath = Paths.get(Settings.baseLocation.toString(), user.getName(), repoName);
        user.getEngine().changeActiveRepository(repoName);
        Cookie userCookie = new Cookie("user", user.getName());
        resp.addCookie(userCookie);
        resp.sendRedirect("http://localhost:8080/repository.jsp");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Repository repository = WebUtils.getSessionUser(req).getEngine().getActiveRepository();
        resp.getWriter().println(repository.getName());
        // TODO return repo details.
    }
}

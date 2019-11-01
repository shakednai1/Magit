import com.google.gson.*;
import core.RemoteRepository;
import core.Repository;
import models.BranchData;
import org.json.simple.JSONObject;
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
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        Gson gson = new GsonBuilder().create();
        Map<String, String> map = new HashMap<>();
        //map.put("redirectUrl", "http://localhost:8080/repository.jsp");
        map.put("redirectUrl", req.getContextPath() + "/repository.jsp");
        resp.getWriter().println((gson.toJson(map)));

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Repository repository = WebUtils.getSessionUser(req).getEngine().getActiveRepository();
        JsonObject res = new JsonObject();
        res.addProperty("repoName", repository.getName());
        res.addProperty("head", repository.getActiveBranch().getName());
        JsonArray localBranches = new JsonArray();
        repository.getAllBranches().stream().forEach(b -> localBranches.add(b.getName()));
        res.add("localBranches", localBranches);

        if(repository.isRemote()){
            JsonArray remoteBranches = new JsonArray();
            repository.getAllRemoteBranches().stream().forEach(b -> remoteBranches.add(b.getName()));
            res.add("remoteBranches", remoteBranches);

            RemoteRepository remoteRepository = repository.getRemoteRepository();

            res.addProperty("remoteFrom", remoteRepository.getPath().getAbsolutePath());
            res.addProperty("remoteName", remoteRepository.getName());
        }
        else{
            res.add("remoteBranches", null);
            res.add("remoteFrom", null);
            res.add("remoteName", null);

        }

        resp.getWriter().println(res.toString());
    }
}

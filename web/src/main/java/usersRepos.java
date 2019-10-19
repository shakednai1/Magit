import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import core.FSUtils;
import core.Settings;
import user.User;
import user.UserManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "usersRepos", urlPatterns = "/usersRepos")
public class usersRepos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        String username = req.getParameter("username");
        User user = UserManager.getUserByName(username);
        ArrayList<String> repos = user.getRepos();
        ArrayList<Map<String, String>> response = new ArrayList<>();
        for (String repo: repos){
            Map<String, String> repoDetails = new HashMap<>();
            File repoFolder = new File(Settings.baseLocation, repo);
            File branches = new File(repoFolder, "/.magit/branches");
            File headBranch = new File(branches, "HEAD");
            String aciveBranch = FSUtils.getFileLines(headBranch.getPath()).get(0);
            Integer numOfBranches = branches.listFiles().length - 1;
            File activeBranchFile = new File(branches, aciveBranch + ".txt");
            String lastCommit = FSUtils.getFileLines(activeBranchFile.getPath()).get(0);
            File commitFile = new File(repoFolder, "/.magit/objects/" + lastCommit + ".zip");
            // TODO get last commit time + commit msg
            repoDetails.put("name", repo);
            repoDetails.put("activeBranch", aciveBranch);
            repoDetails.put("numOfBranches", numOfBranches.toString());
            repoDetails.put("lastCommitTime", "TBD");
            repoDetails.put("lastCommitMessage", "TBD");
            response.add(repoDetails);
        }
        Gson gson = new GsonBuilder().create();
        JsonArray jarray = gson.toJsonTree(response).getAsJsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("response", jarray);
        out.println(jsonObject.toString());
    }
}

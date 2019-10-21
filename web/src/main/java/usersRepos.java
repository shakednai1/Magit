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
import java.util.*;

@WebServlet(name = "usersRepos", urlPatterns = "/usersRepos")
public class usersRepos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String username = req.getParameter("username");
        User user = UserManager.getUserByName(username);
        ArrayList<String> repos = user.getRepos();
        ArrayList<Map<String, String>> res = new ArrayList<>();
        for (String repo : repos) {
            Map<String, String> repoDetails = new HashMap<>();
            File repoFolder = new File(Settings.getRepoPathByUser(username, repo));
            File branches = Settings.getBranchFolderByRepo(repoFolder);

            Integer numOfBranches = branches.listFiles().length - 1;

            File headBranch = new File(branches, "HEAD");
            String activeBranch = FSUtils.getFileLines(headBranch.getPath()).get(0);
            File activeBranchFile = new File(branches, activeBranch + ".txt");
            String lastCommit = FSUtils.getFileLines(activeBranchFile.getPath()).get(0).split(Settings.delimiter)[0];

            String commitTime;
            String msg;
            if (!lastCommit.equals("null")) {
                List<String> content = FSUtils.getZippedContent(repoFolder + "/.magit/objects/", lastCommit);
                String[] commitData = content.get(0).split(Settings.delimiter);
                commitTime = commitData[commitData.length - 2];

                String[] msgParts = Arrays.copyOfRange(commitData, 3, commitData.length - 2);
                msg = String.join(Settings.delimiter, msgParts);
            } else {
                commitTime = "";
                msg = "";
            }

            repoDetails.put("name", repo);
            repoDetails.put("activeBranch", activeBranch);
            repoDetails.put("numOfBranches", numOfBranches.toString());
            repoDetails.put("lastCommitTime", commitTime);
            repoDetails.put("lastCommitMessage", msg);
            res.add(repoDetails);
        }
        Gson gson = new GsonBuilder().create();
        JsonArray jarray = gson.toJsonTree(res).getAsJsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("response", jarray);
        out.println(jsonObject.toString());
    }

}

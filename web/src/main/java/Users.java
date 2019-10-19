import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.simple.JSONArray;
import user.User;
import user.UserManager;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

@WebServlet(name = "users", urlPatterns = "/users")
public class Users extends HttpServlet {

    //get all users with their repos
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        Map<String, User> users = UserManager.getUsers(req.getParameter("onlyCreated").equals("true"));
        ArrayList<Map> response = new ArrayList<>();
        for(User user : users.values()){
            Map userData = new HashMap();
            userData.put("username", user.getName());
            ArrayList<Map> usersRepos = new ArrayList<>();
            userData.put("repositories", usersRepos);
            List<String> repos = user.getRepos();
            for(String repo: repos){
                Map repoDetails = new HashMap();
                repoDetails.put("name", repo);
                // TODO add more repo data
                usersRepos.add(repoDetails);
            }
            response.add(userData);
        }
        Gson gson = new GsonBuilder().create();
        JsonArray jarray = gson.toJsonTree(response).getAsJsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("response", jarray);
        out.println(jsonObject.toString());

    }
}

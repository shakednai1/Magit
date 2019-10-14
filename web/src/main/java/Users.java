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
        ArrayList<JSONObject> response = new ArrayList<JSONObject>();
        for(User user : users.values()){
            JSONObject userData = new JSONObject();
            userData.put("username", user.getName());
            ArrayList<JSONObject> usersRepos = new ArrayList<>();
            userData.put("repositories", usersRepos);
            List<String> repos = user.getRepos();
            for(String repo: repos){
                JSONObject repoDetails = new JSONObject();
                repoDetails.put("name", repo);
                usersRepos.add(repoDetails);
            }
            response.add(userData);
        }
        out.println(response);

    }
}

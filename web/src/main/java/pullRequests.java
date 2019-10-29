import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import user.User;
import pullRequest.PullRequest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/pull_requests")

public class pullRequests extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        User user = WebUtils.getSessionUser(request);

        if(user == null){
            System.out.println(request.getRequestURI() + ": No user given");
            return;
        }

        List<PullRequest> prs = user.getPullRequestsCurrentRepo();

        JsonObject res = new JsonObject();
        JsonArray prsStr = new JsonArray();
        res.add("response", prsStr);

        if(prs != null){
            for(PullRequest pr: prs){
                Gson gson = new Gson();
                prsStr.add(gson.toJson(pr));
            }
        }
        response.getWriter().println(res.toString());

    }
}
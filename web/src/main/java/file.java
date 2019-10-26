import com.google.gson.JsonObject;
import core.FSUtils;
import user.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet(name = "file", urlPatterns = "/file")
public class file extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        String queryString = request.getQueryString();
        String[] params = queryString.split(Pattern.quote("="));

        String path;
        String sha1;
        if (params[0].equals("path")) {
            path = params[1].split(Pattern.quote("&"))[0];
            sha1 = params[2];
        } else {
            path = params[2];
            sha1 = params[1].split(Pattern.quote("&"))[0];
        }
        path = path.replace("%20", " ");

        User user = WebUtils.getSessionUser(request);

        String content = String.join("\n",
                FSUtils.getZippedContent(user.getEngine().getRepositoryManager().getSettings().getRepositoryObjectsFullPath().getAbsolutePath(),
                        sha1));
        JsonObject res = new JsonObject();
        res.addProperty("path", path);
        res.addProperty("content", content);

        response.getWriter().println(res.toString());
    }

}
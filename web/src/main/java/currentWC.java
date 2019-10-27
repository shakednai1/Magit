import com.google.gson.Gson;
import com.google.gson.JsonArray;
import core.*;
import exceptions.NoActiveRepositoryError;
import user.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "currentWC", urlPatterns = "/currentWC/*")
public class currentWC extends HttpServlet {


    Map<String, String> files = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user =  WebUtils.getSessionUser(req);
        Settings settings = user.getEngine().getRepositoryManager().getSettings();
        if(req.getParameterMap().isEmpty()){
            files = user.getEngine().getAllFilesContentOfCurrentWC();
            JsonArray jsonFiles = new JsonArray();
            files.keySet().stream()
                    .forEach(f -> jsonFiles.add(settings.extractFilePath(f)));
            resp.getWriter().println(jsonFiles);
        }
        else{
            files = WebUtils.getSessionUser(req).getEngine().getAllFilesContentOfCurrentWC();
            String content= files.get(settings.getRepositoryFullPath()+ "\\"+ req.getParameter("fileName"));
            Gson gson = new Gson();
            String jsonStr = gson.toJson(content);
            resp.getWriter().println(jsonStr);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String content = req.getParameter("content");
            String filePath = req.getParameter("fileName");

            User user = WebUtils.getSessionUser(req);
            String repoName;
            try {
                repoName = user.getEngine().getCurrentRepoName();
            }
            catch (NoActiveRepositoryError e) {
                System.out.println("No Active Repository Error");
                return;
            }

            String fullPath = Settings.buildRepoFilePath(user.getName(), repoName, filePath);
            File file = new File(fullPath);
            if(file.exists()){
                FSUtils.writeFile(fullPath, content, false);
            }
            else{
                FSUtils.createNewFile(fullPath, content);
            }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String filePath = req.getRequestURI().split("currentWC/")[1];
        filePath = filePath.replace("%20", " ");

        User user =    WebUtils.getSessionUser(req);
        String repoName;
        try {
            repoName = user.getEngine().getCurrentRepoName();
        }
        catch (NoActiveRepositoryError e){
            System.out.println("No Active Repository Error");
            return;
        }
        String fullPath = Settings.buildRepoFilePath(user.getName(), repoName, filePath);

        FSUtils.deleteFile(fullPath);
    }

}

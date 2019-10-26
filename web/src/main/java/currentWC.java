import com.google.gson.Gson;
import com.google.gson.JsonArray;
import core.*;
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
        if(req.getParameterMap().isEmpty()){
            files = WebUtils.getSessionUser(req).getEngine().getAllFilesContentOfCurrentWC();
            JsonArray jsonFiles = new JsonArray();
            files.keySet().stream().forEach(f -> jsonFiles.add(f));
            resp.getWriter().println(jsonFiles);
        }
        else{
            files = WebUtils.getSessionUser(req).getEngine().getAllFilesContentOfCurrentWC();
            String content= files.get(req.getParameter("fileName"));
            Gson gson = new Gson();
            String jsonStr = gson.toJson(content);
            resp.getWriter().println(jsonStr);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String content = req.getParameter("content");
            String filePath = req.getParameter("fileName");
            String repoPath = WebUtils.getSessionUser(req).getEngine().getRepositoryManager().getSettings().getRepositoryFullPath();
            String fullPath = repoPath + "\\" + filePath;
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
        FSUtils.deleteFile(filePath);
    }

}

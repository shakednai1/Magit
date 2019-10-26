import core.MainEngine;
import core.Merge;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "collaboration", urlPatterns = "/collaboration")
public class collaboration extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        MainEngine engine = WebUtils.getSessionUser(req).getEngine();
        if(action.equals("push")){
            engine.push();
        }
        else{
            Merge merge = engine.pull();
            engine.getActiveRepo().makeFFMerge(merge);
        }
    }
}

import core.Settings;
import user.notification.ForkNotification;
import user.notification.Notification;
import user.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@WebServlet(name = "fork", urlPatterns = "/fork")
public class Fork extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String[]> params = request.getParameterMap();


        String repoName = params.get("repoName")[0];
        String fromUser = params.get("fromUser")[0];
        String forkName = params.get("forkName")[0];
        User user = WebUtils.getSessionUser(request);
        Path srcRepo = Paths.get(Settings.baseLocation.toString(), fromUser, repoName);
        Path dstRepo = Paths.get(Settings.baseLocation.toString(), user.getName(), forkName);
        user.getEngine().cloneRepo(srcRepo.toString(), dstRepo.toString(), forkName);
        user.addRepo(forkName);

        new ForkNotification(repoName,
                fromUser,
                user.getName()
        ).save();
    }
}

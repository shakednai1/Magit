import core.Settings;
import user.UserManager;
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
import java.util.List;
import java.util.Map;

@WebServlet(name = "userNotifications", urlPatterns = "/userNotifications")
public class userNotifications extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = WebUtils.getSessionUser(request);
        List<Notification> notifications = user.getNotifications(true, true);

        for(Notification notification: notifications){

//            response.add();
        }
//        response.add(repoDetails);


    }
}

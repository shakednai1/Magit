import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@WebServlet(name = "userNotifications", urlPatterns = "/userNotifications")
public class userNotifications extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = WebUtils.getSessionUser(request);

        if(user == null){
            System.out.println(request.getRequestURI() + ": No user given");
            return;
        }

        List<Notification> notifications = user.getNotifications(true, true);

        JsonObject res = new JsonObject();
        JsonArray notificationsRes = new JsonArray();
        res.add("notifications", notificationsRes);

        for(Notification notification: notifications){
            JsonObject notificationRes = new JsonObject();
            notificationRes.addProperty("show", notification.show());
            notificationRes.addProperty("type", notification.getType().name());

            notificationsRes.add(notificationRes);
        }

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        out.println(notificationsRes.toString());

    }
}

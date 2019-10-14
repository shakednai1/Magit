import user.User;
import user.UserManager;

import javax.servlet.http.HttpServletRequest;

public class WebUtils {

    static User getSessionUser(HttpServletRequest request){
        String userName = (String) request.getSession().getAttribute("user");
        return UserManager.getUsers(false).get(userName);
    }

}

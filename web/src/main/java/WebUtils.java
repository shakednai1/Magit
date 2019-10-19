import user.User;
import user.UserManager;

import javax.servlet.http.HttpServletRequest;

public class WebUtils {

    static User getSessionUser(HttpServletRequest request){
        String userName = (String) request.getCookies()[0].getValue();
        return UserManager.getUserByName(userName);
    }

}

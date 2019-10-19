import user.User;
import user.UserManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;

public class WebUtils {

    static User getSessionUser(HttpServletRequest request){
        Cookie userCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("user")).collect(Collectors.toList()).get(0);

        return UserManager.getUserByName(userCookie.getValue());
    }

}

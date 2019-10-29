import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import user.User;
import user.UserManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class WebUtils {

    static User getSessionUser(HttpServletRequest request){

        Cookie[] cookies =request.getCookies() ;

        int i;
        if (cookies == null)
            i=1;


        Cookie userCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("user")).collect(Collectors.toList()).get(0);

        return UserManager.getUserByName(userCookie.getValue());
    }

}

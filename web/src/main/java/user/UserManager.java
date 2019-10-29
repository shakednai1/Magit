package user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserManager {

    private static Integer prSerialNumber = 0;
    static Map<String, User> users = new HashMap<>();

    public static User addUser(String name, String password){

        if (users.get(name) == null) {
            users.put(name, new User(name, password));
        }
        return users.get(name);
    }

    public static Map<String, User> getUsers(boolean onlyCreated){
        if (!onlyCreated)
            return new HashMap<>(users);

        Map<String, User> res = new HashMap<>();
        for(User user: users.values()){
            if (!user.getRepos().isEmpty())
                res.put(user.name, user);
        }
        return res;
    }

    public static User getUserByName(String username){
        return users.get(username);
    }

    public static Integer generatePRNumber(){
        prSerialNumber += 1;
        return prSerialNumber;
    }

}

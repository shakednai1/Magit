package user;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserManager {

    static ArrayList<User> users = new ArrayList<User>();

    public static User addUser(String name, String password){
        Predicate<User>  nameExists = s -> s.getName().equals(name);
        if (users.stream().anyMatch(nameExists)){
            return users.stream().filter(nameExists).collect(Collectors.toList()).get(0);
        }
        User newUser = new User(name, password);
        users.add(newUser);
        return newUser;
    }

    public List<User> getUsers(boolean onlyCreated){
        return users.stream().filter(u -> !u.getRepos().isEmpty()).collect(Collectors.toList());
    }

}

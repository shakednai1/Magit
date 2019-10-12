package user;

import java.util.ArrayList;

public class User {

    public String getName() {
        return name;
    }

    String name;
    String password;
    ArrayList repos = new ArrayList();

    public ArrayList getRepos() {
        return repos;
    }

    User(String name, String password){
        this.name = name;
        this.password = password;
    }

    protected void addRepo(String repoName){
        repos.add(repoName);
    }

}

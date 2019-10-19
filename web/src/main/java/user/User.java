package user;

import core.MainEngine;

import java.util.ArrayList;

public class User {

    String name;
    String password;

    MainEngine engine;

    ArrayList<String> repos = new ArrayList();

    public String getName() {
        return name;
    }

    public ArrayList getRepos() {
        return repos;
    }

    User(String name, String password){
        this.name = name;
        this.password = password;

        this.engine = new MainEngine(this.name);
    }

    public void addRepo(String repoName){
        repos.add(repoName);
    }

    public MainEngine getEngine(){
        return engine;
    }

}

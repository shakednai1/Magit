package models;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class RepositoryModel {

    private StringProperty repoName = new SimpleStringProperty();
    private StringProperty repoPath = new SimpleStringProperty();

    private StringProperty remoteRepoPath = new SimpleStringProperty();


    public RepositoryModel(){
        setRepo("No repository", "");
        setRemoteRepo("Repository has no remote");
    }

    public StringProperty getRepoNameProperty(){ return repoName; }
    public StringProperty getRepoPathProperty(){ return repoPath; }
    public StringProperty getRemoteRepoPathProperty(){ return remoteRepoPath;}

    public void setRepo(String name, String path){
        repoName.set(name);
        repoPath.set(path);
    }

    public void setRemoteRepo(String path){
        remoteRepoPath.set(path);
    }
}

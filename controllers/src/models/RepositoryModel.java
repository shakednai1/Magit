package models;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class RepositoryModel {

    private StringProperty repoName = new SimpleStringProperty();
    private StringProperty repoPath = new SimpleStringProperty();

    public RepositoryModel(){setRepo("No repository", "");}

    public StringProperty getRepoNameProperty(){ return repoName; }
    public StringProperty getRepoPathProperty(){ return repoPath; }
    public void setRepo(String name, String path){
        repoName.set(name);
        repoPath.set(path);
    }

}

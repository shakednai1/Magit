package core;

import javafx.concurrent.Task;

public class LoadXmlTask extends Task {

    private String xmlFilePath;
    private MainEngine engine;

    public LoadXmlTask(String xmlFilePath, MainEngine engine){
        this.xmlFilePath = xmlFilePath;
        this.engine = engine;
    }

    @Override
    protected Object call() throws Exception {
        engine.loadRepositoryFromXML();
        return null;
    }
}

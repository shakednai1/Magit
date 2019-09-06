package commitTree.node;

import core.Item;

public class FSObject {
    private String sha1;
    private String displayName;
    private Boolean isRoot;

    public FSObject(Item item, boolean isRoot){
        this.isRoot = isRoot;
        sha1 = item.getSha1();
        this.displayName = isRoot ? item.getFullPath() : item.getName();
    }

    public String toString() {
        return displayName;
    }

    public String getSha1(){
        return sha1;
    }
}

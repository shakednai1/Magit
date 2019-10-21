package core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.List;

public class ItemSha1{

    String sha1;
    String content;

    File contentFolder;

    public ItemSha1(String strForSha1, boolean isContent, boolean saveContent, File contentFolder){
        sha1 = (isContent) ? getSha1FromContent(strForSha1): strForSha1;
        if(saveContent)
            content = strForSha1;
        this.contentFolder = contentFolder;
    }

    private static String getSha1FromContent(String content){
        return DigestUtils.sha1Hex(content);
    }

    String getContent(){
        if (content != null)
            return content;
        return String.join("\n", FSUtils.getZippedContent(contentFolder.getAbsolutePath(), sha1));
    }

    @Override
    public String toString(){ return sha1; }

    @Override
    public boolean equals(Object object){
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        ItemSha1 other = (ItemSha1) object;
        return sha1.equals(other.sha1) ;
    }
}
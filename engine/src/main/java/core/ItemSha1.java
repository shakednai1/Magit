package core;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

public class ItemSha1{

    String sha1;

    ItemSha1(String strForSha1, boolean isContent){
        sha1 = (isContent) ? getSha1FromContent(strForSha1): strForSha1;
    }

    private static String getSha1FromContent(String content){
        return DigestUtils.sha1Hex(content);
    }

    List<String > getContent(){
        return Utils.getZippedContent(sha1);
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;

import com.sun.javaws.exceptions.InvalidArgumentException;
import fromXml.*;
import fromXml.Item;

public class XmlLoader {

    private MagitRepository magitRepository;
    private MagitBlobs magitBlobs;
    private MagitBranches magitBranches;
    private MagitFolders magitFolders;
    private MagitCommits magitCommits;

    private Map<String, MagitBlob> blobMap = new HashMap<>();
    private Map<String, MagitSingleFolder> folderMap = new HashMap<>();
    private Map<String, MagitSingleCommit> commitMap = new HashMap<>();

    private String repositoryPath;


    public XmlLoader(String XmlPath){
        File file = new File(XmlPath);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MagitRepository.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            magitRepository = (MagitRepository) jaxbUnmarshaller.unmarshal(file);
            magitBranches = magitRepository.getMagitBranches();
            magitCommits = magitRepository.getMagitCommits();
            magitFolders = magitRepository.getMagitFolders();
            magitBlobs = magitRepository.getMagitBlobs();
            repositoryPath = magitRepository.getLocation();
        } catch (JAXBException e) {
            e.printStackTrace();
            // TODO - verify if the file is not exist or not .xml file - this exception is thrown
        }
//        MAGIT_DIR_PATH = this._path.concat("/.magit");
//        OBJECTS_DIR_PATH = MAGIT_DIR_PATH.concat("/Objects");
//        BRANCHES_DIR_PATH = MAGIT_DIR_PATH.concat("/Branches");
//        HEAD_PATH = BRANCHES_DIR_PATH.concat("/HEAD");
    }


    public void checkValidXml() throws XmlException {
        checkMagitBlolb();
        checkMagitCommits();
        checkMagitFodler();
        checkFolderPointers();
        checkCommitsPointers();
        checkBranchPointers();
        checkHeadPointer();
    }

    private void checkMagitBlolb() throws XmlException {
        Set<String> ids = new HashSet<>();
        for(MagitBlob blob: magitBlobs.getMagitBlob()){
            if (!ids.add(blob.getId())){
                throw new XmlException("There is duplicate ID in blobs. id : " + blob.getId());
            }
            blobMap.put(blob.getId(), blob);
        }
    }

    private void checkMagitFodler() throws XmlException {
        Set<String> ids = new HashSet<>();
        for(MagitSingleFolder folder: magitFolders.getMagitSingleFolder()){
            if (!ids.add(folder.getId())){
                throw new XmlException("There is duplicate ID in folders. id : " + folder.getId());
            }
            folderMap.put(folder.getId(), folder);
        }
    }

    private void checkMagitCommits() throws XmlException {
        Set<String> ids = new HashSet<>();
        for(MagitSingleCommit commit: magitCommits.getMagitSingleCommit()){
            if (!ids.add(commit.getId())){
                throw new XmlException("There is duplicate ID in Commits. id : " + commit.getId());
            }
            commitMap.put(commit.getId(), commit);
        }
    }


    private void checkFolderPointers() throws XmlException {
        for (MagitSingleFolder folder : folderMap.values()) {
            List<Item> items = folder.getItems().getItem();
            for (Item item : items) {
                String type = item.getType();
                String id = item.getId();
                if(type.equals("blob")) {
                    if (blobMap.get(id) == null) {
                        throw new XmlException("Folder id " + folder.getId() +
                                " points to non existing blob item (id : " + id + ")");
                    }
                }

                else if(type.equals("folder")){
                    if(id.equals(folder.getId())){
                        throw new XmlException("Folder id " + id + " points to itself");
                    }
                    if(folderMap.get(id) == null){
                        throw new XmlException("Folder id " + folder.getId() +
                                " points to non existing folder item (id : " + id + ")");
                    }
                }
            }
        }
    }

    private void checkCommitsPointers() throws XmlException {
        for(MagitSingleCommit commit : commitMap.values()){
            String folderId = commit.getRootFolder().getId();
            MagitSingleFolder folder = folderMap.get(folderId);
            if(folder == null){
                throw new XmlException("commit id " + commit.getId() +
                        " points to not existing folder (id : " + folderId + ")");
            }
            else{
                if(!folder.isIsRoot()){
                    throw new XmlException("commit id " + commit.getId() +
                            " points to not root folder (id : " + folderId + ")");
                }
            }

        }
    }

    private void checkBranchPointers() throws XmlException {
        for(MagitSingleBranch branch: magitBranches.getMagitSingleBranch()){
            if(commitMap.get(branch.getPointedCommit().getId()) == null){
                throw new XmlException("branch " + branch.getName() +
                        " points to non existing commit id : " + branch.getPointedCommit().getId());
            }
        }
    }

    private void checkHeadPointer() throws XmlException {
        boolean isFound = false;
        String head = magitBranches.getHead();
        for(MagitSingleBranch branch : magitBranches.getMagitSingleBranch()){
            if(branch.getName().equals(head)){
                isFound = true;
            }
        }
        if (!isFound){
            throw new XmlException("Head: " + head + " is not an existing branch");
        }
    }



}

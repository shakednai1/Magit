import javax.rmi.CORBA.Util;
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

    public void loadRepo(){
        RepositoryManager repositoryManager = new RepositoryManager();
        repositoryManager.createNewRepository(magitRepository.getLocation());
        // TODO verify
        MagitSingleCommit commit = null;
        for(MagitSingleBranch magitSingleBranch: magitBranches.getMagitSingleBranch()){
            if (magitBranches.getHead().equals(magitSingleBranch.getName())){
                commit = commitMap.get(magitSingleBranch.getPointedCommit().getId());
                break;
            }
        }
        openCommitRec(commit);
        // TODO connect bracnh to commits + open the head branch on file system
    }

    public String openCommitRec(MagitSingleCommit commit){
        List<PrecedingCommits.PrecedingCommit> precedingCommits =
                commitMap.get(commit.getId()).getPrecedingCommits().getPrecedingCommit();
        Commit commitObj = null;
        for(PrecedingCommits.PrecedingCommit precedingCommit : precedingCommits){
            MagitSingleCommit magitCommit = commitMap.get(precedingCommit.getId());
             if(!magitCommit.getPrecedingCommits().getPrecedingCommit().isEmpty()){
                String prevSha1 = openCommitRec(magitCommit);
                commitObj = openCommit(magitCommit.getId(), prevSha1);
             }
             else{
                 commitObj = openCommit(magitCommit.getId(), null);
             }
        }
        return commitObj.getCommitSHA1();
    }

    public Commit openCommit(String commitID, String prevCommit){
        MagitSingleCommit magitCommit = commitMap.get(commitID);
        MagitSingleFolder magitRootFolder = folderMap.get(magitCommit.getRootFolder().getId());
        String path = Settings.repositoryFullPath + "/" + magitRootFolder.getName();
        Folder rootFolder = createFilesTree(magitRootFolder, path);
        Commit commit = new Commit(magitCommit.getMessage(), rootFolder.getCurrentSHA1(), magitCommit.getAuthor(),
                magitCommit.getDateOfCreation(), prevCommit); // TODO get the previous commit
        for(MagitSingleBranch magitBranch : magitBranches.getMagitSingleBranch()){
            if (magitBranch.getPointedCommit().getId().equals(magitCommit.getId())){
                Branch branch = new Branch(magitBranch.getName(), commit, rootFolder);
                branch.commit(magitCommit.getMessage(), magitCommit.getAuthor(), magitCommit.getDateOfCreation(),
                        commit);
            }
        }
        Utils.clearCurrentWC();
        return commit;
    }

    private Folder createFilesTree(MagitSingleFolder magitRootFolder, String path){
        List<Item> items = magitRootFolder.getItems().getItem();
        Map<String, Blob> subBlobs = new HashMap<>();
        Map<String, Folder> subFolders = new HashMap<>();

        Folder rootFolder = new Folder(path, magitRootFolder.getName(), magitRootFolder.getLastUpdater(),
                magitRootFolder.getLastUpdateDate());
        File directory = new File(path);
        directory.mkdir();

        for( Item item : items){
            String itemId = item.getId();
            switch (item.getType()){
                case "blob":
                    MagitBlob magitBlob = blobMap.get(itemId);
                    Blob blob = new Blob(path, magitBlob.getName(), magitBlob.getContent(),
                            magitBlob.getLastUpdater(), magitBlob.getLastUpdateDate());
                    Utils.createNewFile(path + "/" + magitBlob.getName(), magitBlob.getContent());
                    subBlobs.put(blob.getCurrentSHA1(), blob);
                    break;
                case "folder":
                    MagitSingleFolder magitFolder = folderMap.get(itemId);
                    String folderPath = path +  "/" + magitFolder.getName();
                    Folder newFolder = createFilesTree(magitFolder, folderPath);
                    subFolders.put(newFolder.getCurrentSHA1(), newFolder);
                    break;
            }
        }
        rootFolder.setSubItems(subBlobs, subFolders);
        return rootFolder;
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

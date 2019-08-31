package core;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;

import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;
import exceptions.XmlException;
import fromXml.*;
import fromXml.Item;

class XmlLoader {

    private MagitRepository magitRepository;
    private MagitBlobs magitBlobs;
    private MagitBranches magitBranches;
    private MagitFolders magitFolders;
    private MagitCommits magitCommits;
    private String repositoryPath;
    private MagitRepository.MagitRemoteReference remoteReference;

    private Map<String, MagitBlob> blobMap = new HashMap<>();
    private Map<String, MagitSingleFolder> folderMap = new HashMap<>();
    private Map<String, MagitSingleCommit> commitMap = new HashMap<>();
    // only commits with preceding commits - without first commit
    private Map<String, List<MagitSingleCommit>> commitPointersMap = new HashMap<>();
    private MagitSingleCommit firstCommit;

    private Map<String, RemoteBranch> remoteBranchMap = new HashMap<>();

    RepositoryManager repositoryManager = MainEngine.getRepositoryManager();



    XmlLoader(String XmlPath) throws XmlException {
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
            throw new XmlException("Given file has no XML extension OR XML file not exist");
        }
    }


    void checkValidXml() throws XmlException {
        checkMagitBlolb();
        checkMagitCommits();
        checkMagitFodler();
        checkFolderPointers();
        checkCommitsPointers();
        checkBranchPointers();
        checkHeadPointer();
    }

    public void loadRepo() throws UncommittedChangesError, InvalidBranchNameError {
        //create empty repository
        repositoryManager.createNewRepository(repositoryPath, magitRepository.getName(), true);

        Utils.clearCurrentWC();

        setFirstCommit();
        if(firstCommit == null){
            repositoryManager.getActiveRepository().createNewBranch(magitBranches.getHead(), true);
        }
        else{
            buildCommitPointersMap();
            openCommitRec(firstCommit, null);
            repositoryManager.getActiveRepository().checkoutBranch(magitBranches.getHead(), true);
        }

        if(magitRepository.getMagitRemoteReference()!=null){
            repositoryManager.getActiveRepository().setRemoteRepositoryName(
                    magitRepository.getMagitRemoteReference().getName());
            repositoryManager.getActiveRepository().setRemoteRepositoryPath(
                    magitRepository.getMagitRemoteReference().getLocation());
        }
    }

    private void setFirstCommit() {
        for (MagitSingleCommit magitSingleCommit : magitCommits.getMagitSingleCommit()) {
            if (magitSingleCommit.getPrecedingCommits() == null ||
                    magitSingleCommit.getPrecedingCommits().getPrecedingCommit().isEmpty()) {
                firstCommit = magitSingleCommit;
                break;
            }
        }
    }

    private void buildCommitPointersMap() {
        for(MagitSingleCommit commit: magitCommits.getMagitSingleCommit()){
            commitPointersMap.put(commit.getId(), new ArrayList<>());
        }
        for(MagitSingleCommit commit: magitCommits.getMagitSingleCommit()){
            if(commit.getPrecedingCommits() == null) continue;
            if(commit.getPrecedingCommits().getPrecedingCommit().isEmpty() ) continue;
            for(PrecedingCommits.PrecedingCommit precedingCommit : commit.getPrecedingCommits().getPrecedingCommit()){
                    List<MagitSingleCommit> currentChilds = commitPointersMap.get(precedingCommit.getId());
                    currentChilds.add(commit);
                }
        }
    }


    void openCommitRec(MagitSingleCommit commit, String prevCommitSha1){
        // open my commit and than
        // search for the commits that the prev commit its me and open them
        Commit commitObj = openCommit(commit.getId(), prevCommitSha1);
        List<MagitSingleCommit> commitChilds = commitPointersMap.get(commit.getId());
        if(!commitChilds.isEmpty()){
            for(MagitSingleCommit child: commitChilds){
                openCommitRec(child, commitObj.getCommitSHA1());
            }
        }
    }

    Commit openCommit(String commitID, String prevCommit){
        MagitSingleCommit magitCommit = commitMap.get(commitID);
        MagitSingleFolder magitRootFolder = folderMap.get(magitCommit.getRootFolder().getId());
        Folder rootFolder = createFilesTree(magitRootFolder, Settings.repositoryFullPath);
        Commit commit = new Commit(magitCommit.getMessage(), rootFolder.getCurrentSHA1(), magitCommit.getAuthor(),
                magitCommit.getDateOfCreation(), prevCommit);


        List<MagitSingleBranch> pointingBranches = getPointedMagitBranch(magitCommit.getId(), false);
        List<MagitSingleBranch> pointingRemoteBranches = getPointedMagitBranch(magitCommit.getId(), true);

        if (!pointingRemoteBranches.isEmpty()) {
            for (MagitSingleBranch pointingRemoteBranch : pointingRemoteBranches) {
                RemoteBranch remoteBranch = new RemoteBranch(pointingRemoteBranch.getName(), commit.getCommitSHA1());
                repositoryManager.getActiveRepository().addRemoteBranch(remoteBranch);
            }
        }

        if (!pointingBranches.isEmpty()) {
            for (MagitSingleBranch pointingBranch : pointingBranches) {
                Branch branch = new Branch(pointingBranch.getName(), commit, rootFolder);
                repositoryManager.getActiveRepository().addNewBranch(branch);
                if (magitBranches.getHead().equals(pointingBranch.getName())) {
                    repositoryManager.getActiveRepository().setActiveBranch(branch);
                }
                if(pointingBranch.isTracking()){
                    RemoteBranch remoteBranch = findTrackingBranch(pointingBranch.getTrackingAfter());
                    branch.addTracking(remoteBranch.name);
                }
                rootFolder.zipRec();
            }
        }
        else{
            rootFolder.zipRec();
            commit.zipCommit();
        }

        Utils.clearCurrentWC();
        return commit;
    }

    private RemoteBranch findTrackingBranch(String name){
        for(RemoteBranch remoteBranch: repositoryManager.getActiveRepository().getAllRemoteBranches()){
            if (remoteBranch.name.equals(name)){
                return remoteBranch;
            }
        }
        return null;
    }

    private List<MagitSingleBranch> getPointedMagitBranch(String id, boolean remote){

        List<MagitSingleBranch> pointingBranches = new LinkedList<>();

        for(MagitSingleBranch magitBranch : magitBranches.getMagitSingleBranch()){
            if (magitBranch.getPointedCommit().getId().equals(id)){
                if(remote && magitBranch.isIsRemote()){
                    pointingBranches.add(magitBranch);
                }
                else if(!remote && !magitBranch.isIsRemote()){
                    pointingBranches.add(magitBranch);
                }
            }

        }
        return pointingBranches;
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
                    Blob blob = new Blob(path + "/" + magitBlob.getName(), magitBlob.getName(), magitBlob.getContent(),
                            magitBlob.getLastUpdater(), magitBlob.getLastUpdateDate());
                    Utils.createNewFile(path + "/" + magitBlob.getName(), magitBlob.getContent());
                    subBlobs.put(blob.fullPath, blob);
                    break;
                case "folder":
                    MagitSingleFolder magitFolder = folderMap.get(itemId);
                    String folderPath = path +  "/" + magitFolder.getName();
                    Folder newFolder = createFilesTree(magitFolder, folderPath);
                    subFolders.put(newFolder.fullPath, newFolder);
                    break;
            }
        }
        rootFolder.setSubItems(subBlobs, subFolders);
        rootFolder.setSHA1();
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
                        throw new XmlException("core.Folder id " + folder.getId() +
                                " points to non existing blob item (id : " + id + ")");
                    }
                }

                else if(type.equals("folder")){
                    if(id.equals(folder.getId())){
                        throw new XmlException("core.Folder id " + id + " points to itself");
                    }
                    if(folderMap.get(id) == null){
                        throw new XmlException("core.Folder id " + folder.getId() +
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
            if (branch.getPointedCommit().getId().equals("")) continue;

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

    String checkRepoLocation() throws XmlException {
        String repositoryLocation = magitRepository.getLocation();
        File repoLocation = new File(repositoryLocation);
        File magitRepoLocation = new File(repositoryLocation + "\\.magit");
        if (repoLocation.exists()) {
            if (!magitRepoLocation.exists()) {
                throw new XmlException("Cannot create new repository in " +
                        repositoryLocation + " ,already have existing files in this path");
            } else {
                return "Are you sure you want to override repository in " +
                        repositoryLocation + "? " + Settings.YNquestion;
            }
        }
         return null;
    }
}

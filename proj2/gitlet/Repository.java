package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author haya
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");
    public static final File REFS_FILE = join(GITLET_DIR,"refs");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");// commitId → Commit

    private static void saveCurrentBranch(String currentBranch) {
        writeContents(HEAD_FILE, currentBranch);
    }
    private static String getCurrentBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    private static void saveCommit(Commit commit,String sha) {

        final File commitFile = join(COMMITS_DIR,sha);
        try {
            if(!commitFile.createNewFile()){
                throw error("Failed to create commit file.");
            }
        } catch (IOException e) {
            System.out.println("Failed to create commit file.");
        };
        writeObject(commitFile,commit);
    }

    private static Commit getCommit(String sha){
        final File commitFile = join(COMMITS_DIR,sha);
        if(!commitFile.exists()){
            return null;
        }
        return readObject(commitFile,Commit.class);
    }

    private static void saveStage(Stage stage) {
        writeObject(STAGE_FILE, stage);
    }

    private static Stage getStage() {
        return readObject(STAGE_FILE,Stage.class);
    }
    private static void saveRefs(Map<String, String> branches) {
        writeObject(REFS_FILE, (Serializable) branches);
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, String> getRefs() {
         return readObject(REFS_FILE,HashMap.class);
    }

    private static String getCurrentCommitId() {
        String currentBranch=getCurrentBranch();
        HashMap<String, String> refs=getRefs();
        return refs.get(currentBranch);
    }
    private static Commit getCurrentCommit() {
        String currentCommitId=getCurrentCommitId();
        return getCommit(currentCommitId);
    }

    public static void init() {
        // 已存在（不论是文件还是目录）
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        // 不存在，尝试创建
        if (!GITLET_DIR.mkdirs()) {
            throw error("Failed to create .gitlet directory.");
        }

        //创建HEAD文件
        try {
            if(!HEAD_FILE.createNewFile()){
                throw error("Failed to create HEAD file.");
            }
        } catch (IOException e) {
            System.out.println("Failed to create HEAD file.");
        };
        String currentBranch= "master";
        saveCurrentBranch(currentBranch);


        //创建commits目录，用于存储commit
        if(!COMMITS_DIR.mkdirs()) {
            throw error("Failed to create commits directory.");
        }

        //存储初始commit message
        Commit commit = new Commit("initial commit",new Date(0L),new HashMap<>(),null);
        String sha = sha1(commit.toString());
        saveCommit(commit,sha);

        //创建blobs目录,用于存储已提交文件
        if(!BLOBS_DIR.mkdirs()) {
            throw error("Failed to create blobs directory.");
        }

        try{
            if(!STAGE_FILE.createNewFile()){
                throw error("Failed to create stage file.");
            }
        }catch (IOException e){
            throw error("Failed to create stage file.");
        }
        Stage stage = new Stage(new HashMap<String,String>(),new HashMap<String,String>());
        saveStage(stage);

        //创建refs文件，用于保存每个分支的head
        try{
            if(!REFS_FILE.createNewFile()){
                throw error("Failed to create refs file.");
            }
        }catch (IOException e){
            throw error("Failed to create refs file.");
        }
        Map<String, String> branches = new HashMap<>();
        branches.put(currentBranch, sha);
        saveRefs(branches);
        //创建refs文件，用于保存每个分支的head

    }
    static void add(String fileName){
        File file = join(CWD, fileName);
        if(!file.exists()){
            System.out.println("File does not exist.");
            return;
        }

        Stage stage = getStage();

        String oldBlobId = stage.getStashBlobId(fileName);
        if(oldBlobId!=null){
            File oldBlobFile = join(BLOBS_DIR,oldBlobId);
            if(!oldBlobFile.delete()){
                throw error("Failed to delete stash file "+fileName);
            }
            stage.unstageFile(fileName);
        }

        byte[] contents= readContents(file);
        String newBlobId = sha1((Object) contents);

        Commit currentCommit = getCurrentCommit();
        Map<String, String> commitFiles = currentCommit.getFileToBlobMap();

        //检查文件是否被修改
        if (commitFiles.containsKey(fileName) && Objects.equals(commitFiles.get(fileName), newBlobId)) {
            //文件未被修改
            stage.unremoveFile(fileName);
            saveStage(stage);
            return;
        }

        //文件被修改,将该文件加入blobs目录
        File stashFile = join(BLOBS_DIR,newBlobId);
        try{
            if(!stashFile.createNewFile()){
                byte[] fileContents= readContents(stashFile);
                if(!Arrays.equals(fileContents, contents)){
                    throw error("Failed to create new stash file "+fileName);
                }
            }
        }catch (IOException e){
            throw error("Failed to create new stash file "+fileName);
        }
        stage.stageFile(fileName,newBlobId);
        saveStage(stage);

//        Map<String,String> addedFiles = stage.getAddFiles();
//        Map<String,String> removeFiles = stage.getRemoveFiles();
//        System.out.println("addedFiles:"+addedFiles.toString());
//        System.out.println("removeFiles:"+removeFiles.toString());

        writeContents(stashFile, (Object) contents);
    }

    public static void commit(String message){
        Stage stage = getStage();
        Map<String,String> addedFiles = stage.getAddFiles();
        Map<String,String> removeFiles = stage.getRemoveFiles();

        //确认是否有修改
        if(addedFiles.isEmpty() && removeFiles.isEmpty()){
            System.out.println("No changes added to the commit.");
            return;
        }
//        System.out.println("addedFiles:"+addedFiles.toString());
//        System.out.println("removeFiles:"+removeFiles.toString());

        //生成新的commit
        Commit newCommit = getNewCommit(message, addedFiles, removeFiles);

        //保存commit
        String commitId = sha1(newCommit.toString());
        saveCommit(newCommit,commitId);

        //更新分支HEAD
        HashMap<String, String>refs =getRefs();
        String currentBranch= getCurrentBranch();
        refs.put(currentBranch,commitId);
        saveRefs(refs);

        //清除stash文件
        stage.clear();
        saveStage(stage);
    }

    private static Commit getNewCommit(String message, Map<String, String> addedFiles, Map<String, String> removeFiles) {
        Commit currentCommit = getCurrentCommit();
        Date timestamp = new Date();
        Map<String,String> commitFiles = currentCommit.getFileToBlobMap();
        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            commitFiles.put(key,value);
        }

        for(String fileName: removeFiles.keySet()){
            commitFiles.remove(fileName);
        }
        String currentCommitId=getCurrentCommitId();
        return new Commit(message,timestamp,commitFiles,currentCommitId);
    }

    public static void rm(String fileName){
        Stage stage = getStage();
        String oldBlobId = stage.getStashBlobId(fileName);
        boolean stageIsChanged =false;
        if(oldBlobId != null){
            File oldBlobFile = join(BLOBS_DIR,oldBlobId);
            if(!oldBlobFile.delete()){
                throw error("Failed to delete stash file "+fileName);
            }
            stage.unstageFile(fileName);
            stageIsChanged =true;
        }
        Commit currentCommit = getCurrentCommit();
        Map<String,String> commitedFile = currentCommit.getFileToBlobMap();
        if(commitedFile.containsKey(fileName)){
            File oldBlobFile = join(CWD, fileName);
            if(oldBlobFile.exists()&&!oldBlobFile.delete()){
                throw error("Failed to delete file "+fileName);
            }
            stage.removeFile(fileName,oldBlobId);
            stageIsChanged=true;
        }
        if(stageIsChanged){
//            Map<String,String> addedFiles = stage.getAddFiles();
//            Map<String,String> removeFiles = stage.getRemoveFiles();
//            System.out.println("addedFiles:"+addedFiles.toString());
//            System.out.println("removeFiles:"+removeFiles.toString());
            saveStage(stage);
        }else{
            System.out.println("No reason to remove the file.");
        }
    }

    private static void printCommit(Commit commit,String commitId){
        System.out.println("===");
        System.out.println("commit "+commitId);
        SimpleDateFormat sdf =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        String formatted = sdf.format(commit.getTimestamp());
        System.out.println("Date: "+formatted);
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void log() {
        String currentCommitId = getCurrentCommitId();
        while(currentCommitId!=null){
            Commit currentCommit = getCommit(currentCommitId);
            printCommit(currentCommit,currentCommitId);
            currentCommitId = currentCommit.getParent();
        }
    }

    public static void globalLog() {
        List<String> fileNames = plainFilenamesIn(COMMITS_DIR);

        if (fileNames != null) {
            for(String fileName: fileNames){
                Commit commit = getCommit(fileName);
                printCommit(commit,fileName);
            }
        }
    }

    public static void find(String message){
        List<String> fileNames = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;

        if (fileNames != null) {
            for(String fileName: fileNames){
                Commit commit = getCommit(fileName);
                if(commit.getMessage().equals(message)){
                    found = true;
                    System.out.println(fileName);
                }
            }
        }
        if(!found){
            System.out.println("Found no commit with that message.");
        }
    }

    private static void getFileStatus(Set<String> workFiles,TreeMap<String,String> modifiedFiles,TreeSet<String> untrackedFiles){
        Stage stage = getStage();
        Map<String,String> addedFiles = stage.getAddFiles();
        Map<String,String> removeFiles = stage.getRemoveFiles();
        Commit newCommit = null;

        //确认是否有修改
        if(addedFiles.isEmpty() && removeFiles.isEmpty()){
            newCommit= getCurrentCommit();
        }else{
            newCommit = getNewCommit("",addedFiles,removeFiles);
        }

        Set<String> union = new HashSet<>(workFiles);
        Set<String> commitFiles = newCommit.getFileToBlobMap().keySet();
        union.addAll(commitFiles);

        for(String fileName: union){
            if(workFiles.contains(fileName) && !commitFiles.contains(fileName)){
                untrackedFiles.add(fileName);
            }else if(!workFiles.contains(fileName) && commitFiles.contains(fileName)){
                modifiedFiles.put(fileName,"deleted");
            }else{
                File workFile = join(CWD,fileName);
                String workFileContents= readContentsAsString(workFile);
                String hash = sha1(workFileContents);
                if(!hash.equals(newCommit.getFileToBlobMap().get(fileName))){
                    modifiedFiles.put(fileName,"modified");
                }
            }
        }

    }

    public static void status(){
        //打印分支信息
        HashMap<String, String>refs =getRefs();
        String currentBranch= getCurrentBranch();
        System.out.println("=== Branches ===");
        for(String branchName: refs.keySet()){
            if(branchName.equals(currentBranch)){
                System.out.println("*"+branchName);
            }else{
                System.out.println(branchName);
            }
        }
        System.out.println();


        //打印stage文件
        Stage stage = getStage();
        System.out.println("=== Stages Files ===");
        Map<String, String> addFiles= stage.getAddFiles();
        for(String fileName: addFiles.keySet()){
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Map<String, String> removedFiles= stage.getRemoveFiles();
        for(String fileName: removedFiles.keySet()){
            System.out.println(fileName);
        }
        System.out.println();

        //获取modified和untracked文件
        TreeMap<String,String> modifiedFiles=new TreeMap<>();
        TreeSet<String> untrackedFiles=new TreeSet<>();
        List<String > fileNames = plainFilenamesIn(CWD);
        TreeSet<String>workfileNames = new TreeSet<>();
        if(fileNames!=null){
            workfileNames =new TreeSet<>(fileNames);
        }

        getFileStatus(workfileNames,modifiedFiles,untrackedFiles);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (Map.Entry<String, String> entry : modifiedFiles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key+ " ("+value+")");
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for(String fileName: untrackedFiles){
            System.out.println(fileName);
        }
        System.out.println();
    }

    public static void checkoutFile(String fileName,String CommitId){
        if(CommitId==null){
            CommitId = getCurrentCommitId();
        }

        Commit commit = getCommit(CommitId);
        if(commit==null){
            System.out.println("No commit with that id exists.");
        }

        Map<String,String> commitFiles = commit.getFileToBlobMap();
        if(!commitFiles.containsKey(fileName)){
            System.out.println("File does not exist in that commit.");
        }

        String blobId = commitFiles.get(fileName);
        File commitFile = new File(BLOBS_DIR,blobId);
        File file = new File(CWD,fileName);
        String contents= readContentsAsString(commitFile);
        writeContents(file,contents);
    }

    public static void checkoutBranch(String branch){
        HashMap<String, String>refs =getRefs();
        String currentBranch= getCurrentBranch();
        if(!refs.containsKey(branch)){
            System.out.println("No such branch exists.");
            return;
        }
        if(branch.equals(currentBranch)){
            System.out.println("No need to checkout the current branch.");
            return;
        }
        //获取modified和untracked文件
        TreeMap<String,String> modifiedFiles=new TreeMap<>();
        TreeSet<String> untrackedFiles=new TreeSet<>();
        List<String > fileNames = plainFilenamesIn(CWD);
        TreeSet<String>workfileNames = new TreeSet<>();
        if(fileNames!=null){
            workfileNames =new TreeSet<>(fileNames);
        }

        getFileStatus(workfileNames,modifiedFiles,untrackedFiles);
        if(!untrackedFiles.isEmpty()){
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first");
            return;
        }

        Stage stage = getStage();
        stage.clear();
        saveStage(stage);

        for(String fileName: workfileNames){
            File workFile = new File(CWD,fileName);
            if(!workFile.delete()){
                throw error("Failed to delete file "+fileName);
            }
        }

        String commitId = refs.get(branch);
        Commit commit = getCommit(commitId);
        Map<String,String> commitFiles = commit.getFileToBlobMap();
        for (Map.Entry<String, String> entry : commitFiles.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            File commitFile = new File(BLOBS_DIR,blobId);
            String contents= readContentsAsString(commitFile);
            File workFile = new File(CWD,fileName);
            try{
                if(!workFile.createNewFile()){
                    throw error("Failed to create refs file.");
                }
            }catch (IOException e){
                throw error("Failed to create refs file.");
            }
            writeContents(workFile,contents);
        }
        currentBranch = branch;
        saveCurrentBranch(currentBranch);

    }
}

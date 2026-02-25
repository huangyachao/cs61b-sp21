package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *
 * @author haya
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");
    public static final File REFS_FILE = join(GITLET_DIR, "refs");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD"); // commitId → Commit

    private static void saveCurrentBranch(String currentBranch) {
        writeContents(HEAD_FILE, currentBranch);
    }

    private static String getCurrentBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    private static void saveCommit(Commit commit, String sha) {

        final File commitFile = join(COMMITS_DIR, sha);
        try {
            if (!commitFile.createNewFile()) {
                throw error("Failed to create commit file.");
            }
        } catch (IOException e) {
            System.out.println("Failed to create commit file.");
        }
        writeObject(commitFile, commit);
    }

    private static Commit getCommit(String sha) {
        final File commitFile = join(COMMITS_DIR, sha);
        if (!commitFile.exists()) {
            return null;
        }
        return readObject(commitFile, Commit.class);
    }

    private static void saveStage(Stage stage) {
        writeObject(STAGE_FILE, stage);
    }

    private static Stage getStage() {
        return readObject(STAGE_FILE, Stage.class);
    }

    private static void saveRefs(TreeMap<String, String> branches) {
        writeObject(REFS_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    private static TreeMap<String, String> getRefs() {
        return readObject(REFS_FILE, TreeMap.class);
    }

    private static String getCurrentCommitId() {
        String currentBranch = getCurrentBranch();
        TreeMap<String, String> refs = getRefs();
        return refs.get(currentBranch);
    }

    private static Commit getCurrentCommit() {
        String currentCommitId = getCurrentCommitId();
        return getCommit(currentCommitId);
    }

    public static boolean isInitialized() {
        return GITLET_DIR.exists();
    }

    public static void init() {
        // 不存在，尝试创建
        if (!GITLET_DIR.mkdirs()) {
            throw error("Failed to create .gitlet directory.");
        }

        //创建HEAD文件
        try {
            if (!HEAD_FILE.createNewFile()) {
                throw error("Failed to create HEAD file.");
            }
        } catch (IOException e) {
            System.out.println("Failed to create HEAD file.");
        }
        String currentBranch = "master";
        saveCurrentBranch(currentBranch);


        //创建commits目录，用于存储commit
        if (!COMMITS_DIR.mkdirs()) {
            throw error("Failed to create commits directory.");
        }

        //存储初始commit message
        Commit commit = new Commit("initial commit", new Date(0L), new HashMap<>(), null);
        String sha = sha1(commit.toString());
        saveCommit(commit, sha);

        //创建blobs目录,用于存储已提交文件
        if (!BLOBS_DIR.mkdirs()) {
            throw error("Failed to create blobs directory.");
        }
        try {
            if (!STAGE_FILE.createNewFile()) {
                throw error("Failed to create stage file.");
            }
        } catch (IOException e) {
            throw error("Failed to create stage file.");
        }
        Stage stage = new Stage(new HashMap<String, String>(), new HashSet<>());
        saveStage(stage);

        //创建refs文件，用于保存每个分支的head
        try {
            if (!REFS_FILE.createNewFile()) {
                throw error("Failed to create refs file.");
            }
        } catch (IOException e) {
            throw error("Failed to create refs file.");
        }
        TreeMap<String, String> branches = new TreeMap<>();
        branches.put(currentBranch, sha);
        saveRefs(branches);
        //创建refs文件，用于保存每个分支的head

    }

    static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        Stage stage = getStage();

        String oldBlobId = stage.getStashBlobId(fileName);
        if (oldBlobId != null) {
            File oldBlobFile = join(BLOBS_DIR, oldBlobId);
            if (!oldBlobFile.delete()) {
                throw error("Failed to delete stash file " + fileName);
            }
            stage.unstageFile(fileName);
        }

        byte[] contents = readContents(file);
        String newBlobId = sha1((Object) contents);

        Commit currentCommit = getCurrentCommit();
        Map<String, String> commitFiles = currentCommit.getFileToBlobMap();

        //检查文件是否被修改
        if (commitFiles.containsKey(fileName) && Objects.equals(commitFiles.get(fileName),
                newBlobId)) {
            //文件未被修改
            stage.unremoveFile(fileName);
            saveStage(stage);
            return;
        }

        //文件被修改,将该文件加入blobs目录
        File stashFile = join(BLOBS_DIR, newBlobId);
        try {
            if (!stashFile.createNewFile()) {
                byte[] fileContents = readContents(stashFile);
                if (!Arrays.equals(fileContents, contents)) {
                    throw error("Failed to create new stash file " + fileName);
                }
            }
        } catch (IOException e) {
            throw error("Failed to create new stash file " + fileName);
        }
        stage.stageFile(fileName, newBlobId);
        saveStage(stage);

//        Map<String,String> addedFiles = stage.getAddFiles();
//        Map<String,String> removeFiles = stage.getRemoveFiles();
//        System.out.println("addedFiles:"+addedFiles.toString());
//        System.out.println("removeFiles:"+removeFiles.toString());

        writeContents(stashFile, (Object) contents);
    }

    public static void commit(String message, String parent) {
        Stage stage = getStage();
        Map<String, String> addedFiles = stage.getAddFiles();
        Set<String> removeFiles = stage.getRemoveFiles();

        //确认是否有修改
        if (addedFiles.isEmpty() && removeFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
//        System.out.println("addedFiles:"+addedFiles.toString());
//        System.out.println("removeFiles:"+removeFiles.toString());

        //生成新的commit
        Commit newCommit = getNewCommit(message, addedFiles, removeFiles, parent);

        //保存commit
        String commitId = sha1(newCommit.toString());
        saveCommit(newCommit, commitId);

        //更新分支HEAD
        TreeMap<String, String> refs = getRefs();
        String currentBranch = getCurrentBranch();
        refs.put(currentBranch, commitId);
        saveRefs(refs);

        //清除stash文件
        stage.clear();
        saveStage(stage);
    }

    private static Commit getNewCommit(String message, Map<String, String> addedFiles,
                                       Set<String> removeFiles, String parent) {
        Commit currentCommit = getCurrentCommit();
        Date timestamp = new Date();
        Map<String, String> commitFiles = currentCommit.getFileToBlobMap();
        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            commitFiles.put(key, value);
        }

        for (String fileName : removeFiles) {
            commitFiles.remove(fileName);
        }
        String currentCommitId = getCurrentCommitId();
        List<String> list = new ArrayList<>();
        list.add(currentCommitId);
        if (parent != null) {
            list.add(parent);
        }
        return new Commit(message, timestamp, commitFiles, list);
    }

    public static void rm(String fileName) {
        Stage stage = getStage();
        String oldBlobId = stage.getStashBlobId(fileName);
        boolean stageIsChanged = false;
        if (oldBlobId != null) {
            File oldBlobFile = join(BLOBS_DIR, oldBlobId);
            if (!oldBlobFile.delete()) {
                throw error("Failed to delete stash file " + fileName);
            }
            stage.unstageFile(fileName);
            stageIsChanged = true;
        }
        Commit currentCommit = getCurrentCommit();
        Map<String, String> commitedFile = currentCommit.getFileToBlobMap();
        if (commitedFile.containsKey(fileName)) {
            File oldBlobFile = join(CWD, fileName);
            if (oldBlobFile.exists() && !oldBlobFile.delete()) {
                throw error("Failed to delete file " + fileName);
            }
            stage.removeFile(fileName);
            stageIsChanged = true;
        }
        if (stageIsChanged) {
//            Map<String,String> addedFiles = stage.getAddFiles();
//            Set<String> removeFiles = stage.getRemoveFiles();
//            System.out.println("addedFiles:"+addedFiles.toString());
//            System.out.println("removeFiles:"+removeFiles.toString());
            saveStage(stage);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    private static void printCommit(Commit commit, String commitId) {
        System.out.println("===");
        System.out.println("commit " + commitId);
        SimpleDateFormat sdf =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        String formatted = sdf.format(commit.getTimestamp());
        System.out.println("Date: " + formatted);
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void log() {
        String currentCommitId = getCurrentCommitId();
        while (currentCommitId != null) {
            Commit currentCommit = getCommit(currentCommitId);
            printCommit(currentCommit, currentCommitId);
            currentCommitId = currentCommit.getParent();
        }
    }

    public static void globalLog() {
        List<String> fileNames = plainFilenamesIn(COMMITS_DIR);

        if (fileNames != null) {
            for (String fileName : fileNames) {
                Commit commit = getCommit(fileName);
                printCommit(commit, fileName);
            }
        }
    }

    public static void find(String message) {
        List<String> fileNames = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;

        if (fileNames != null) {
            for (String fileName : fileNames) {
                Commit commit = getCommit(fileName);
                if (commit.getMessage().equals(message)) {
                    found = true;
                    System.out.println(fileName);
                }
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    private static void getFileStatus(Set<String> workFiles,
                                      TreeMap<String, String> modifiedFiles,
                                      TreeSet<String> untrackedFiles) {
        Stage stage = getStage();
        Map<String, String> addedFiles = stage.getAddFiles();
        Set<String> removeFiles = stage.getRemoveFiles();
        Commit newCommit = null;

        //确认是否有修改
        if (addedFiles.isEmpty() && removeFiles.isEmpty()) {
            newCommit = getCurrentCommit();
        } else {
            newCommit = getNewCommit("", addedFiles, removeFiles, null);
        }

        Set<String> union = new HashSet<>(workFiles);
        Set<String> commitFiles = newCommit.getFileToBlobMap().keySet();
        union.addAll(commitFiles);

        for (String fileName : union) {
            if (workFiles.contains(fileName) && !commitFiles.contains(fileName)) {
                untrackedFiles.add(fileName);
            } else if (!workFiles.contains(fileName) && commitFiles.contains(fileName)) {
                modifiedFiles.put(fileName, "deleted");
            } else {
                File workFile = join(CWD, fileName);
                String workFileContents = readContentsAsString(workFile);
                String hash = sha1(workFileContents);
                if (!hash.equals(newCommit.getFileToBlobMap().get(fileName))) {
                    modifiedFiles.put(fileName, "modified");
                }
            }
        }

    }

    public static void status() {
        //打印分支信息
        TreeMap<String, String> refs = getRefs();
        String currentBranch = getCurrentBranch();
        System.out.println("=== Branches ===");
        for (String branchName : refs.keySet()) {
            if (branchName.equals(currentBranch)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();


        //打印stage文件
        Stage stage = getStage();
        System.out.println("=== Staged Files ===");
        Map<String, String> addFiles = stage.getAddFiles();
        for (String fileName : addFiles.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Set<String> removedFiles = stage.getRemoveFiles();
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        //获取modified和untracked文件
        TreeMap<String, String> modifiedFiles = new TreeMap<>();
        TreeSet<String> untrackedFiles = new TreeSet<>();
        List<String> fileNames = plainFilenamesIn(CWD);
        TreeSet<String> workFileNames = new TreeSet<>();
        if (fileNames != null) {
            workFileNames = new TreeSet<>(fileNames);
        }

        getFileStatus(workFileNames, modifiedFiles, untrackedFiles);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (Map.Entry<String, String> entry : modifiedFiles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + " (" + value + ")");
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    public static void checkoutFile(String fileName, String commitId) {
        if (commitId == null) {
            commitId = getCurrentCommitId();
        }

        Commit commit = getCommit(commitId);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Map<String, String> commitFiles = commit.getFileToBlobMap();
        if (!commitFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String blobId = commitFiles.get(fileName);
        File commitFile = new File(BLOBS_DIR, blobId);
        File file = new File(CWD, fileName);
        String contents = readContentsAsString(commitFile);
        writeContents(file, contents);
    }

    public static void checkoutBranch(String branch) {
        TreeMap<String, String> refs = getRefs();
        String currentBranch = getCurrentBranch();
        if (!refs.containsKey(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String commitId = refs.get(branch);
        reset(branch, commitId);
        saveCurrentBranch(branch);
    }

    public static void branch(String branch) {
        TreeMap<String, String> refs = getRefs();
        if (refs.containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        String commitId = getCurrentCommitId();
        refs.put(branch, commitId);
        saveRefs(refs);
    }

    public static void rmBranch(String branch) {
        TreeMap<String, String> refs = getRefs();
        if (!refs.containsKey(branch)) {
            System.out.println("A branch with that name does not exists.");
            return;
        }
        String currentBranch = getCurrentBranch();
        if (branch.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        refs.remove(branch);
        saveRefs(refs);
    }

    public static void reset(String branch, String commitId) {
        Commit commit = getCommit(commitId);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        //获取modified和untracked文件
        TreeMap<String, String> modifiedFiles = new TreeMap<>();
        TreeSet<String> untrackedFiles = new TreeSet<>();
        List<String> fileNames = plainFilenamesIn(CWD);
        TreeSet<String> workFileNames = new TreeSet<>();
        if (fileNames != null) {
            workFileNames = new TreeSet<>(fileNames);
        }

        getFileStatus(workFileNames, modifiedFiles, untrackedFiles);
        if (!untrackedFiles.isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and "
                    + "commit it first.");
            return;
        }

        Stage stage = getStage();
        stage.clear();
        saveStage(stage);

        if (branch == null) {
            branch = getCurrentBranch();
        }
        TreeMap<String, String> refs = getRefs();
        refs.put(branch, commitId);
        saveRefs(refs);

        for (String fileName : workFileNames) {
            File workFile = new File(CWD, fileName);
            if (!workFile.delete()) {
                throw error("Failed to delete file " + fileName);
            }
        }

        Map<String, String> commitFiles = commit.getFileToBlobMap();
        for (Map.Entry<String, String> entry : commitFiles.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            File commitFile = new File(BLOBS_DIR, blobId);
            String contents = readContentsAsString(commitFile);
            File workFile = new File(CWD, fileName);
            try {
                if (!workFile.createNewFile()) {
                    throw error("Failed to create refs file.");
                }
            } catch (IOException e) {
                throw error("Failed to create refs file.");
            }
            writeContents(workFile, contents);
        }
    }

    private static HashSet<String> getAllParents(String commitId) {
        HashSet<String> parents = new HashSet<>();
        Commit commit = getCommit(commitId);
        if (commit == null) {
            throw error("commitId " + commitId + " does not exist.");
        }
        parents.add(commitId);
        List<String> allParents = commit.getAllParents();
        if (allParents != null) {
            for (String parentId : allParents) {
                HashSet<String> children = getAllParents(parentId);
                parents.addAll(children);
            }
        }
        return parents;
    }

    private static String getSplitPoint(String commitId1, String commitId2) {
        HashSet<String> parents = getAllParents(commitId1);
        if (parents.contains(commitId2)) {
            return commitId2;
        }

        Commit commit = getCommit(commitId2);
        if (commit == null) {
            throw error("commitId " + commitId2 + " does not exist.");
        }
        while (commit.getParent() != null) {
            commitId2 = commit.getParent();
            if (parents.contains(commitId2)) {
                return commitId2;
            }
            commit = getCommit(commitId2);
            if (commit == null) {
                throw error("commitId " + commitId2 + " does not exist.");
            }
        }
        throw error("Fail to get split point.");
    }

    private static void getFilestatus(String commitId1,
                                      String commitId2,
                                      TreeMap<String, String> modifiedFiles) {
        Commit commit1 = getCommit(commitId1);
        if (commit1 == null) {
            throw error("commitId " + commitId1 + " does not exist.");
        }
        Commit commit2 = getCommit(commitId2);
        if (commit2 == null) {
            throw error("commitId " + commitId2 + " does not exist.");
        }
        Map<String, String> fileMap1 = commit1.getFileToBlobMap();
        Map<String, String> fileMap2 = commit2.getFileToBlobMap();

        Set<String> unionKeys = new HashSet<>(fileMap1.keySet());
        unionKeys.addAll(fileMap2.keySet());
        for (String fileName : unionKeys) {
            boolean exists1 = fileMap1.containsKey(fileName);
            boolean exists2 = fileMap2.containsKey(fileName);
            if (!exists1 && !exists2) {
                throw error("file " + fileName + " does not exist.");
            } else if (exists1 && !exists2) {
                modifiedFiles.put(fileName, "deleted");
            } else if (!exists1 && exists2) {
                modifiedFiles.put(fileName, fileMap2.get(fileName));
            } else {
                String blobId1 = fileMap1.get(fileName);
                String blobId2 = fileMap2.get(fileName);
                if (!blobId1.equals(blobId2)) {
                    modifiedFiles.put(fileName, blobId2);
                }
            }
        }
    }

    private static boolean mergeAllFiles(Set<String> unionKeys, TreeMap<String,
                                                 String> currentBranchModifiedFiles,
                                         TreeMap<String, String> targetBranchModifiedFiles) {
        boolean conflict = false;
        for (String fileName : unionKeys) {
            boolean exists1 = currentBranchModifiedFiles.containsKey(fileName);
            boolean exists2 = targetBranchModifiedFiles.containsKey(fileName);
            if (!exists1 && !exists2) {
                throw new IllegalArgumentException("file " + fileName + " does not exist.");
            } else if (exists1 && !exists2) {
                continue;
            } else if (!exists1 && exists2) {
                String blobId = targetBranchModifiedFiles.get(fileName);
                File workFile = new File(CWD, fileName);
                if (Objects.equals(blobId, "deleted")) {
                    rm(fileName);
                } else {
                    File newFile = new File(BLOBS_DIR, blobId);
                    String contents = readContentsAsString(newFile);
                    if (!workFile.exists()) {
                        try {
                            if (!workFile.createNewFile()) {
                                throw error("Failed to create work file.");
                            }
                        } catch (IOException e) {
                            throw error("Failed to create work file.");
                        }
                    }
                    writeContents(workFile, contents);
                    add(fileName);
                }
            } else {
                String blobId1 = currentBranchModifiedFiles.get(fileName);
                String blobId2 = targetBranchModifiedFiles.get(fileName);
                if (blobId1.equals(blobId2)) {
                    continue;
                }

                String contents1 = "";
                if (!blobId1.equals("deleted")) {
                    File newFile = new File(BLOBS_DIR, blobId1);
                    contents1 = readContentsAsString(newFile);
                }
                String contents2 = "";
                if (!blobId2.equals("deleted")) {
                    File newFile = new File(BLOBS_DIR, blobId2);
                    contents2 = readContentsAsString(newFile);
                }
                String newContents = "<<<<<<< HEAD\n"
                        + contents1
                        + "=======\n"
                        + contents2
                        + ">>>>>>>\n";

                File workFile = new File(CWD, fileName);
                if (!workFile.exists()) {
                    try {
                        if (!workFile.createNewFile()) {
                            throw error("Failed to create work file.");
                        }
                    } catch (IOException e) {
                        throw error("Failed to create work file.");
                    }
                }
                writeContents(workFile, newContents);
                add(fileName);
                conflict = true;
            }
        }
        return conflict;
    }

    public static void merge(String targetBranch) {
        //确认是否有未提交的修改
        Stage stage = getStage();
        Map<String, String> addedFiles = stage.getAddFiles();
        Set<String> removeFiles = stage.getRemoveFiles();
        if (!addedFiles.isEmpty() || !removeFiles.isEmpty()) {
            System.out.println("You have uncommitted changes");
            return;
        }

        //确认分支是否存在
        TreeMap<String, String> refs = getRefs();
        if (!refs.containsKey(targetBranch)) {
            System.out.println("A branch with that name does not exists.");
            return;
        }

        //确认是否为当前分支
        String currentBranch = getCurrentBranch();
        if (currentBranch.equals(targetBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        String currentBranchCommitId = getCurrentCommitId();
        String targetBranchCommitId = refs.get(targetBranch);
//        System.out.println("Target branch commit id: " + targetBranchCommitId);
//        System.out.println("current branch commit id: " + currentBranchCommitId);
        String splitPoint = getSplitPoint(currentBranchCommitId, targetBranchCommitId);
//        System.out.println("Split point: " + splitPoint);
        if (splitPoint.equals(targetBranchCommitId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        if (splitPoint.equals(currentBranchCommitId)) {
            reset(null, targetBranchCommitId);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        TreeMap<String, String> currentBranchModifiedFiles = new TreeMap<>();
        getFilestatus(splitPoint, currentBranchCommitId, currentBranchModifiedFiles);
        TreeMap<String, String> targetBranchModifiedFiles = new TreeMap<>();
        getFilestatus(splitPoint, targetBranchCommitId, targetBranchModifiedFiles);
        Set<String> unionKeys = new HashSet<>(currentBranchModifiedFiles.keySet());
        unionKeys.addAll(targetBranchModifiedFiles.keySet());

        //检查untrackdeFile是否被覆盖或者删除
        TreeMap<String, String> modifiedFiles = new TreeMap<>();
        TreeSet<String> untrackedFiles = new TreeSet<>();
        List<String> fileNames = plainFilenamesIn(CWD);
        TreeSet<String> workFileNames = new TreeSet<>();
        if (fileNames != null) {
            workFileNames = new TreeSet<>(fileNames);
        }

        getFileStatus(workFileNames, modifiedFiles, untrackedFiles);
        for (String fileName : untrackedFiles) {
            if (unionKeys.contains(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
                return;
            }
        }

//        System.out.println("unionKeys: " + unionKeys);
//        System.out.println("currentBranchModifiedFiles: " + currentBranchModifiedFiles);
//        System.out.println("targetBranchModifiedFiles: " + targetBranchModifiedFiles);
        boolean conflict = mergeAllFiles(unionKeys,
                currentBranchModifiedFiles,
                targetBranchModifiedFiles);

        //检查所有文件
        commit("Merged " + targetBranch + " into " + currentBranch + ".",
                targetBranchCommitId);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
}

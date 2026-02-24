package gitlet;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class Stage implements Serializable {
    private final Map<String, String> addFiles;    //file name -> hash id
    private final Set<String> removeFiles; //file name -> hash id


    public Stage(Map<String, String> addFiles, Set<String> removeFiles) {
        this.addFiles = addFiles;
        this.removeFiles = removeFiles;
    }

    public void stageFile(String fileName, String blobId) {
        addFiles.put(fileName, blobId);
        removeFiles.remove(fileName);
    }

    public void unstageFile(String fileName) {
        addFiles.remove(fileName);
    }

    public void removeFile(String fileName) {
        removeFiles.add(fileName);
        addFiles.remove(fileName);
    }

    public void unremoveFile(String fileName) {
        removeFiles.remove(fileName);
    }

    public String getStashBlobId(String fileName) {
        return addFiles.get(fileName);
    }

    public Map<String, String> getAddFiles() {
        return addFiles;
    }

    public Set<String> getRemoveFiles() {
        return removeFiles;
    }

    public void clear() {
        addFiles.clear();
        removeFiles.clear();
    }

}

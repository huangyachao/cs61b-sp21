package gitlet;

import java.io.Serializable;
import java.util.Map;

public class Stage implements Serializable {
    private Map<String,String> addFiles;    //file name -> hash id
    private Map<String,String> removeFiles; //file name -> hash id


    public Stage(Map<String,String> addFiles,Map<String,String> removeFiles){
        this.addFiles = addFiles;
        this.removeFiles = removeFiles;
    }

    public void stageFile(String fileName,String blobId){
        addFiles.put(fileName,blobId);
        removeFiles.remove(fileName);
    }

    public void unstageFile(String fileName){
        addFiles.remove(fileName);
    }

    public void removeFile(String fileName,String blobId){
        removeFiles.put(fileName,blobId);
        addFiles.remove(fileName);
    }

    public void unremoveFile(String fileName){
        removeFiles.remove(fileName);
    }

    public String getStashBlobId(String fileName){
        return addFiles.get(fileName);
    }

    public String getRemoveBlobId(String fileName){
        return removeFiles.get(fileName);
    }

    public  Map<String,String> getAddFiles() {
        return addFiles;
    }

    public  Map<String,String> getRemoveFiles() {
        return removeFiles;
    }

    public void clear(){
        addFiles.clear();
        removeFiles.clear();
    }

}

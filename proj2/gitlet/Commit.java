package gitlet;


import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Represents a gitlet commit object.
 * does at a high level.
 *
 * @author haya
 */
public class Commit implements Serializable {
    /**
     * The message of this Commit.
     */
    private String message;
    private Date timestamp;
    private Map<String, String> fileToBlobMap;
    private String parent;


    public Commit(String message, Date timestamp, Map<String, String> fileToBlobMap,
                  String parent) {
        this.message = message;
        this.timestamp = timestamp;
        this.fileToBlobMap = fileToBlobMap;
        this.parent = parent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getFileToBlobMap() {
        return fileToBlobMap;
    }

    public void setFileToBlobMap(Map<String, String> fileToBlobMap) {
        this.fileToBlobMap = fileToBlobMap;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String toString() {
        return message + timestamp.toString() + fileToBlobMap.toString() + parent;
    }
}

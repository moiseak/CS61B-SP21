package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
/** Represents a gitlet commit object.
 *  Does at a high level.
 *
 *  @author Moiads
 */
public class Commit implements Serializable {
    /*
      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    private final String commitDate;
    private final String hashCodeCommit;
    private final String parent;
    private String parent2 = null;

    public String getMergeMessage() {
        return mergeMessage;
    }

    public String getParent2() {
        return parent2;
    }

    private final String mergeMessage;
    //file and file hash value
    private HashMap<String, String> fileHashcode = new HashMap<>();

    public Commit() {
        this.message  = "initial commit";
        this.mergeMessage = "";
        Date d = new Date(0);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        this.commitDate = sdf.format(d);
        this.parent = "";
        this.hashCodeCommit = Utils.sha1(this.message, this.commitDate);
    }

    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.mergeMessage = "";
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        this.commitDate = sdf.format(d);
        hashCodeCommit = Utils.sha1(this.message, this.commitDate, this.parent);
    }

    public Commit(String message, String parent, String mergeMessage, String parent2) {
        this.message = message;
        this.parent = parent;
        this.mergeMessage = mergeMessage;
        this.parent2 = parent2;
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        this.commitDate = sdf.format(d);
        hashCodeCommit = Utils.sha1(this.message, this.commitDate, this.parent, this.mergeMessage);
    }

    public boolean isTracked(String fileName, String fileHash) {
        return fileHashcode.containsKey(fileName) && fileHashcode.get(fileName).equals(fileHash);
    }

    public HashMap<String, String> getFileHashcode() {
        return this.fileHashcode;
    }

    public void setFileHashcode(HashMap<String, String> fileHashcode) {
        this.fileHashcode = fileHashcode;
    }

    public void addFileHashcode(String filename, String hashcode) {
        this.fileHashcode.put(filename, hashcode);
    }

    public String getHashcodeCommit() {
        return this.hashCodeCommit;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDate() {
        return this.commitDate;
    }

    public String getParent() {
        if (Objects.equals(this.parent, "")) {
            return null;
        }
        return this.parent;
    }

    public void commit() throws IOException {
        File commitFile = Utils.join(Repository.COMMIT, this.getHashcodeCommit());
        commitFile.createNewFile();
        Utils.writeObject(commitFile, this);
    }
}

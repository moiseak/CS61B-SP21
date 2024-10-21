package gitlet;

// TODO: any imports you need here
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /*
      TODO: add instance variables here.
      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String commitDate;
    private String hashCodeCommit;
    private String parent;
    private String branch;
    //file and file hash value
    private HashMap<String, String> fileHashcode = new HashMap<>();

    public Commit() {
        this.message  = "initial commit";
        Date d = new Date(0);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        String formattedDate = sdf.format(d);
        this.commitDate = formattedDate;
        this.parent = "";
        this.branch = "master";
        this.hashCodeCommit = Utils.sha1(this.message, this.commitDate);
    }

    public Commit(String message, String parent, String branch) {
        this.message = message;
        this.parent = parent;
        this.branch = branch;
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        String formattedDate = sdf.format(d);
        this.commitDate = formattedDate;
        hashCodeCommit = Utils.sha1(this.message, this.commitDate, this.parent);
    }

    public HashMap<String, String> getFileHashcode() {
        return this.fileHashcode;
    }

    public void setFileHashcode(HashMap<String, String> fileHashcode) {
        this.fileHashcode = fileHashcode;
    }

    public void addFileHashcode(String filename, String hashcode) {
        this.fileHashcode.put(filename,hashcode);
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

    public String getCommitDate() {
        return this.commitDate;
    }

    public String getParent() {
        if (Objects.equals(this.parent, "")) {
            return null;
        }
        return this.parent;
    }

    public String getBranch() {
        return this.branch;
    }

    public void commit() throws IOException {
        File commitFile = Utils.join(Repository.COMMIT, this.getHashcodeCommit());
        commitFile.createNewFile();
        Utils.writeObject(commitFile, this);
    }
}

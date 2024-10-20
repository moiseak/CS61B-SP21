package gitlet;

// TODO: any imports you need here
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.Objects;

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
    private static HashMap<String, File> blobs = new HashMap<>();
    private String hashCodeCommit;
    private String parent;
    private String branch;
    private HashMap<String, String> fileHashcode = new HashMap<>();
    /* TODO: fill in the rest of this class. */

    public Commit() {
        this.message  = "initial commit";
        this.commitDate = new Date(0).toString();
        this.parent = "";
        this.branch = "master";
        hashCodeCommit = Utils.sha1(this.message, this.commitDate, this.parent, this.branch);
    }

    public Commit(String message, String parent, String branch) {
        this.message = message;
        this.parent = parent;
        this.branch = branch;
        this.commitDate = new Date().toString();
        hashCodeCommit = Utils.sha1(this.message, this.commitDate, this.parent, this.branch);
    }

    public HashMap<String, String> getFileHashcode() {
        return this.fileHashcode;
    }

    public void setFileHashcode(HashMap<String, String> fileHashcode) {
        this.fileHashcode = fileHashcode;
    }

    public void addFileHashcode(String hashcode, String filename) {
        this.fileHashcode.put(filename,hashcode);
    }

    public static HashMap<String, File> getBlobs() {
        return blobs;
    }

    public String getHashcodeCommit() {
        return this.hashCodeCommit;
    }

    public static void addBlob(String fileHash, File file) {
        blobs.put(fileHash, file);
    }

    public String getMessage() {
        return this.message;
    }

    public String getCommitDate() {
        return this.commitDate;
    }

    public String getParent() {
        return this.parent;
    }

    public String getBranch() {
        return this.branch;
    }

    public void commit() throws IOException {
        File commitFile = Utils.join(Repository.COMMIT, this.getHashcodeCommit());
        Utils.writeObject(commitFile, this);
        commitFile.createNewFile();
    }
}

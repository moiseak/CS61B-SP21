package gitlet;

// TODO: any imports you need here
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class

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
    private final String message;
    private final Date commitDate;
    private static ArrayList<File> blobs;
    private final String hashCodeCommit;
    private final Commit parent;
    private static ArrayList<String> hashCodeBlobs;
    private String branch;
    /* TODO: fill in the rest of this class. */

    public Commit(String message, Commit parent) {
        this.message = message;
        this.parent = parent;
        hashCodeCommit = Utils.sha1(this.message);
        if (this.parent == null) {
            this.commitDate = new Date(0);
        } else {
            this.commitDate = new Date();
        }
    }

    public static void addBlob(File file) {
        blobs.add(file);
    }

    public static void addHashBlob(String hash) {
        hashCodeBlobs.add(hash);
    }

    public static ArrayList<String> getBlobHash() {
        return hashCodeBlobs;
    }

    public String getMessage() {
        return this.message;
    }

    public Date getCommitDate() {
        return this.commitDate;
    }

    public Commit getParent() {
        return this.parent;
    }

    public String getBranch() {
        return this.branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void commit() throws IOException {
        File commitFile = Utils.join(Repository.GITLET_DIR, hashCodeCommit);
        commitFile.createNewFile();
        Utils.writeObject(commitFile, this);
        Utils.restrictedDelete()
    }
}

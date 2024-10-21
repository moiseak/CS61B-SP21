package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Moiads
 */
public class Repository implements Serializable {
    /*
      TODO: add instance variables here.
      List all instance variables of the Repository class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /* The staging */
    public static final File STAGING_AREA = join(GITLET_DIR, "staging");
    //Each constant that needs to be saved
    public static final File COMMIT = Utils.join(Repository.GITLET_DIR, "commit");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File MASTER_FILE = join(GITLET_DIR, "master");
    public static final File COMMITS_FILE = join(GITLET_DIR, "commits");
    public static final File BLOBS_FILE = join(GITLET_DIR, "blobs");

    //Commit hash value to commit mapping
    public static HashMap<String, Commit> commits = new HashMap<>();
    //branch
    public static Commit HEAD;
    public static Commit master;
    //string is file hash value, byte[] is file content
    public static HashMap<String, byte[]> blobs = new HashMap<>();


    public static void init() throws IOException {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }
        else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        if (!STAGING_AREA.exists()) {
            STAGING_AREA.mkdir();
        }
        if (!COMMIT.exists()) {
            COMMIT.mkdir();
        }
        HEAD_FILE.createNewFile();
        MASTER_FILE.createNewFile();
        COMMITS_FILE.createNewFile();
        BLOBS_FILE.createNewFile();

        //first commit
        Commit firstCommit = new Commit();
        HEAD = firstCommit;
        master = firstCommit;
        firstCommit.commit();
        commits.put(firstCommit.getHashcodeCommit(), firstCommit);

        //save
        writeObject(BLOBS_FILE, blobs);
        writeObject(HEAD_FILE, HEAD);
        writeObject(MASTER_FILE, master);
        writeObject(COMMITS_FILE, commits);
    }

    public static void add(String file) throws IOException {
        //find local file
        File add = join(CWD, file);
        if (!add.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        //Get file hash value
        String addHash = sha1((Object) readContents(add));
        blobs =  readObject(BLOBS_FILE, HashMap.class);
        //Traverse blobs without adding new blobs if the file to be submitted is the same as the file in the blobs
        if (blobs != null){
            for (String key : blobs.keySet()) {
                //If the same hash value is found, it will not be added again.
                if (addHash.equals(key)) {
                    //Equivalent to copying a local file to the stage
                    File addStage = join(STAGING_AREA, file);
                    writeContents(addStage, (Object) readContents(add));
                    addStage.createNewFile();
                    return;
                }
            }
        }
        //There is no identical one, so create a new blob and create a file in the temporary storage area.
        //!!!This is the only place to change blobs!!!
        if (blobs != null) {
            blobs.put(addHash, readContents(add));
        }
        //save
        writeObject(BLOBS_FILE, blobs);
        //copy to stage
        File addStage = join(STAGING_AREA, file);
        writeContents(addStage, (Object) readContents(add));
        addStage.createNewFile();
    }

    public static void commit(String message) throws IOException {
        //get parent commit
        HEAD = readObject(HEAD_FILE, Commit.class);
        master = readObject(MASTER_FILE, Commit.class);
        commits = readObject(COMMITS_FILE, HashMap.class);
        String parentHash = HEAD.getHashcodeCommit();
        String branchName = HEAD.getBranch();
        Commit commit = new Commit(message, parentHash, branchName);
        //Defaults to the same file as the parent commit
        commit.setFileHashcode(HEAD.getFileHashcode());
        //change file-hashcode
        //get all filename in stage
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        //stage not null
        if (hashList != null) {
            for (String s : hashList) {
                //whether you find
                boolean flag = false;
                //Get the files in the stage based on the file name
                File file = join(STAGING_AREA, s);
                //Get the hash of the corresponding file
                String fileHash = sha1((Object) readContents(file));
                //Determine whether the file hash corresponding
                if (commit.getFileHashcode().isEmpty()) {
                    flag = true;
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                }
                else {
                    //key is filename
                    for (String key : commit.getFileHashcode().keySet()) {
                        //find equal filename
                        if (s.equals(key)) {
                            //hash value not equal
                            if (!fileHash.equals(commit.getFileHashcode().get(key))) {
                                commit.addFileHashcode(key, fileHash);
                            }
                            flag = true;
                            file.delete();
                        }
                    }
                }
                //not find
                if (!flag) {
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                }
            }
        } else {
            System.out.println("No changes added to the commit.");
            return;
        }
        //save
        commit.commit();
        HEAD = commit;
        master = commit;
        commits.put(commit.getHashcodeCommit(), commit);
        writeObject(HEAD_FILE, HEAD);
        writeObject(MASTER_FILE, master);
        writeObject(COMMITS_FILE, commits);
    }

    public static void log() {
        HEAD = readObject(HEAD_FILE, Commit.class);
        master = readObject(MASTER_FILE, Commit.class);
        commits = readObject(COMMITS_FILE, HashMap.class);
        while (HEAD != null) {
            printCommit();
            HEAD = (Commit) commits.get(HEAD.getParent());
        }
    }

    //helper print
    private static void printCommit() {
        System.out.println("===");
        System.out.println("commit " + HEAD.getHashcodeCommit());
        System.out.println("Date: " + HEAD.getDate());
        System.out.println(HEAD.getMessage());
        System.out.println();
    }

    public static void checkout(String file) throws IOException {
        HEAD = readObject(HEAD_FILE, Commit.class);
        blobs = readObject(BLOBS_FILE, HashMap.class);
        File checkoutFile = join(CWD, file);
        //Determine whether there are files to be checked out
        if (!HEAD.getFileHashcode().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        //Get the file and write it
        String checkHash = HEAD.getFileHashcode().get(file);
        byte[] checkFile = blobs.get(checkHash);
        writeContents(checkoutFile, (Object) checkFile);
        checkoutFile.createNewFile();
    }

    public static void checkoutCommit(String commitId, String file) throws IOException {
        commits = readObject(COMMITS_FILE, HashMap.class);
        blobs = readObject(BLOBS_FILE, HashMap.class);
        //Whether to include this commit
        if (!commits.containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit checkCommit = commits.get(commitId);

        //whether this commit have the file
        if (!checkCommit.getFileHashcode().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String fileHash = checkCommit.getFileHashcode().get(file);
        //get and write
        byte[] checkByte = blobs.get(fileHash);
        File checkoutFile = join(CWD, file);
        writeContents(checkoutFile, (Object) checkByte);
        checkoutFile.createNewFile();
    }
}



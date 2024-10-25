package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  Does at a high level.
 *
 *  @author Moiads
 */
public class Repository implements Serializable {
    /*
      List all instance variables of the Repository class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided two examples for you.
     */

    /* The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /* The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /* The staging */
    public static final File STAGING_AREA = join(GITLET_DIR, "staging");
    public static final File RM_AREA = join(GITLET_DIR, "rm");
    //Each constant that needs to be saved
    //all commits
    public static final File COMMIT = Utils.join(Repository.GITLET_DIR, "commit");
    //HEAD commit
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    //hashmap about commitId to commit
    public static final File COMMITS_FILE = join(GITLET_DIR, "commits");
    //hashmap about filename to file a byte array
    public static final File BLOBS_FILE = join(GITLET_DIR, "blobs");
    //hashmap about branch name to branch
    public static final File BRANCHES_FILE = join(GITLET_DIR, "branches");
    //current branch
    public static final File BRANCH_FILE = join(GITLET_DIR, "branch");

    //Commit hash value to commit mapping
    public static HashMap<String, Commit> commits = new HashMap<>();
    //current commit
    public static Commit HEAD;
    //current branch
    public static class Branch implements Serializable{
        private final String name;
        private Commit commit;
        public Branch(String name, Commit commit) {
            this.name = name;
            this.commit = commit;
        }

        public Commit getCommit() {
            return commit;
        }
    }
    public static Branch currentBranch;
    //branches
    public static HashMap<String, Branch> branches = new HashMap<>();
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
        if (!RM_AREA.exists()) {
            RM_AREA.mkdir();
        }
        HEAD_FILE.createNewFile();
        COMMITS_FILE.createNewFile();
        BRANCH_FILE.createNewFile();
        BRANCHES_FILE.createNewFile();
        BLOBS_FILE.createNewFile();

        //first commit
        Commit firstCommit = new Commit();
        HEAD = firstCommit;
        firstCommit.commit();
        commits.put(firstCommit.getHashcodeCommit(), firstCommit);
        //default branch name is master
        Branch master = new Branch("master", firstCommit);
        currentBranch = master;
        branches.put("master", master);

        writeObject(HEAD_FILE, HEAD);
        writeObject(COMMITS_FILE, commits);
        //save blob
        writeObject(BLOBS_FILE, blobs);
        //save current branch
        writeObject(BRANCH_FILE, currentBranch);
        //save all branches
        writeObject(BRANCHES_FILE, branches);

    }

    @SuppressWarnings("unchecked")
    public static void add(String file) throws IOException {
        //find a local file
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

    @SuppressWarnings("unchecked")
    public static void commit(String ... message) throws IOException {
        //get parent commit
        HEAD = readObject(HEAD_FILE, Commit.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        commits = readObject(COMMITS_FILE, HashMap.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        String parentHash = HEAD.getHashcodeCommit();
        Commit commit = new Commit();
        if (message.length == 1) {
            commit = new Commit(message[0], parentHash);
        }
        if (message.length == 2) {
            commit = new Commit(message[0], parentHash, message[1]);
        }
        //Defaults to the same file as the parent commit
        commit.setFileHashcode(HEAD.getFileHashcode());
        //change file-hashcode
        //get all filenames in stage
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        List<String> hashListRm = plainFilenamesIn(RM_AREA);
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
                //not find and not null
                if (!flag) {
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                }
            }
        } else {
            System.out.println("No changes added to the commit.");
        }
        if (hashListRm != null) {
            for (String r : hashListRm) {
                commit.getFileHashcode().remove(r);
                File rmF = join(RM_AREA, r);
                rmF.delete();
            }
        }

        //save
        commit.commit();
        HEAD = commit;
        currentBranch.commit = commit;
        //update
        branches.put(currentBranch.name, currentBranch);
        commits.put(commit.getHashcodeCommit(), commit);
        writeObject(HEAD_FILE, HEAD);
        writeObject(BRANCH_FILE, currentBranch);
        writeObject(COMMITS_FILE, commits);
        writeObject(BRANCHES_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public static void log() {
        HEAD = readObject(HEAD_FILE, Commit.class);
        commits = readObject(COMMITS_FILE, HashMap.class);
        while (HEAD != null) {
            printCommit();
            HEAD = (Commit) commits.get(HEAD.getParent());
        }
    }

    //helper print to log
    private static void printCommit() {
        if (Objects.equals(HEAD.getMergeMessage(), "")) {
            System.out.println("===");
            System.out.println("commit " + HEAD.getHashcodeCommit());
            System.out.println("Date: " + HEAD.getDate());
            System.out.println(HEAD.getMessage());
            System.out.println();
        } else {
            System.out.println("===");
            System.out.println("commit " + HEAD.getHashcodeCommit());
            System.out.println(HEAD.getMergeMessage());
            System.out.println("Date: " + HEAD.getDate());
            System.out.println(HEAD.getMessage());
            System.out.println();
        }

    }

    //help print to log-global
    private static void printCommit(Commit commit) {
        if ("".equals(commit.getMergeMessage())) {
            System.out.println("===");
            System.out.println("commit " + commit.getHashcodeCommit());
            System.out.println("Date: " + commit.getDate());
            System.out.println(commit.getMessage());
            System.out.println();
        } else {
            System.out.println("===");
            System.out.println("commit " + commit.getHashcodeCommit());
            System.out.println(commit.getMergeMessage());
            System.out.println("Date: " + commit.getDate());
            System.out.println(commit.getMessage());
            System.out.println();
        }

    }

    //checkout HEAD file to CWD
    @SuppressWarnings("unchecked")
    public static void checkout(String file) throws IOException {
        //first, save HEAD. if you cannot understand, please delete it and then test checkBranch:)
        writeObject(HEAD_FILE, HEAD);
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

    //checkout commit corresponds commitId 's file to CWD
    @SuppressWarnings("unchecked")
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
        //get file content and write in CWD
        byte[] checkByte = blobs.get(fileHash);
        File checkoutFile = join(CWD, file);
        writeContents(checkoutFile, (Object) checkByte);
        checkoutFile.createNewFile();
    }

    @SuppressWarnings("unchecked")
    public static void checkBranch(String branch) throws IOException {
        HEAD = readObject(HEAD_FILE, Commit.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        currentBranch = branches.get(branch);
        HEAD = branches.get(branch).commit;
        List<String> cwd = plainFilenamesIn(CWD);
        if (cwd != null) {
            for (String s : cwd) {
                if (!HEAD.getFileHashcode().containsKey(s)) {
                    restrictedDelete(s);
                }
            }
        }
        for (String key : HEAD.getFileHashcode().keySet()) {
            checkout(key);
        }
        branches.put(currentBranch.name, currentBranch);
        writeObject(HEAD_FILE, HEAD);
        writeObject(BRANCH_FILE, currentBranch);
        writeObject(BRANCHES_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public static void rm(String file) throws IOException {
        HEAD = readObject(HEAD_FILE, Commit.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        //if all do not have this file
        if (hashList != null && !hashList.contains(file) && !HEAD.getFileHashcode().containsKey(file)) {
            System.out.println("No reason to remove the file.");
        }
        //remove from stage
        if (hashList != null) {
            if (hashList.contains(file)) {
                File file1 = join(STAGING_AREA, file);
                File rm = join(RM_AREA, file);
                writeContents(rm, (Object) readContents(file1));
                rm.createNewFile();
                file1.delete();
            }
        }
        //remove from HEAD
        HEAD.getFileHashcode().remove(file);
        currentBranch.commit.getFileHashcode().remove(file);
        branches.put(currentBranch.name, currentBranch);
        writeObject(BRANCH_FILE, currentBranch);
        writeObject(BRANCHES_FILE, branches);
        writeObject(HEAD_FILE, HEAD);
        restrictedDelete(file);
    }

    //print all commit
    @SuppressWarnings("unchecked")
    public static void logGlobal() {
        List<String> commitList = plainFilenamesIn(COMMIT);
        commits = readObject(COMMITS_FILE, HashMap.class);
        if (commitList != null) {
            for (String s : commitList) {
                Commit commit = commits.get(s);
                printCommit(commit);
            }
        }
    }

    //print commitId which message is an arg message
    @SuppressWarnings("unchecked")
    public static void find(String message) {
        commits = readObject(COMMITS_FILE, HashMap.class);
        boolean found = false;
        for (Commit commit : commits.values()) {
            if (commit.getMessage().equals(message)) {
                System.out.println(commit.getHashcodeCommit());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    @SuppressWarnings("unchecked")
    public static void status() {
        branches = readObject(BRANCHES_FILE, HashMap.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        HEAD = readObject(HEAD_FILE, Commit.class);
        System.out.println("=== Branches ===");
        for (String name : branches.keySet()) {
            if (Objects.equals(name, currentBranch.name)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        if (hashList != null) {
            for (String s : hashList) {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String file : currentBranch.commit.getFileHashcode().keySet()) {
            if (!HEAD.getFileHashcode().containsKey(file)) {
                System.out.println(file);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    @SuppressWarnings("unchecked")
    public static void branch(String branch) {
        HEAD = readObject(HEAD_FILE, Commit.class);
        Branch newBranch = new Branch(branch, HEAD);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        if (branches.containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        branches.put(branch, newBranch);
        writeObject(BRANCHES_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public static void rmBranch(String branch) {
        branches = readObject(BRANCHES_FILE, HashMap.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        if (branch.equals(currentBranch.name)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        if (branches.remove(branch) == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        writeObject(BRANCHES_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public static void reset(String commitId) throws IOException {
        commits = readObject(COMMITS_FILE, HashMap.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        HEAD = readObject(HEAD_FILE, Commit.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        if (!commits.containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit nowCommit = commits.get(commitId);
        for (String fileName : nowCommit.getFileHashcode().keySet()) {
            checkoutCommit(commitId, fileName);
        }
        List<String> file = plainFilenamesIn(STAGING_AREA);
        if (file != null) {
            for (String s : file) {
                if (!nowCommit.getFileHashcode().containsKey(s)) {
                    File f = join(STAGING_AREA, s);
                    f.delete();
                }
            }
        }
        currentBranch.commit = nowCommit;
        HEAD = nowCommit;
        branches.put(currentBranch.name, currentBranch);
        writeObject(HEAD_FILE, HEAD);
        writeObject(BRANCH_FILE, currentBranch);
        writeObject(BRANCHES_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public static void merge(String branch) throws IOException {
        branches = readObject(BRANCHES_FILE, HashMap.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        HEAD = readObject(HEAD_FILE, Commit.class);
        commits = readObject(COMMITS_FILE, HashMap.class);
        blobs = readObject(BLOBS_FILE, HashMap.class);
        Commit splitCommit = currentBranch.commit;
        Branch giveBranch = branches.get(branch);
        //give branch must exist
        if (giveBranch == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        //the stage is not empty
        List<String> file = plainFilenamesIn(STAGING_AREA);
        if (file != null && !file.isEmpty()) {
            System.out.println("You have uncommitted changes.");
        }
        //merge itself
        if (currentBranch.name.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        //CWD
        List<String> neFile = plainFilenamesIn(CWD);
        if (neFile != null) {
            for (String fileName : neFile) {
                if (giveBranch.commit.getFileHashcode().containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return;
                }
            }
        }

        // find splitCommit
        List<String> id1 = new ArrayList<>();
        List<String> id2 = new ArrayList<>();
        Commit parent1 = giveBranch.commit;
        Commit parent2 = currentBranch.commit;
        while (parent1 != null) {
            id1.add(parent1.getParent());
            parent1 = commits.get(parent1.getParent());
        }
        while (parent2 != null) {
            id2.add(parent2.getParent());
            parent2 = commits.get(parent2.getParent());
        }
        for (String s : id1) {
            if (id2.contains(s)) {
                splitCommit = commits.get(s);
                break;
            }
        }
        if (splitCommit.getHashcodeCommit().equals(currentBranch.commit.getHashcodeCommit())) {
            //ToDo: we have not add
            checkBranch(branch);
            System.out.println("Current branch fast-forwarded.");
            return;
        } else if (splitCommit.getHashcodeCommit().equals(giveBranch.commit.getHashcodeCommit())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        //file exists in split commit
        for (Map.Entry<String, String> entry: splitCommit.getFileHashcode().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String gValue = giveBranch.commit.getFileHashcode().get(key);
            String cValue = currentBranch.commit.getFileHashcode().get(key);
            //ToDo:two branches delete file commonly
            //no delete
            if (cValue != null && gValue != null) {
                //give branch modifies, current branch does not modify
                if (!value.equals(gValue) && value.equals(cValue)) {
                    checkoutCommit(currentBranch.commit.getHashcodeCommit(), key);
                    add(key);
                    continue;
                }
                //curr branch modifies, give branch does not modify -- not need code

                //two branches modify the file commonly -- not need code

                //ToDo:two branches modify the file but not common
                if (!value.equals(gValue) && !cValue.equals(gValue)) {

                }

            }
            //ToDo:all delete
            else if (cValue == null && gValue == null) {

            }
            //in curr does not modify, in give be deleted
            else if (gValue == null && cValue.equals(value)) {
                rm(key);
            }
            //in give does not modify, in curr be deleted -- not need code

            //ToDo:one modify, one delete
            else if (gValue != null && !gValue.equals(value) && cValue == null) {

            }
            else if (cValue != null && gValue == null && !cValue.equals(value)) {

            }

            //ToDO:we have not cope file not exist
            //if give not equal split
//            if (!value.equals()) {
//                //and split equal current
//                if (splitCommit.getFileHashcode().get(entry.getKey()).equals(currentBranch.commit.getFileHashcode().get(entry.getKey()))) {
//                    checkoutCommit(giveBranch.commit.getHashcodeCommit(), entry.getKey());
//                    add(entry.getKey());
//                } else {
//                    //conflicted file
//                    byte[] currFile = blobs.get(currentBranch.commit.getFileHashcode().get(entry.getKey()));
//                    byte[] giveFile = blobs.get(giveBranch.commit.getFileHashcode().get(entry.getKey()));
//                    File newFile = join(STAGING_AREA, "new");
//                    writeContents(newFile, "<<<<<<< HEAD");
//                    writeContents(newFile, (Object) currFile);
//                    writeContents(newFile, "=======");
//                    writeContents(newFile, (Object) giveFile);
//                    writeContents(newFile, ">>>>>>>");
//
//                    String fHash = sha1(newFile);
//                    blobs.put(fHash, readContents(newFile));
//                    currentBranch.commit.getFileHashcode().put(entry.getKey(), fHash);
//                    branches.put(currentBranch.name, currentBranch);
//                    writeObject(BRANCH_FILE, currentBranch);
//                    writeObject(BLOBS_FILE, blobs);
//                    writeObject(BRANCHES_FILE, branches);
//                    giveBranch.commit.getFileHashcode().put(entry.getKey(), fHash);
//                    System.out.println("Encountered a merge conflict.");
//                }
//            }
        }
        //ToDo:file not exist in split commit


        String mergeMessage = "Merge: " + currentBranch.commit.getHashcodeCommit().substring(0, 7) + " " +  giveBranch.commit.getHashcodeCommit().substring(0, 7);
        String message = "Merged " + branch + " into " + currentBranch.name + ".";
        Repository.commit(message, mergeMessage);
    }
}



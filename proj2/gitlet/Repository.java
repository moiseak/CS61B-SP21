package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    //File
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

    //Fields
    //Commit hash value to commit mapping
    private static HashMap<String, Commit> commits = new HashMap<>();
    //current commit
    private static Commit HEAD;
    //current branch
    private static class Branch implements Serializable {
        private final String name;
        private Commit commit;
        Branch(String name, Commit commit) {
            this.name = name;
            this.commit = commit;
        }
    }
    private static Branch currentBranch;
    //branches
    private static HashMap<String, Branch> branches = new HashMap<>();
    //string is file hash value, byte[] is file content
    private static HashMap<String, byte[]> blobs = new HashMap<>();

    //Methods
    @SuppressWarnings("unchecked")
    private static void readAll() {
        branches = readObject(BRANCHES_FILE, HashMap.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        HEAD = readObject(HEAD_FILE, Commit.class);
        commits = readObject(COMMITS_FILE, HashMap.class);
        blobs = readObject(BLOBS_FILE, HashMap.class);
    }

    private static void saveAll() {
        writeObject(HEAD_FILE, HEAD);
        writeObject(COMMITS_FILE, commits);
        writeObject(BLOBS_FILE, blobs);
        writeObject(BRANCH_FILE, currentBranch);
        writeObject(BRANCHES_FILE, branches);
    }

    private static void createFile() throws IOException {
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
    }

    public static void init() throws IOException {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        createFile();
        //first commit
        Commit firstCommit = new Commit();
        HEAD = firstCommit;
        firstCommit.commit();
        commits.put(firstCommit.getHashcodeCommit(), firstCommit);
        //default branch name is master
        Branch master = new Branch("master", firstCommit);
        currentBranch = master;
        branches.put("master", master);
        saveAll();
    }

    @SuppressWarnings("unchecked")
    public static void add(String file) throws IOException {
        blobs =  readObject(BLOBS_FILE, HashMap.class);
        HEAD = readObject(HEAD_FILE, Commit.class);
        //if file in rm stage, then "unremove"
        List<String> rm = plainFilenamesIn(RM_AREA);
        if (rm != null) {
            for (String f : rm) {
                if (f.equals(file)) {
                    File beDeleted = join(RM_AREA, f);
                    beDeleted.delete();
                    return;
                }
            }
        }
        //find a local file
        File add = join(CWD, file);
        if (!add.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String addHash = sha1((Object) readContents(add));
        //file equal HEAD's file
        if (HEAD.getFileHashcode().containsKey(file)) {
            if (HEAD.getFileHashcode().get(file).equals(addHash)) {
                return;
            }
        }
        //create the file in stage area
        File addStage = join(STAGING_AREA, file);
        writeContents(addStage, (Object) readContents(add));
        addStage.createNewFile();
        if (!blobs.containsKey(addHash)) {
            blobs.put(addHash, readContents(add));
        }
        writeObject(BLOBS_FILE, blobs);
    }

    public static void commit(String ... message) throws IOException {
        readAll();
        String parentHash = HEAD.getHashcodeCommit();
        Commit commit = new Commit();
        if (message.length == 1) {
            commit = new Commit(message[0], parentHash);
        }
        if (message.length == 3) {
            commit = new Commit(message[0], parentHash, message[1], message[2]);
        }
        //Defaults to the same file as the parent commit
        commit.setFileHashcode(HEAD.getFileHashcode());
        //get all filenames in stage
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        List<String> hashListRm = plainFilenamesIn(RM_AREA);
        if (hashList != null
                && hashListRm != null
                && hashList.isEmpty()
                && hashListRm.isEmpty()) {
            System.out.println("No changes added to the commit.");
        }
        //clear add stage
        if (hashList != null) {
            for (String s : hashList) {
                //whether you find
                boolean flag = false;
                File file = join(STAGING_AREA, s);
                String fileHash = sha1((Object) readContents(file));
                //Determine whether the file hash corresponding
                if (commit.getFileHashcode().isEmpty()) {
                    flag = true;
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                } else {
                    //key is filename
                    for (String key : commit.getFileHashcode().keySet()) {
                        //find equal filename
                        if (s.equals(key)) {
                            commit.addFileHashcode(key, fileHash);
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
        }
        //clear rm stage
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
        saveAll();
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
    public static void checkoutCommit(String commitId, String file) throws IOException {
        readAll();
        HashMap<String, String> shortId = new HashMap<>();
        for (String s : commits.keySet()) {
            String shortI = getShortId(s);
            shortId.put(shortI, s);
        }
        if (shortId.containsKey(commitId)) {
            commitId = shortId.get(commitId);
        }
        //Whether to include this commit
        if (!commits.containsKey(commitId) && !shortId.containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit checkCommit = commits.get(commitId);
        //whether this commit have the file
        if (!checkCommit.getFileHashcode().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String checkfileHash = checkCommit.getFileHashcode().get(file);
        File cwdFile = join(CWD, file);
        isUntracked(file, checkfileHash);
        //get file content and write in CWD
        byte[] checkByte = blobs.get(checkfileHash);
        cwdFile.createNewFile();
        writeContents(cwdFile, (Object) checkByte);
    }

    private static void isUntracked(String file, String checkfileHash) {
        File cwdFile = join(CWD, file);
        String cwdHash = null;
        if (cwdFile.exists()) {
            cwdHash = sha1((Object) readContents(cwdFile));
        }
        if (!checkfileHash.equals(cwdHash)
                && !isTracked(file, cwdHash)
                && cwdHash != null) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    @SuppressWarnings("unchecked")
    public static void checkBranch(String branch) throws IOException {
        HEAD = readObject(HEAD_FILE, Commit.class);
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        Branch giveBranch = branches.get(branch);
        if (!branches.containsKey(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (giveBranch.name.equals(currentBranch.name)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        List<String> cwd = plainFilenamesIn(CWD);
        if (cwd != null) {
            for (String s : cwd) {
                File cwdF = join(CWD, s);
                String cwdHash = sha1((Object) readContents(cwdF));
                if (HEAD.getFileHashcode().containsValue(cwdHash)
                        && !giveBranch.commit.getFileHashcode().containsKey(s)) {
                    cwdF.delete();
                }
            }
        }
        for (String key : giveBranch.commit.getFileHashcode().keySet()) {
            checkoutCommit(giveBranch.commit.getHashcodeCommit(), key);
        }
        currentBranch = branches.get(branch);
        HEAD = branches.get(branch).commit;
        branches.put(currentBranch.name, currentBranch);
        writeObject(HEAD_FILE, HEAD);
        writeObject(BRANCH_FILE, currentBranch);
        writeObject(BRANCHES_FILE, branches);
    }

    public static void rm(String file) throws IOException {
        HEAD = readObject(HEAD_FILE, Commit.class);
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        //if all do not have this file
        if (hashList != null
                && !hashList.contains(file)
                && !HEAD.getFileHashcode().containsKey(file)) {
            System.out.println("No reason to remove the file.");
        }
        //remove from stage
        if (hashList != null) {
            if (hashList.contains(file)) {
                File file1 = join(STAGING_AREA, file);
                file1.delete();
            }
        }
        //remove from HEAD
        if (HEAD.getFileHashcode().containsKey(file)) {
            File rmF = join(RM_AREA, file);
            rmF.createNewFile();
            //remove from CWD
            restrictedDelete(file);
        }
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
            if (name.equals(currentBranch.name)) {
                System.out.println("*"  + name);
            }
        }
        for (String name : branches.keySet()) {
            if (!name.equals(currentBranch.name)) {
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
        List<String> hashListR = plainFilenamesIn(RM_AREA);
        if (hashListR != null) {
            for (String s : hashListR) {
                System.out.println(s);
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
        currentBranch = readObject(BRANCH_FILE, Branch.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        commits = readObject(COMMITS_FILE, HashMap.class);
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

    private static void mergePre(Branch giveBranch) {
        if (giveBranch == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        List<String> file = plainFilenamesIn(STAGING_AREA);
        if (file != null && !file.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (currentBranch.name.equals(giveBranch.name)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    public static void merge(String branch) throws IOException {
        readAll();
        Commit splitCommit;
        Branch giveBranch = branches.get(branch);
        mergePre(giveBranch);
        splitCommit = findSplit(giveBranch);
        String beDelete = null;
        //file exists in split commit
        for (Map.Entry<String, String> entry: splitCommit.getFileHashcode().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String gValue = giveBranch.commit.getFileHashcode().get(key);
            String cValue = currentBranch.commit.getFileHashcode().get(key);
            //two branches delete file commonly
            if (gValue == null && cValue == null) {
                File f = join(CWD, key);
                if (f.exists()) {
                    beDelete = key;
                }
            }
            if (cValue != null && gValue != null) {
                //give branch modifies, current branch does not modify
                if (!value.equals(gValue) && value.equals(cValue)) {
                    checkoutCommit(giveBranch.commit.getHashcodeCommit(), key);
                    add(key);
                }
            }
        }
        //only exist in give
        for (String s : giveBranch.commit.getFileHashcode().keySet()) {
            if (!currentBranch.commit.getFileHashcode().containsKey(s)
                    && !splitCommit.getFileHashcode().containsKey(s)) {
                checkoutCommit(giveBranch.commit.getHashcodeCommit(), s);
                add(s);
            }
        }
        //all tracked
        for (Map.Entry<String, String> entry: splitCommit.getFileHashcode().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String gValue = giveBranch.commit.getFileHashcode().get(key);
            String cValue = currentBranch.commit.getFileHashcode().get(key);
            if (cValue != null && cValue.equals(value) && gValue == null) {
                rm(key);
            }
            if (gValue != null && cValue != null && !value.equals(gValue)
                    && !cValue.equals(gValue)
                    && !value.equals(cValue)) {
                conflict(key, cValue, gValue);
            } else if (gValue != null && !gValue.equals(value) && cValue == null) {
                conflict(key, "", gValue);
            } else if (cValue != null && !cValue.equals(value) && gValue == null) {
                conflict(key, cValue, "");
            }
        }
        //file doesn't exist in split commit
        for (String s : currentBranch.commit.getFileHashcode().keySet()) {
            if (!giveBranch.commit.getFileHashcode().containsKey(s)
                    || splitCommit.getFileHashcode().containsKey(s)) {
                continue;
            }
            String gValue = giveBranch.commit.getFileHashcode().get(s);
            String cValue = currentBranch.commit.getFileHashcode().get(s);
            if (!gValue.equals(cValue)) {
                conflict(s, cValue, gValue);
            }
        }
        String parent2 = giveBranch.commit.getHashcodeCommit();
        addFromCWD(beDelete);
        String mergeMessage = "Merge: "
                + currentBranch.commit.getHashcodeCommit().substring(0, 7)
                + " " + giveBranch.commit.getHashcodeCommit().substring(0, 7);
        String message = "Merged " + branch
                + " into " + currentBranch.name + ".";
        Repository.commit(message, mergeMessage, parent2);
        writeObject(BRANCH_FILE, currentBranch);
        writeObject(BRANCHES_FILE, branches);
    }

    private static void addFromCWD(String beDelete) throws IOException {
        List<String> cwd = plainFilenamesIn(CWD);
        if (cwd != null) {
            for (String s : cwd) {
                if (!s.equals(beDelete)) {
                    add(s);
                }
            }
        }
    }

    public static void conflict(String name, String curr, String give) {
        byte[] currFile = new byte [0];
        byte[] giveFile = new byte [0];

        if ("".equals(curr)) {
            giveFile = blobs.get(give);
        } else if ("".equals(give)) {
            currFile = blobs.get(curr);
        } else {
            currFile = blobs.get(curr);
            giveFile = blobs.get(give);
        }

        File newFile = join(CWD, name);
        writeContents(newFile, "<<<<<<< HEAD\n",
                (Object) currFile, "=======\n",
                (Object) giveFile, ">>>>>>>\n");
        String fHash = sha1((Object) readContents(newFile));
        blobs.put(fHash, readContents(newFile));
        writeObject(BLOBS_FILE, blobs);
        System.out.println("Encountered a merge conflict.");
    }

    private static Commit findSplit(Branch giveBranch) throws IOException {
        List<String> id1 = new ArrayList<>();
        List<String> id2 = new ArrayList<>();
        Commit splitCommit = new Commit();
        Commit parent1 = giveBranch.commit;
        Commit parent2 = currentBranch.commit;
        while (parent1 != null) {
            id1.add(parent1.getHashcodeCommit());
            parent1 = commits.get(parent1.getParent());
        }
        while (parent2 != null) {
            id2.add(parent2.getHashcodeCommit());
            if (parent2.getParent2() != null) {
                parent2 = commits.get(parent2.getParent2());
            } else {
                parent2 = commits.get(parent2.getParent());
            }
        }
        for (String s : id1) {
            if (id2.contains(s)) {
                splitCommit = commits.get(s);
                break;
            }
        }
        if (splitCommit.getHashcodeCommit().equals
                (currentBranch.commit.getHashcodeCommit())) {
            checkBranch(giveBranch.name);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else if (splitCommit.getHashcodeCommit().equals
                (giveBranch.commit.getHashcodeCommit())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        return splitCommit;
    }

    private static String getShortId(String commitHash) {
        String regex = "([a-f0-9]{8})[a-f0-9]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(commitHash);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean isTracked(String filename, String fileHash) {
        Commit temp = HEAD;
        while (temp != null) {
            if (temp.isTracked(filename, fileHash)) {
                return true;
            }
            temp = commits.get(temp.getParent());
        }
        return false;
    }
}



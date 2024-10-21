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
    public static final File COMMIT = Utils.join(Repository.GITLET_DIR, "commit");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File MASTER_FILE = join(GITLET_DIR, "master");
    public static final File COMMITS_FILE = join(GITLET_DIR, "commits");
    public static final File BLOBS_FILE = join(GITLET_DIR, "blobs");

    //提交的哈希值与提交的映射
    public static HashMap<String, Commit> commits = new HashMap<>();
    //分支
    public static Commit HEAD;
    public static Commit master;
    //string是文件的哈希值,file是具体的文件
    public static HashMap<String, File> blobs = new HashMap<>();


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
        Commit firstCommit = new Commit();
        HEAD = firstCommit;
        master = firstCommit;
        firstCommit.commit();
        commits.put(firstCommit.getHashcodeCommit(), firstCommit);
        //保存
        writeObject(BLOBS_FILE, blobs);
        writeObject(HEAD_FILE, HEAD);
        writeObject(MASTER_FILE, master);
        writeObject(COMMITS_FILE, commits);
    }

    public static void add(String file) throws IOException {
        //找到本地文件
        File add = join(CWD, file);
        //得到文件哈希值
        String addHash = sha1(readContents(add));
        blobs = (HashMap<String, File>) readObject(BLOBS_FILE, HashMap.class);
        //遍历blobs,如果要提交的文件与blobs中的文件相同则不添加新的blob
        if (blobs != null){
            for (String key : blobs.keySet()) {
                //找到相同的哈希值就不再添加了
                if (addHash.equals(key)) {
                    //相当于把本地文件复制一份到stage
                    File addStage = join(STAGING_AREA, file);
                    writeContents(addStage, readContents(add));
                    addStage.createNewFile();
                    return;
                }
            }
        }
        //不存在相同,于是创建新的blob,并且在暂存区创建文件
        //!!!这里是唯一改变blobs的地方!!!
        blobs.put(addHash, add);
        System.out.println(readContentsAsString(blobs.get(addHash)));////
        writeObject(BLOBS_FILE, blobs);
        //相当于把本地文件复制一份到stage
        File addStage = join(STAGING_AREA, file);
        writeContents(addStage, (Object) readContents(add));
        addStage.createNewFile();
    }

    public static void commit(String message) throws IOException {
        //获取父提交
        HEAD = readObject(HEAD_FILE, Commit.class);
        master = readObject(MASTER_FILE, Commit.class);
        commits = (HashMap<String, Commit>) readObject(COMMITS_FILE, HashMap.class);
        String parentHash = HEAD.getHashcodeCommit();
        String branchName = HEAD.getBranch();
        Commit commit = new Commit(message, parentHash, branchName);
        //默认与父提交的文件相同
        commit.setFileHashcode(HEAD.getFileHashcode());
        //如果不同,则进行更改
        //获取暂存区所有的文件名
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        //暂存区不为空
        if (hashList != null) {
            for (String s : hashList) {
                //判断是否找到对应文件
                boolean flag = false;
                //根据文件名获取stage里面的文件
                File file = join(STAGING_AREA, s);
                //获取对应文件的哈希
                String fileHash = sha1((Object) readContents(file));
                //判断从父提交继承的文件与stage的文件名对应的文件哈希是否相等
                if (commit.getFileHashcode().isEmpty()) {
                    flag = true;
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                }
                else {
                    //key是文件名
                    for (String key : commit.getFileHashcode().keySet()) {
                        //找到相等的文件名
                        if (s.equals(key)) {
                            //如果哈希值不相等则更新对应文件名的哈希值
                            if (!fileHash.equals(commit.getFileHashcode().get(key))) {
                                commit.addFileHashcode(key, fileHash);
                            }
                            flag = true;
                            file.delete();
                        }
                    }
                }
                //如果没有找到该文件,则直接添加
                if (!flag) {
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                }
            }
        }
        //ToDo:提交树?
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
        commits = (HashMap<String, Commit>) readObject(COMMITS_FILE, HashMap.class);
        while (HEAD != null) {
            printCommit();
            HEAD = (Commit) commits.get(HEAD.getParent());
        }
    }

    private static void printCommit() {
        System.out.println("===");
        System.out.println("commit " + HEAD.getHashcodeCommit());
        System.out.println("Date: " + HEAD.getDate());
        System.out.println(HEAD.getMessage());
        System.out.println();
    }

    public static void checkout(String file) throws IOException {
        HEAD = readObject(HEAD_FILE, Commit.class);
        blobs = (HashMap<String, File>) readObject(BLOBS_FILE, HashMap.class);
        File checkoutFile = join(CWD, file);
        if (HEAD.getFileHashcode().get(file) == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String checkHash = HEAD.getFileHashcode().get(file);
        File checkFile = blobs.get(checkHash);
        System.out.println(readContentsAsString(checkFile));///
        writeContents(checkoutFile, (Object) readContents(checkFile));
        checkoutFile.createNewFile();
        System.out.println(readContentsAsString(checkoutFile));////
    }
}



package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
        Branch master = new Branch("master");
        Commit firstCommit = new Commit();
        master.addCommit(firstCommit);
        firstCommit.commit();
    }

    public static void add(String file) throws IOException {
        //找到本地文件
        File add = join(CWD, file);
        //得到文件哈希值
        String addHash = sha1(readContents(add));
        //遍历blobs,如果要提交的文件与blobs中的文件相同则不进行提交
        if (Commit.getBlobs() != null){
            for (String key : Commit.getBlobs().keySet()) {
                if (addHash.equals(key)) {
                    return;
                }
            }
        }
        //不存在相同,于是创建新的blob,并且在暂存区创建文件
        Commit.addBlob(addHash, add);
        File addStage = join(STAGING_AREA, file);
        writeContents(addStage, readContents(add));
        addStage.createNewFile();
    }

    public static void commit(String message) throws IOException {
        //获取父提交
        Commit parent = Branch.getHead();
        String parentHash = "aaa";//ToDo
        String b = "data";//ToDo
        Commit commit = new Commit(message, parentHash, b);
        //默认与父提交的文件相同
        commit.setFileHashcode(Branch.getHead().getFileHashcode());
        //如果不同,则进行更改
        //获取文件名
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        for (int i = 0; i < hashList.size(); i++) {
            //判断是否找到对应文件
            boolean flag = false;
            //根据文件名获取stage里面的文件
            File file = join(STAGING_AREA, hashList.get(i));
            //获取对应文件的哈希
            String fileHash = sha1(readContents(file));
            //判断与新创建commit的文件名对应的哈希是否相等
            if (commit.getFileHashcode() != null){
                for (String key : commit.getFileHashcode().keySet()) {
                    if (hashList.get(i).equals(key)) {
                        //如果不相等则更新对应文件名的哈希值
                        if (!fileHash.equals(commit.getFileHashcode().get(key))) {
                            commit.getFileHashcode().put(key, fileHash);
                        }
                        flag = true;
                    }
                }
            }
            //如果没有找到该文件,则直接添加
            if (!flag) {
                commit.addFileHashcode(hashList.get(i), fileHash);
            }
        }
        commit.commit();
    }
}



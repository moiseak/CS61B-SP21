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

    //�ύ�Ĺ�ϣֵ���ύ��ӳ��
    public static HashMap<String, Commit> commits = new HashMap<>();
    //��֧
    public static Commit HEAD;
    public static Commit master;
    //string���ļ��Ĺ�ϣֵ,file�Ǿ�����ļ�
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
        //����
        writeObject(BLOBS_FILE, blobs);
        writeObject(HEAD_FILE, HEAD);
        writeObject(MASTER_FILE, master);
        writeObject(COMMITS_FILE, commits);
    }

    public static void add(String file) throws IOException {
        //�ҵ������ļ�
        File add = join(CWD, file);
        //�õ��ļ���ϣֵ
        String addHash = sha1(readContents(add));
        blobs = (HashMap<String, File>) readObject(BLOBS_FILE, HashMap.class);
        //����blobs,���Ҫ�ύ���ļ���blobs�е��ļ���ͬ������µ�blob
        if (blobs != null){
            for (String key : blobs.keySet()) {
                //�ҵ���ͬ�Ĺ�ϣֵ�Ͳ��������
                if (addHash.equals(key)) {
                    //�൱�ڰѱ����ļ�����һ�ݵ�stage
                    File addStage = join(STAGING_AREA, file);
                    writeContents(addStage, readContents(add));
                    addStage.createNewFile();
                    return;
                }
            }
        }
        //��������ͬ,���Ǵ����µ�blob,�������ݴ��������ļ�
        //!!!������Ψһ�ı�blobs�ĵط�!!!
        blobs.put(addHash, add);
        System.out.println(readContentsAsString(blobs.get(addHash)));////
        writeObject(BLOBS_FILE, blobs);
        //�൱�ڰѱ����ļ�����һ�ݵ�stage
        File addStage = join(STAGING_AREA, file);
        writeContents(addStage, (Object) readContents(add));
        addStage.createNewFile();
    }

    public static void commit(String message) throws IOException {
        //��ȡ���ύ
        HEAD = readObject(HEAD_FILE, Commit.class);
        master = readObject(MASTER_FILE, Commit.class);
        commits = (HashMap<String, Commit>) readObject(COMMITS_FILE, HashMap.class);
        String parentHash = HEAD.getHashcodeCommit();
        String branchName = HEAD.getBranch();
        Commit commit = new Commit(message, parentHash, branchName);
        //Ĭ���븸�ύ���ļ���ͬ
        commit.setFileHashcode(HEAD.getFileHashcode());
        //�����ͬ,����и���
        //��ȡ�ݴ������е��ļ���
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        //�ݴ�����Ϊ��
        if (hashList != null) {
            for (String s : hashList) {
                //�ж��Ƿ��ҵ���Ӧ�ļ�
                boolean flag = false;
                //�����ļ�����ȡstage������ļ�
                File file = join(STAGING_AREA, s);
                //��ȡ��Ӧ�ļ��Ĺ�ϣ
                String fileHash = sha1((Object) readContents(file));
                //�жϴӸ��ύ�̳е��ļ���stage���ļ�����Ӧ���ļ���ϣ�Ƿ����
                if (commit.getFileHashcode().isEmpty()) {
                    flag = true;
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                }
                else {
                    //key���ļ���
                    for (String key : commit.getFileHashcode().keySet()) {
                        //�ҵ���ȵ��ļ���
                        if (s.equals(key)) {
                            //�����ϣֵ���������¶�Ӧ�ļ����Ĺ�ϣֵ
                            if (!fileHash.equals(commit.getFileHashcode().get(key))) {
                                commit.addFileHashcode(key, fileHash);
                            }
                            flag = true;
                            file.delete();
                        }
                    }
                }
                //���û���ҵ����ļ�,��ֱ�����
                if (!flag) {
                    commit.addFileHashcode(s, fileHash);
                    file.delete();
                }
            }
        }
        //ToDo:�ύ��?
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



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
        //�ҵ������ļ�
        File add = join(CWD, file);
        //�õ��ļ���ϣֵ
        String addHash = sha1(readContents(add));
        //����blobs,���Ҫ�ύ���ļ���blobs�е��ļ���ͬ�򲻽����ύ
        if (Commit.getBlobs() != null){
            for (String key : Commit.getBlobs().keySet()) {
                if (addHash.equals(key)) {
                    return;
                }
            }
        }
        //��������ͬ,���Ǵ����µ�blob,�������ݴ��������ļ�
        Commit.addBlob(addHash, add);
        File addStage = join(STAGING_AREA, file);
        writeContents(addStage, readContents(add));
        addStage.createNewFile();
    }

    public static void commit(String message) throws IOException {
        //��ȡ���ύ
        Commit parent = Branch.getHead();
        String parentHash = "aaa";//ToDo
        String b = "data";//ToDo
        Commit commit = new Commit(message, parentHash, b);
        //Ĭ���븸�ύ���ļ���ͬ
        commit.setFileHashcode(Branch.getHead().getFileHashcode());
        //�����ͬ,����и���
        //��ȡ�ļ���
        List<String> hashList = plainFilenamesIn(STAGING_AREA);
        for (int i = 0; i < hashList.size(); i++) {
            //�ж��Ƿ��ҵ���Ӧ�ļ�
            boolean flag = false;
            //�����ļ�����ȡstage������ļ�
            File file = join(STAGING_AREA, hashList.get(i));
            //��ȡ��Ӧ�ļ��Ĺ�ϣ
            String fileHash = sha1(readContents(file));
            //�ж����´���commit���ļ�����Ӧ�Ĺ�ϣ�Ƿ����
            if (commit.getFileHashcode() != null){
                for (String key : commit.getFileHashcode().keySet()) {
                    if (hashList.get(i).equals(key)) {
                        //������������¶�Ӧ�ļ����Ĺ�ϣֵ
                        if (!fileHash.equals(commit.getFileHashcode().get(key))) {
                            commit.getFileHashcode().put(key, fileHash);
                        }
                        flag = true;
                    }
                }
            }
            //���û���ҵ����ļ�,��ֱ�����
            if (!flag) {
                commit.addFileHashcode(hashList.get(i), fileHash);
            }
        }
        commit.commit();
    }
}



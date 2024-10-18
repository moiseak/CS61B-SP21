package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Moiads
 */
public class Repository {
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
        Commit firstCommit = new Commit("initial commit", null);
        Branch master = new Branch(firstCommit);
        firstCommit.commit();
    }

    public static void add(String file) throws IOException {
        File addFile = Utils.join(STAGING_AREA, file);
        String fileHash = Utils.sha1(readContents(addFile));
        for (int i = 0; i < Commit.getBlobHash().size(); i++) {
            if (fileHash.equals(Commit.getBlobHash().get(i))) {
                return;
            }
        }
        Commit.addBlob(addFile);
        Commit.addHashBlob(fileHash);
    }
}



package gitlet;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Branch {
    private String branchName;
    private static Commit HEAD = new Commit();
    private Commit Master;
    private LinkedList <Commit> commits;
    public Branch(String branchName) {
        this.branchName = branchName;
        this.commits = new LinkedList<>();
    }

    public static Commit getHead() {
        return HEAD;
    }

    public static void setHead(Commit head) {
        HEAD = head;
    }

    public void addCommit(Commit commit) {
        this.commits.add(commit);
        Branch.setHead(commit);
        this.Master = commit;
    }

    public String getBranchName() {
        return this.branchName;
    }
}

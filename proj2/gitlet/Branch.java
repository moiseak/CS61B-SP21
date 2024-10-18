package gitlet;

import java.util.ArrayList;

public class Branch {
    private String branchName;
    private Commit HEAD;
    private ArrayList<Commit> branchs;
    public Branch(Commit first) {
        this.branchName = "master";
        this.HEAD = first;
        this.branchs = new ArrayList<>();
        this.branchs.add(HEAD);
        first.setBranch(branchName);
    }
}

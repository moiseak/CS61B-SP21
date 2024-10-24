package gitlet;

import java.io.IOException;
import java.util.Objects;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main{

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                try {
                    Repository.init();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "add":
                String secondArg = args[1];
                try {
                    Repository.add(secondArg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "commit":
                //args : commit message
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                }
                String secondArg1 = args[1];
                try {
                    Repository.commit(secondArg1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "log":
                Repository.log();
                break;
            case "checkout":
                // maybe"--",  commitId ,  or branch
                String secondArg2 = args[1];
                if (Objects.equals(secondArg2, "--")) {
                    //first arg checkout,  second  is "--", third is file name
                    //checkout -- []
                    // filename
                    String thirdArg = args[2];
                    //arg is filename
                    try {
                        Repository.checkout(thirdArg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String fourthArg = args[2];
                    if (Objects.equals(fourthArg, "--")) {
                        //args: first is checkout, second is commitId, third is --", fourth is filename
                        //checkout [] -- []

                        //filename
                        String fifthArg = args[3];
                        //args are commitId and file name
                        try {
                            Repository.checkoutCommit(secondArg2, fifthArg);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Repository.checkBranch(secondArg2);
                    }

                }
                break;
            case "rm":
                String secondArg3 = args[1];
                try {
                    Repository.rm(secondArg3);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "global-log":
                Repository.logGlobal();
                break;
            case "find":
                String secondArg4 = args[1];
                Repository.find(secondArg4);
                break;
            case "status":
                Repository.status();
                break;
            case "branch":
                String secondArg5 = args[1];
                Repository.branch(secondArg5);
                break;
            case "rm-branch":
                String secondArg6 = args[1];
                Repository.rmBranch(secondArg6);
                break;
            case "reset":
                String secondArg7 = args[1];
                Repository.reset(secondArg7);
        }
    }
}

package gitlet;

import java.io.IOException;
import java.util.Objects;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                String secondArg = args[1];
                Repository.add(secondArg);
                break;
            case "commit":
                //args : commit message
                String secondArg1 = args[1];
                Repository.commit(secondArg1);
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
                    Repository.checkout(thirdArg);
                } else {
                    String fourthArg = args[2];
                    if (Objects.equals(fourthArg, "--")) {
                        //args : first is checkout, second is commitId, third is --", fourth is filename
                        //checkout [] -- []

                        //filename
                        String fifthArg = args[3];
                        //args are commitId and file name
                        Repository.checkoutCommit(secondArg2, fifthArg);
                    }
//                    else {
//                        //checkout []
//                    }
                }
        }
    }
}

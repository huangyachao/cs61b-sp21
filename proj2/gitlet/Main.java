package gitlet;

import java.util.Objects;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {

        if (args.length ==0) {
            System.out.println("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateOperands(firstArg,args,1);
                Repository.init();
                break;
            case "add":
                validateOperands(firstArg,args,2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateOperands(firstArg,args,2);
                if(Objects.equals(args[1], "")){
                    System.out.println("Please enter a commit message.");
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                validateOperands(firstArg,args,2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateOperands(firstArg,args,1);
                Repository.log();
                break;
            case "global-log":
                validateOperands(firstArg,args,1);
                Repository.globalLog();
                break;
            case "find":
                validateOperands(firstArg,args,2);
                Repository.find(args[1]);
                break;
            case "status":
                validateOperands(firstArg,args,1);
                Repository.status();
                break;
            case "checkout":
                if(args.length ==2){
                    Repository.checkoutBranch(args[1]);
                }
                else if(args.length ==3){
                    if(!Objects.equals(args[1], "--")){
                        System.out.println("Incorrect operands.");
                    }else{
                        Repository.checkoutFile(args[2],null);
                    }
                }else if(args.length ==4){
                    if(!Objects.equals(args[2], "--")){
                        System.out.println("Incorrect operands.");
                    }else{
                        Repository.checkoutFile(args[3],args[1]);
                    }
                }
                break;
            case "branch":
                break;
            case "rm-branch":
                validateOperands(firstArg,args,2);
                break;
            case "reset":
                validateOperands(firstArg,args,2);
                break;
            case "merge":
                validateOperands(firstArg,args,2);
                break;

            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
    public static void validateOperands(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}

package gitlet;

import java.util.Objects;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author haya
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateOperands(firstArg, args, 1);
                if (Repository.isInitialized()) {
                    System.out.println("A Gitlet version-control system already exists in the current " +
                            "directory.");
                } else {
                    Repository.init();
                }
                break;
            case "add":
                validateOperands(firstArg, args, 2);
                if (validateInitialized()) {
                    Repository.add(args[1]);
                }

                break;
            case "commit":
                validateOperands(firstArg, args, 2);
                if (Objects.equals(args[1], "")) {
                    System.out.println("Please enter a commit message.");
                }
                if (validateInitialized()) {
                    Repository.commit(args[1]);
                }
                break;
            case "rm":
                validateOperands(firstArg, args, 2);
                if (validateInitialized()) {
                    Repository.rm(args[1]);
                }
                break;
            case "log":
                validateOperands(firstArg, args, 1);
                if (validateInitialized()) {
                    Repository.log();
                }
                break;
            case "global-log":
                validateOperands(firstArg, args, 1);
                if (validateInitialized()) {
                    Repository.globalLog();
                }
                break;
            case "find":
                validateOperands(firstArg, args, 2);
                if (validateInitialized()) {
                    Repository.find(args[1]);
                }
                break;
            case "status":
                validateOperands(firstArg, args, 1);
                if (validateInitialized()) {
                    Repository.status();
                }
                break;
            case "checkout":
                if (validateInitialized()) {
                    if (args.length == 2) {
                        Repository.checkoutBranch(args[1]);
                    } else if (args.length == 3) {
                        if (!Objects.equals(args[1], "--")) {
                            System.out.println("Incorrect operands.");
                        } else {
                            Repository.checkoutFile(args[2], null);
                        }
                    } else if (args.length == 4) {
                        if (!Objects.equals(args[2], "--")) {
                            System.out.println("Incorrect operands.");
                        } else {
                            Repository.checkoutFile(args[3], args[1]);
                        }
                    }
                }
                break;
            case "branch":
                validateOperands(firstArg, args, 2);
                if (validateInitialized()) {
                    Repository.branch(args[1]);
                }
                break;
            case "rm-branch":
                validateOperands(firstArg, args, 2);
                if (validateInitialized()) {
                    Repository.rmBranch(args[1]);
                }
                break;
            case "reset":
                validateOperands(firstArg, args, 2);
                validateInitialized();
                break;
            case "merge":
                validateOperands(firstArg, args, 2);
                validateInitialized();
                break;

            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

    public static boolean validateInitialized() {
        if (!Repository.isInitialized()) {
            System.out.println("Not in a initialized Gitlet directory.");
            return false;
        }
        return true;
    }

    public static void validateOperands(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}

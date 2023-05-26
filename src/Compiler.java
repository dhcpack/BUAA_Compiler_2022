import Config.Config;

import java.io.IOException;

public class Compiler {
    // Debug Version
    // public static void main(String[] args) throws IOException {
    //     // command: java -jar Mars2022.jar nc mips.txt
    //     if (Config.debugMode) {
    //         TestAll.run();
    //         TestWriter.flush();
    //     } else {
    //         Config.mipsRun(Config.inputFile);
    //     }
    // }

    // Release Version
    static boolean parseArgs(String[] args) {
        if (args.length == 1) {
            Config.inputFile = args[0];
            return true;
        }
        if (args.length == 3) {
            Config.inputFile = args[0];
            if (args[1].equals("-o")) {
                Config.mipsFile = args[2];
                return true;
            }
            return false;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Yuelin's Compiler version 6.0");
        if (parseArgs(args)) {
            Config.mipsRun(Config.inputFile);
            System.out.println("Compile Succeed!");
        } else {
            System.out.println("Usage: <source-file> [-o <target-file>]");
            System.out.println("    -o               " +
                    "set target file, default to mips.txt");
            System.out.println("Compile Terminate!");
        }
        System.out.println();
    }
}

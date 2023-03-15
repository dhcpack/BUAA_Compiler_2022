import Config.Config;
import Config.TestWriter;
import Test.TestAll;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // command: java -jar Mars2022.jar nc mips.txt
        if (Config.debugMode) {
            TestAll.run();
            TestWriter.flush();
        } else {
            Config.mipsRun(Config.inputFile);
        }
    }
}

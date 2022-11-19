import Config.Config;
import Config.TestWriter;
import Test.TestAll;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // command: java -jar ./src/Test/Mars2022.jar nc mips.txt
        /*
        * Unfixed error type
        *
            int x[0];
            int main(){
                return x[];
            }
        * */
        if (Config.debugMode) {
            TestAll.run();
            TestWriter.flush();
        } else {
            Config.mipsRun(Config.inputFile);
        }
    }
}
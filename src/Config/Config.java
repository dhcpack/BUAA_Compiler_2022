package Config;

public class Config {
    public static final boolean debugMode = false;
    private static final int testpoint = 1;
    private static final String[] inputFiles =
            {String.format("mytest.txt", testpoint), "testfile.txt"};
    public static final String inputFile = debugMode ? inputFiles[0] : inputFiles[1];
    public static final String outputFile = "error.txt";
    public static final String expectedFile = String.format("myoutput.txt",
            testpoint);
}

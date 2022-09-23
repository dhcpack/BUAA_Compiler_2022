package Config;

public class Config {
    public static boolean debugMode = false;
    private static final String[] inputFiles = {"../2021-testfiles/testfiles-only/C/testfile9.txt", "testfile.txt"};
    public static String inputFile = debugMode ? inputFiles[0] : inputFiles[1];
    public static String outputFile = "output.txt";
}

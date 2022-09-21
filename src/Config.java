public class Config {
    public static boolean debugMode = false;
    private static final String[] inputFiles = {"../2021testfiles/full/A/testfile21.txt", "testfile.txt"};
    public static String inputFile = debugMode ? inputFiles[0] : inputFiles[1];
    public static String outputFile = "output.txt";
}

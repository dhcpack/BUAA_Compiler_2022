package Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MipsWriter {
    public static String string = "";

    public static void print(String s, Object o) {
        string += s;
        if (string.charAt(string.length() - 1) != '\n') {
            string += "\n";
        }
    }

    public static void print(String s) {
        string += ("\t" + s);
        if (string.charAt(string.length() - 1) != '\n') {
            string += "\n";
        }
    }

    public static void flush() throws IOException {
        File file = new File(Config.mipsFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(string);
        writer.close();
    }
}

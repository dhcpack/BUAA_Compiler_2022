package Config;

import Frontend.Lexer.InputHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ErrorWriter {
    public static String string = "";

    public static void print(String s) {
        string += s;
        if (string.charAt(string.length() - 1) != '\n') {
            string += "\n";
        }
    }

    public static void flush() throws IOException {
        File file = new File(Config.errorFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(string);
        writer.close();
    }
}

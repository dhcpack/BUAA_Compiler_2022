package Config;

import Frontend.Lexer.InputHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
    public static String string = "";

    public static InputHandler input() throws IOException {
        InputHandler inputHandler = new InputHandler();
        File file = new File(Config.inputFile);
        if (!file.exists()) {
            System.out.println("input file not exists");
            return inputHandler;
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null)
            inputHandler.add(line);
        return inputHandler;
    }
}

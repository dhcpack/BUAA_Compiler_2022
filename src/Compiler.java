import Config.IO;
import Lexer.Lexer;
import Parser.Parser;
import Parser.TokenHandler;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // Parser parser = new Parser(new TokenHandler(Lexer.lex(Config.IO.input())));
        // parser.parse();
        Parser.parse(new TokenHandler(Lexer.lex(IO.input())));
    }
}

import Config.IO;
import Lexer.Lexer;
import Parser.Parser;
import Parser.TokenHandler;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Parser.parseCompUnit(new TokenHandler(Lexer.lex(IO.input())));

        Parser.parseCompUnit(new TokenHandler(Lexer.lex(IO.input()))).output();
        IO.flush();
    }
}

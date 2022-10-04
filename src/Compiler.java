import Config.Config;
import Config.IO;
import Lexer.Lexer;
import Parser.Parser;
import Parser.TokenHandler;
import Symbol.SymbolTableBuilder;
import Util.Check;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        new SymbolTableBuilder(Parser.parseCompUnit(new TokenHandler(Lexer.lex(IO.input())))).checkCompUnit();
        IO.flush();
        if (Config.debugMode) {
            Check.check();
        }
    }
}
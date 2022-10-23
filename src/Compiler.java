import Config.Config;
import Config.IO;
import Lexer.Lexer;
import Parser.CompUnit;
import Parser.Parser;
import Parser.TokenHandler;
import Symbol.SymbolTableBuilder;
import Util.CheckUtil;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // CompUnit compUnit = Parser.parseCompUnit(new TokenHandler(Lexer.lex(IO.input())));
        /*
        * Unfixed error type
        *
            int x[0];
            int main(){
                return x[];
            }
        * */
        new SymbolTableBuilder(Parser.parseCompUnit(new TokenHandler(Lexer.lex(IO.input())))).checkCompUnit();
        IO.flush();
        // if (Config.debugMode) {
        //     CheckUtil.check();
        // }
    }
}
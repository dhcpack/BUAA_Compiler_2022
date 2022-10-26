import Config.IO;
import Frontend.Lexer.Lexer;
import Frontend.Parser.Parser;
import Frontend.Parser.TokenHandler;
import Frontend.Symbol.SymbolTableBuilder;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // CompUnit compUnit = frontend.Parser.parseCompUnit(new TokenHandler(frontend.Lexer.lex(IO.input())));
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
import BackEnd.MipsCode;
import BackEnd.Translator;
import Config.Reader;
import Frontend.Lexer.Lexer;
import Frontend.Parser.Parser;
import Frontend.Parser.TokenHandler;
import Frontend.Symbol.Errors;
import Frontend.SymbolTableBuilder;
import Middle.MiddleCode;

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
        SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(
                Parser.parseCompUnit(new TokenHandler(Lexer.lex(Reader.input()))));
        symbolTableBuilder.checkCompUnit();
        MiddleCode middleCode = symbolTableBuilder.getMiddleCode();
        middleCode.output();  // through MiddleWriter

        MipsCode mipsCode = new Translator(middleCode).translate();
        mipsCode.output();  // through MipsWriter

        Errors errors = symbolTableBuilder.getErrors();
        errors.output();  // through ErrorWriter
    }
}
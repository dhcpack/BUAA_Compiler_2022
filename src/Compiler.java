import BackEnd.MipsCode;
import BackEnd.Translator;
import BackEnd.optimizer.DeleteUselessJump;
import Config.Config;
import Config.Reader;
import Config.TestWriter;
import Frontend.Lexer.Lexer;
import Frontend.Parser.CompUnit;
import Frontend.Parser.Parser;
import Frontend.Parser.TokenHandler;
import Frontend.Symbol.Errors;
import Frontend.SymbolTableBuilder;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.FuncBlock;
import Test.TestAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // command: java -jar ./src/Test/Mars2022.jar nc mips.txt
        /*
        * Unfixed error type
        *
            int x[0];
            int main(){
                return x[];
            }
        * */
        if (Config.debugMode) {
            TestAll.run();
            TestWriter.flush();
        } else {
            CompUnit compUnit = Parser.parseCompUnit(new TokenHandler(Lexer.lex(Reader.input(Config.inputFile))));
            SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(compUnit);
            symbolTableBuilder.checkCompUnit();

            // compUnit.output();  // through SyntaxWriter

            Errors errors = symbolTableBuilder.getErrors();
            errors.output();  // through ErrorWriter

            MiddleCode middleCode = symbolTableBuilder.getMiddleCode();
            LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = middleCode.getFuncToSortedBlock();

            // output middle code
            middleCode.output();  // through MiddleWriter

            MipsCode mipsCode = new Translator(middleCode).translate();

            // Mips Optimize
            DeleteUselessJump.optimize(mipsCode);

            // output mips code
            mipsCode.output();  // through MipsWriter
        }
    }
}
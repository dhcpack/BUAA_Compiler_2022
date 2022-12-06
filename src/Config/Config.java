package Config;

import BackEnd.MipsCode;
import BackEnd.Translator;
import BackEnd.optimizer.DeleteUselessJump;
import BackEnd.optimizer.DeleteUselessMips;
import Frontend.Lexer.Lexer;
import Frontend.Parser.CompUnit;
import Frontend.Parser.Parser;
import Frontend.Parser.TokenHandler;
import Frontend.SymbolTableBuilder;
import Middle.MiddleCode;

import java.io.IOException;
import java.io.PrintStream;

public class Config {
    public static final boolean debugMode = false;

    public static final String inputFile = "testfile.txt";
    public static final String syntaxFile = "syntax.txt";
    public static final String middleFile = "middle.txt";
    public static final String mipsFile = "mips.txt";
    public static final String errorFile = "error.txt";
    public static final String expectedFile = "output.txt";

    public static void mipsRun(String filename) throws IOException {
        CompUnit compUnit = Parser.parseCompUnit(new TokenHandler(Lexer.lex(Reader.input(filename))));
        SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(compUnit);
        symbolTableBuilder.checkCompUnit();

        // compUnit.output();  // through SyntaxWriter

        // Errors errors = symbolTableBuilder.getErrors();
        // errors.output();  // through ErrorWriter

        MiddleCode middleCode = symbolTableBuilder.getMiddleCode();

        // Middle Optimize
        // ExtractCommonExpr.optimize(middleCode);

        // output middle code
        middleCode.output();  // through MiddleWriter

        MipsCode mipsCode = new Translator(middleCode).translate();

        // Mips Optimize
        DeleteUselessJump.optimize(mipsCode);
        DeleteUselessMips.optimize(mipsCode);

        // output mips code
        mipsCode.output(new PrintStream(Config.mipsFile));  // through MipsWriter
    }
}

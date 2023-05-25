package Config;

import BackEnd.MipsCode;
import BackEnd.Translator;
import BackEnd.optimizer.DeleteUselessJump;
import BackEnd.optimizer.DeleteUselessMips;
import BackEnd.optimizer.OptimizeBranch;
import Frontend.Lexer.Lexer;
import Frontend.Parser.CompUnit;
import Frontend.Parser.Parser;
import Frontend.Parser.TokenHandler;
import Frontend.Symbol.Errors;
import Frontend.SymbolTableBuilder;
import Middle.MiddleCode;
import Middle.optimizer.DeleteUselessMiddleCode;
import Middle.optimizer.ExtractCommonExpr;
import Middle.optimizer.MergeBranch;

import java.io.IOException;
import java.io.PrintStream;

public class Config {
    public static final boolean debugMode = true;

    public static String inputFile = "testfile.txt";
    public static final String syntaxFile = "syntax.txt";
    public static final String middleFile = "middle.txt";
    public static String mipsFile = "mips.txt";
    public static final String errorFile = "error.txt";
    public static final String expectedFile = "output.txt";

    public static void mipsRun(String filename) throws IOException {
        CompUnit compUnit = Parser.parseCompUnit(new TokenHandler(Lexer.lex(Reader.input(filename))));
        SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(compUnit);
        symbolTableBuilder.checkCompUnit();

        // compUnit.output();  // through SyntaxWriter

        Errors errors = symbolTableBuilder.getErrors();
        errors.output();  // through ErrorWriter

        if (errors.hasErrors()) {
            return;
        }

        MiddleCode middleCode = symbolTableBuilder.getMiddleCode();

        // Middle Optimize
        ExtractCommonExpr.optimize(middleCode);
        MergeBranch.optimize(middleCode);
        DeleteUselessMiddleCode.optimize(middleCode);

        // output middle code
        // middleCode.output();  // through MiddleWriter

        MipsCode mipsCode = new Translator(middleCode).translate();

        // Mips Optimize
        DeleteUselessJump.optimize(mipsCode);
        DeleteUselessMips.optimize(mipsCode);
        OptimizeBranch.optimize(mipsCode);

        // output mips code
        mipsCode.output(new PrintStream(Config.mipsFile));  // through MipsWriter
    }
}

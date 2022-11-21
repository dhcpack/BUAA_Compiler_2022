package Config;

import BackEnd.MipsCode;
import BackEnd.Translator;
import BackEnd.optimizer.DeleteUselessJump;
import Frontend.Lexer.Lexer;
import Frontend.Parser.CompUnit;
import Frontend.Parser.Parser;
import Frontend.Parser.TokenHandler;
import Frontend.Symbol.Errors;
import Frontend.SymbolTableBuilder;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.FuncBlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Config {
    public static final boolean debugMode = true;

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

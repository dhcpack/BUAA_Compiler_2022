package Parser;

import Lexer.Token;
import Lexer.TokenType;
import Parser.decl.DeclParser;
import Parser.decl.types.Decl;
import Parser.func.FuncParser;
import Parser.func.types.FuncDef;
import Parser.func.types.MainFuncDef;
import Symbol.SymbolTable;

import java.util.ArrayList;

public class Parser {
    // CompUnit → {Decl} {FuncDef} MainFuncDef
    public static CompUnit parseCompUnit(TokenHandler tokenHandler) {
        Token first = tokenHandler.getTokenAndMove();
        Token second = tokenHandler.getForwardToken();
        tokenHandler.retract(1);

        // parse global variables
        ArrayList<Decl> globalVariables = new ArrayList<>();
        while (!tokenHandler.reachEnd()) {
            if (first.getType() == TokenType.CONSTTK && second.getType() == TokenType.INTTK) {
                globalVariables.add(new DeclParser(tokenHandler).parseDecl());
            } else if (first.getType() == TokenType.INTTK && second.getType() == TokenType.IDENFR) {
                tokenHandler.moveForward(2);
                Token third = tokenHandler.getForwardToken();
                tokenHandler.retract(2);
                if (third.getType() != TokenType.LPARENT) {
                    globalVariables.add(new DeclParser(tokenHandler).parseDecl());
                } else {
                    break;
                }
            } else {
                break;
            }
            first = tokenHandler.getTokenAndMove();
            second = tokenHandler.getForwardToken();
            tokenHandler.retract(1);
        }

        // parse functions
        ArrayList<FuncDef> functions = new ArrayList<>();
        MainFuncDef mainFunction = null;
        while (!tokenHandler.reachEnd()) {
            if (first.getType() == TokenType.INTTK && second.getType() == TokenType.MAINTK) {
                mainFunction = new FuncParser(tokenHandler).parseMainFuncDef();  // encounter main function
                break;
            } else {
                functions.add(new FuncParser(tokenHandler).parseFuncDef());
            }
            first = tokenHandler.getTokenAndMove();
            second = tokenHandler.getForwardToken();
            tokenHandler.retract(1);
        }
        return new CompUnit(globalVariables, functions, mainFunction);
    }
}

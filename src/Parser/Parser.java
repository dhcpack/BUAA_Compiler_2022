package Parser;

import Lexer.Token;
import Lexer.Type;
import Parser.decl.DeclParser;
import Parser.decl.types.Decl;
import Parser.func.FuncParser;
import Parser.func.types.FuncDef;
import Parser.func.types.MainFuncDef;

import java.util.ArrayList;

public class Parser {
    // CompUnit → {Decl} {FuncDef} MainFuncDef
    public static CompUnit parseCompUnit(TokenHandler tokenHandler) {
        Token first = tokenHandler.getTokenAndMove();
        Token second = tokenHandler.getForwardToken();
        tokenHandler.retract(1);

        ArrayList<Decl> globalVariables = new ArrayList<>();
        while (!tokenHandler.reachEnd()) {
            if (first.getType() == Type.CONSTTK && second.getType() == Type.INTTK) {
                globalVariables.add(new DeclParser(tokenHandler).parseDecl());
            } else if (first.getType() == Type.INTTK && second.getType() == Type.IDENFR) {
                tokenHandler.moveForward(2);
                Token third = tokenHandler.getForwardToken();
                tokenHandler.retract(2);
                if (third.getType() != Type.LPARENT) {
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

        ArrayList<FuncDef> functions = new ArrayList<>();
        MainFuncDef mainFunction = null;
        while (!tokenHandler.reachEnd()) {
            if (first.getType() == Type.INTTK && second.getType() == Type.MAINTK) {
                mainFunction = new FuncParser(tokenHandler).parseMainFuncDef();
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

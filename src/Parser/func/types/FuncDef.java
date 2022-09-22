package Parser.func.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.stmt.types.BlockStmt;

import java.util.ArrayList;

public class FuncDef implements Output {
    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
    private final Token funcType;
    private final Token ident;
    private final Token left;
    private final Token right;
    private final ArrayList<FuncFParam> funcFParams;
    private final ArrayList<Token> seps;
    private final BlockStmt blockStmt;

    public FuncDef(Token funcType, Token ident, Token left, ArrayList<FuncFParam> funcFParams, Token right,
                   BlockStmt blockStmt, ArrayList<Token> seps) {
        this.funcType = funcType;
        this.ident = ident;
        this.left = left;
        this.funcFParams = funcFParams;
        this.right = right;
        this.blockStmt = blockStmt;
        this.seps = seps;
    }

    public void printNormal(boolean isMain) {
        IO.print(funcType.toString());
        if (!isMain) {
            IO.print("<FuncType>");
        }
        IO.print(ident.toString());
        IO.print(left.toString());
        if (funcFParams.size() != 0) {
            funcFParams.get(0).output();
            int index = 1;
            for (Token sep : seps) {
                IO.print(sep.toString());
                funcFParams.get(index++).output();
            }
            IO.print("<FuncFParams>");
        }
        IO.print(right.toString());
        blockStmt.output();
    }

    private void printTag() {
        IO.print("<FuncDef>");
    }

    @Override
    public void output() {
        printNormal(false);
        printTag();
    }
}

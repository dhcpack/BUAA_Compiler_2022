package Parser.func.types;

import Lexer.Token;
import Lexer.Type;
import Parser.Output;
import Parser.stmt.types.BlockStmt;

import java.util.ArrayList;

public class MainFuncDef implements Output {
    // // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
    // private final Type funcType;
    // private final Token ident;
    // private final ArrayList<FuncFParam> funcFParams;
    // private final BlockStmt blockStmt;
    //
    // public MainFuncDef(Type funcType, Token ident, ArrayList<FuncFParam> funcFParams, BlockStmt blockStmt) {
    //     this.funcType = funcType;
    //     this.ident = ident;
    //     this.funcFParams = funcFParams;
    //     this.blockStmt = blockStmt;
    // }
    private final FuncDef funcDef;

    public MainFuncDef(FuncDef funcDef) {
        this.funcDef = funcDef;
    }

    @Override
    public void output() {

    }
}

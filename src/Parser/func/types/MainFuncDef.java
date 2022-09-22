package Parser.func.types;

import Config.IO;
import Parser.Output;

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
        funcDef.printNormal();
        IO.print("<MainFuncDef>");
    }
}

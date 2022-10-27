package Frontend.Parser.func.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;
import Frontend.Parser.stmt.types.BlockStmt;

import java.util.ArrayList;

public class FuncDef implements Output {
    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private final Token returnType;
    private final Token ident;
    private final Token left;
    private final Token right;  // error check: right could be null
    private final ArrayList<FuncFParam> funcFParams;
    private final ArrayList<Token> seps;
    private final BlockStmt blockStmt;

    public FuncDef(Token funcType, Token ident, Token left, ArrayList<FuncFParam> funcFParams, Token right,
                   BlockStmt blockStmt, ArrayList<Token> seps) {
        this.returnType = funcType;
        this.ident = ident;
        this.left = left;
        this.funcFParams = funcFParams;
        this.right = right;
        this.blockStmt = blockStmt;
        this.seps = seps;
    }

    public Token getLeftParenthesis() {
        return this.left;
    }

    public Token getRightBrace() {
        return this.blockStmt.getRightBrace();
    }

    public Token getReturn() {
        return this.blockStmt.getReturn();
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    public Token getReturnType() {
        return returnType;
    }

    public Token getIdent() {
        return ident;
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public boolean returnInt() {
        return blockStmt.returnInt();
    }

    public BlockStmt getBlockStmt() {
        return blockStmt;
    }

    public void printNormal(boolean isMain) {
        SyntaxWriter.print(returnType.toString());
        if (!isMain) {  // if not main, print <FuncType>
            SyntaxWriter.print("<FuncType>");
        }
        SyntaxWriter.print(ident.toString());
        SyntaxWriter.print(left.toString());
        if (funcFParams.size() != 0) {  // if has formal Params
            funcFParams.get(0).output();
            int index = 1;
            for (Token sep : seps) {
                SyntaxWriter.print(sep.toString());
                funcFParams.get(index++).output();
            }
            SyntaxWriter.print("<FuncFParams>");
        }
        SyntaxWriter.print(right.toString());
        blockStmt.output();
    }

    @Override
    public void output() {
        printNormal(false);
        SyntaxWriter.print("<FuncDef>");
    }
}

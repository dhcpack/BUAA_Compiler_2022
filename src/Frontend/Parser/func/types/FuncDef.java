package Frontend.Parser.func.types;

import Frontend.Lexer.Token;
import Frontend.Parser.stmt.types.BlockStmt;

import java.util.ArrayList;

public class FuncDef {
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

    public Token getLeft() {
        return left;
    }

    public Token getRight() {
        return right;
    }

    public ArrayList<Token> getSeps() {
        return seps;
    }

    public String printNormal(boolean isMain) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(returnType);
        if (!isMain) {  // if not main, print <FuncType>
            stringBuilder.append("<FuncType>\n");
        }
        stringBuilder.append(ident).append(left);
        if (funcFParams.size() != 0) {  // if has formal Params
            stringBuilder.append(funcFParams.get(0));
            int index = 1;
            for (Token sep : seps) {
                stringBuilder.append(sep).append(funcFParams.get(index++));
            }
            stringBuilder.append("<FuncFParams>\n");
        }
        stringBuilder.append(right).append(blockStmt);
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return printNormal(false) + "<FuncDef>\n";
    }
}

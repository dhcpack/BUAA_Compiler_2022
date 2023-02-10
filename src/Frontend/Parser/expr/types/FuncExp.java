package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;
import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

public class FuncExp implements UnaryExpInterface, LeafNode {
    // 函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
    // 函数实参表 FuncRParams → Exp { ',' Exp }
    private final Token ident;
    private final Token left;
    private final Token right;    // error check: right could be null
    private final FuncRParams params;  // Waining: params could be null
    private SymbolType returnType;

    public FuncExp(Token token, Token left, Token right, FuncRParams params) {
        this.ident = token;
        this.left = left;
        this.right = right;
        this.params = params;
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    public int getLine() {
        if (params == null) {
            return left.getLine();
        } else if (params.getExps().size() != 0) {
            return this.params.getLine();
        } else {
            return this.left.getLine();
        }
    }

    public Token getIdent() {
        return ident;
    }

    // Warning: params could be null
    public FuncRParams getParams() {
        return params;
    }

    public SymbolType getReturnType() {
        assert returnType != null;
        return returnType;
    }

    public Token getLeft() {
        return left;
    }

    public Token getRight() {
        return right;
    }

    public void setReturnType(SymbolType returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        if (params != null) {  // params could be null
            return ident.toString() + left.toString() + params + right.toString();
        } else {
            return ident.toString() + left.toString()  + right.toString();
        }
    }

    @Override
    public SymbolType getSymbolType() {
        // return type之前一定赋值完成了
        assert returnType != null;
        return returnType;
    }

    @Override
    public int getDimCount() {
        return 0;  //
    }

    @Override
    public ArrayList<Integer> getDimSize() {
        return new ArrayList<>();
    }
}

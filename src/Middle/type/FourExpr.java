package Middle.type;

import Frontend.Parser.expr.types.LeafNode;
import Frontend.Symbol.Symbol;

public class FourExpr extends BlockNode {
    public enum ExprOp {
        DEF,  // int a = b + c;

        ADD,
        SUB,
        MUL,
        DIV,
        MOD,

        AND,
        OR,

        GT,
        GE,
        LT,
        LE,
        EQ,
        NEQ,

        NOT,

        ASS,
    }

    private final LeafNode left;
    private final LeafNode right;
    private final Symbol res;
    private final ExprOp op;

    public FourExpr(LeafNode left, Symbol res, ExprOp op) {
        this.left = left;
        this.right = null;
        this.res = res;
        this.op = op;
    }

    public FourExpr(LeafNode left, LeafNode right, Symbol res, ExprOp op) {
        this.left = left;
        this.right = right;
        this.res = res;
        this.op = op;
    }

    public LeafNode getLeft() {
        return left;
    }

    public LeafNode getRight() {
        return right;
    }

    public Symbol getRes() {
        return res;
    }

    public ExprOp getOp() {
        return op;
    }
}

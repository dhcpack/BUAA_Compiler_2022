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

    private final Operand left;
    private final Operand right;
    private final Symbol res;
    private final ExprOp op;

    public FourExpr(Operand left, Symbol res, ExprOp op) {
        this.left = left;
        this.right = null;
        this.res = res;
        this.op = op;
    }

    public FourExpr(Operand left, Operand right, Symbol res, ExprOp op) {
        this.left = left;
        this.right = right;
        this.res = res;
        this.op = op;
    }

    public Operand getLeft() {
        return left;
    }

    public Operand getRight() {
        return right;
    }

    public Symbol getRes() {
        return res;
    }

    public ExprOp getOp() {
        return op;
    }

    @Override
    public String toString() {
        if (this.right != null) {
            return this.op.name() + ", " + this.left + ", " + this.right + ", " + this.res;
        } else {
            return this.op.name() + ", " + this.left + ", " + this.res;
        }
    }
}

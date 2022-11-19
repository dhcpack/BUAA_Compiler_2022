package Middle.type;

import Frontend.Symbol.Symbol;

public class FourExpr implements BlockNode {
    public enum ExprOp {
        // 双操作数
        ADD,
        SUB,
        MUL,
        DIV,
        MOD,
        GT,
        GE,
        LT,
        LE,
        EQ,
        NEQ,


        // 单操作数
        DEF,  // int a = b + c;
        ASS,
        NOT,
        NEG,

        // not used
        AND,  // 短路求值
        OR,  // 短路求值
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

    public boolean isSingle() {
        return this.right == null;
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
            return this.op.name() + ", " + this.res + ", " + this.left + ", " + this.right;
        } else {
            return this.op.name() + ", " + this.res + ", " + this.left;
        }
    }
}

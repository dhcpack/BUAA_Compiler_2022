package Middle.type;

import Frontend.Symbol.Symbol;

public class FourExpr extends BlockNode {
    public enum ExprOp {
        // 双操作数
        ADD,
        MUL,
        EQ,
        NEQ,
        AND,  // 短路求值
        OR,  // 短路求值

        SUB,
        DIV,
        MOD,
        GT,
        GE,
        LT,
        LE,


        // 单操作数
        DEF,  // int a = b + c;
        ASS,
        NOT,
        NEG;

        public boolean isSingle() {
            return this == DEF || this == ASS || this == NOT || this == NEG;
        }

        public boolean couldSwap() {
            if (this == ADD || this == MUL || this == EQ || this == NEQ || this == AND || this == OR) {
                return true;
            } else if (this == SUB || this == DIV || this == MOD || this == GT || this == GE || this == LT || this == LE) {
                return false;
            }
            assert false;
            return false;
        }
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

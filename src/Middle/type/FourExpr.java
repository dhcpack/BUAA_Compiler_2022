package Middle.type;

import Frontend.Symbol.Symbol;

public class FourExpr extends BlockNode {
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

        AND,  // 短路求值
        OR,  // 短路求值
    }

    private  Operand left;
    private  Operand right;
    private Symbol res;
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

    private boolean replaced = false;

    public boolean replaced(){
        return replaced;
    }

    public void setReplaced(){
        this.replaced = true;
    }

    public Operand getLeft() {
        return left;
    }

    public Operand getRight() {
        return right;
    }

    public void setLeft(Operand left) {
        this.left = left;
    }

    public void setRight(Operand right) {
        this.right = right;
    }

    public void setRes(Symbol res) {
        this.res = res;
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

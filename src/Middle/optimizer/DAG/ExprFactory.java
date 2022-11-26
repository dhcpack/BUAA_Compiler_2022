package Middle.optimizer.DAG;

import Frontend.Symbol.Symbol;
import Middle.type.FourExpr;
import Middle.type.Operand;

import java.util.HashSet;

public class ExprFactory {
    private DAGNode leftNode;
    private DAGNode rightNode;
    private FourExpr.ExprOp exprOp;

    private DAGNode resNode;  // 产生第一个表达式后，对resNode做assign操作

    // 是否已经产生一个了，如果产生则使用ASS即可
    private boolean produced = false;
    private Symbol producedSymbol;

    public ExprFactory(DAGNode leftNode, DAGNode rightNode, DAGNode resNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.resNode = resNode;
        this.exprOp = resNode.getExprOp();
    }

    public FourExpr produce(Symbol res) {
        if (produced) {
            return new FourExpr(producedSymbol, res, FourExpr.ExprOp.ASS);
        }

        if (exprOp.isSingle()) {
            if (leftNode.getOperand() == null) {
                return null;
            }
            produced = true;
            producedSymbol = res;
            resNode.setOperand(res);
            return new FourExpr(leftNode.getOperand(), res, exprOp);
        } else {
            if (leftNode.getOperand() == null || rightNode.getOperand() == null) {
                return null;
            }
            produced = true;
            producedSymbol = res;
            resNode.setOperand(res);
            return new FourExpr(leftNode.getOperand(), rightNode.getOperand(), res, exprOp);
        }
    }
};
package Middle.optimizer.DAG;

import Middle.type.FourExpr;
import Middle.type.Operand;

import java.util.HashSet;

public class DAGNode {
    private Operand operand;
    private final FourExpr.ExprOp exprOp;
    private final HashSet<DAGNode> parentNodes = new HashSet<>();
    private DAGNode leftNode = null;
    private DAGNode rightNode = null;
    private boolean exported = false;

    // 是否为expr节点设置Symbol
    private boolean assigned = false;

    public DAGNode(Operand operand) {
        this.operand = operand;
        this.exprOp = null;
    }

    public DAGNode(FourExpr.ExprOp exprOp, DAGNode leftNode) {
        this.operand = null;
        this.exprOp = exprOp;
        this.leftNode = leftNode;
        this.leftNode.addParent(this);
    }

    public DAGNode(FourExpr.ExprOp exprOp, DAGNode leftNode, DAGNode rightNode) {
        this.operand = null;
        this.exprOp = exprOp;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.leftNode.addParent(this);
        this.rightNode.addParent(this);
    }

    public FourExpr.ExprOp getExprOp() {
        return this.exprOp;
    }

    public DAGNode getLeftNode() {
        return this.leftNode;
    }

    public DAGNode getRightNode() {
        return this.rightNode;
    }

    public void addParent(DAGNode node) {
        this.parentNodes.add(node);
    }

    public boolean hasParent() {
        for (DAGNode parentNode : parentNodes) {
            if (!parentNode.isExported()) {
                return true;
            }
        }
        return false;
    }

    // public boolean isChild(DAGNode left, DAGNode right) {
    //     assert this.exprOp != null && !this.exprOp.isSingle();  // 双操作数
    //     if (this.exprOp.couldSwap()) {
    //         return (this.leftNode == left && this.rightNode == right) || (this.leftNode == right && this.rightNode == left);
    //     } else {
    //         return (this.leftNode == left && this.rightNode == right);
    //     }
    // }
    //
    // public boolean isChild(DAGNode left) {
    //     assert this.exprOp != null && this.exprOp.isSingle();  // 单操作数
    //     return this.leftNode == left;
    // }

    public void export() {
        this.exported = true;
    }

    public boolean isExported() {
        return this.exported;
    }

    public boolean isLeaf() {
        if (this.leftNode == null) {
            assert this.rightNode == null && this.exprOp == null;
            return true;
        }
        return false;
    }

    public Operand getOperand() {
        if (!(this.exprOp == null || this.assigned)) {
            return null;
        }
        // 要么是叶子节点，要么是已经被赋值的非叶子节点
        return this.operand;
    }

    public void setOperand(Operand operand) {
        assert this.exprOp != null;
        this.operand = operand;
        this.assigned = true;
    }
}

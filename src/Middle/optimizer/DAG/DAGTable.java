package Middle.optimizer.DAG;

import Middle.type.FourExpr;
import Middle.type.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DAGTable {
    private final HashMap<Operand, DAGNode> operandNodeMap = new HashMap<>();
    private final HashMap<DAGNode, HashSet<Operand>> nodeOperandMap = new HashMap<>();
    private final ArrayList<DAGNode> dagNodes = new ArrayList<>();

    public DAGNode getDAGNode(Operand operand) {
        if (!this.operandNodeMap.containsKey(operand)) {
            DAGNode node = new DAGNode(operand);
            operandNodeMap.put(operand, node);
            nodeOperandMap.put(node, new HashSet<>());
            nodeOperandMap.get(node).add(operand);
            dagNodes.add(node);
            return node;
        }
        return this.operandNodeMap.get(operand);
    }

    public void addDAGNode(FourExpr.ExprOp exprOp, DAGNode left, Operand res) {
        for (DAGNode dagNode : dagNodes) {
            if (dagNode.getExprOp() == exprOp && dagNode.getLeftNode() == left) {
                if (operandNodeMap.containsKey(res)) {
                    DAGNode formerNode = operandNodeMap.get(res);
                    nodeOperandMap.get(formerNode).remove(res);
                }
                operandNodeMap.put(res, dagNode);
                nodeOperandMap.get(dagNode).add(res);
                return;
            }
        }
        DAGNode dagNode = new DAGNode(exprOp, left);
        if (operandNodeMap.containsKey(res)) {  // 更新节点表
            DAGNode formerNode = operandNodeMap.get(res);
            nodeOperandMap.get(formerNode).remove(res);
        }
        operandNodeMap.put(res, dagNode);
        nodeOperandMap.put(dagNode, new HashSet<>());
        nodeOperandMap.get(dagNode).add(res);
        dagNodes.add(dagNode);
    }

    public void addDAGNode(FourExpr.ExprOp exprOp, DAGNode left, DAGNode right, Operand res) {
        for (DAGNode dagNode : dagNodes) {
            if (dagNode.getExprOp() == exprOp) {
                if (exprOp.couldSwap()) {
                    if ((dagNode.getLeftNode() == left && dagNode.getRightNode() == right) || (dagNode.getLeftNode() == right && dagNode.getRightNode() == left)) {
                        if (operandNodeMap.containsKey(res)) {
                            DAGNode formerNode = operandNodeMap.get(res);
                            nodeOperandMap.get(formerNode).remove(res);
                        }
                        operandNodeMap.put(res, dagNode);
                        nodeOperandMap.get(dagNode).add(res);
                        return;
                    }
                } else {
                    if (dagNode.getLeftNode() == left && dagNode.getRightNode() == right) {
                        if (operandNodeMap.containsKey(res)) {
                            DAGNode formerNode = operandNodeMap.get(res);
                            nodeOperandMap.get(formerNode).remove(res);
                        }
                        operandNodeMap.put(res, dagNode);
                        nodeOperandMap.get(dagNode).add(res);
                        return;
                    }
                }
            }
        }
        DAGNode dagNode = new DAGNode(exprOp, left, right);
        if (operandNodeMap.containsKey(res)) {
            DAGNode formerNode = operandNodeMap.get(res);
            nodeOperandMap.get(formerNode).remove(res);
        }
        operandNodeMap.put(res, dagNode);
        nodeOperandMap.put(dagNode, new HashSet<>());
        nodeOperandMap.get(dagNode).add(res);
        dagNodes.add(dagNode);
    }

    public ArrayList<DAGNode> getDagNodes() {
        return this.dagNodes;
    }

    public HashSet<Operand> getDAGNodeOperands(DAGNode dagNode) {
        return this.nodeOperandMap.get(dagNode);
    }
}

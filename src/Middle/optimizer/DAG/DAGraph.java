package Middle.optimizer.DAG;

import Frontend.Parser.expr.types.Exp;
import Frontend.Symbol.Symbol;
import Middle.optimizer.DAG.DAGNode;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.FourExpr;
import Middle.type.Operand;
import Middle.type.Pointer;

import java.util.ArrayList;
import java.util.Formattable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class DAGraph {
    // // 图中的节点
    // private final DAGNodes dagNodes = new DAGNodes();
    private final BasicBlock basicBlock;
    // 当前基本块的节点表
    private final DAGTable dagTable = new DAGTable();

    public DAGraph(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
        ArrayList<BlockNode> blockNodes = basicBlock.getContent();
        for (int i = 0; i < blockNodes.size(); i++) {
            if (!(blockNodes.get(i) instanceof FourExpr)) continue;
            FourExpr fourExpr = (FourExpr) blockNodes.get(i);
            if (fourExpr.isSingle()) {  // 单操作数
                DAGNode leftDAGNode = dagTable.getDAGNode(fourExpr.getLeft());
                dagTable.addDAGNode(fourExpr.getOp(), leftDAGNode, fourExpr.getRes());
            } else {  // 双操作数
                DAGNode leftDAGNode = dagTable.getDAGNode(fourExpr.getLeft());
                DAGNode rightDAGNode = dagTable.getDAGNode(fourExpr.getRight());
                dagTable.addDAGNode(fourExpr.getOp(), leftDAGNode, rightDAGNode, fourExpr.getRes());
            }
        }
    }

    public void dump() {
        Stack<DAGNode> stack = new Stack<>();
        ArrayList<DAGNode> dagNodes = new ArrayList<>(dagTable.getDagNodes());
        // DAG图重新导出代码
        while (dagNodes.size() != 0) {
            System.out.println(dagNodes.size());
            for (DAGNode dagNode : dagNodes) {
                // 没有父节点且不是叶子节点
                if (!dagNode.hasParent() && !dagNode.isLeaf()) {
                    stack.push(dagNode);
                    dagNode.export();
                    dagNodes.remove(dagNode);
                    DAGNode curr = dagNode.getLeftNode();
                    while (curr != null && !curr.hasParent()) {
                        stack.push(curr);
                        curr.export();
                        dagNodes.remove(curr);
                        curr = curr.getLeftNode();
                    }
                    break;
                }
            }
        }
        // 新的四元式
        HashMap<Symbol, ExprFactory> symbolExprFactoryMap = new HashMap<>();
        while (!stack.isEmpty()) {
            DAGNode top = stack.pop();
            if (top.isLeaf()) {
                continue;
            }
            // 操作符
            FourExpr.ExprOp exprOp = top.getExprOp();

            // 节点表中指向该节点的符号
            HashSet<Symbol> resSymbol = dagTable.getDAGNodeOperands(top).stream().map(t -> (Symbol) t)
                    .collect(Collectors.toCollection(HashSet::new));
            ExprFactory exprFactory = null;
            if (top.getExprOp().isSingle()) {
                exprFactory = new ExprFactory(top.getLeftNode(), null, top);
            } else {
                exprFactory = new ExprFactory(top.getLeftNode(), top.getRightNode(), top);
            }
            for (Symbol symbol : resSymbol) {
                assert !symbolExprFactoryMap.containsKey(symbol);
                symbolExprFactoryMap.put(symbol, exprFactory);
            }
        }

        ArrayList<BlockNode> blockNodes = basicBlock.getContent();
        ArrayList<BlockNode> newBlockNode = new ArrayList<>();
        for (BlockNode blockNode : blockNodes) {
            if (!(blockNode instanceof FourExpr)) {
                newBlockNode.add(blockNode);
                continue;
            }
            FourExpr fourExpr = (FourExpr) blockNode;
            Symbol res = fourExpr.getRes();
            if (symbolExprFactoryMap.containsKey(res)) {
                FourExpr newExpr = symbolExprFactoryMap.get(res).produce(res);
                if (newExpr == null) {
                    System.err.printf("DELETE: %s\n", fourExpr.toString());
                    continue;
                } else {
                    newBlockNode.add(newExpr);
                    System.err.printf("REPLACE %s WITH %s\n", fourExpr.toString(), newExpr.toString());
                }
            } else {
                newBlockNode.add(fourExpr);
            }
        }

        basicBlock.setContent(newBlockNode);
    }
}

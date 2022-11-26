package Middle.optimizer.DAG;

import Frontend.Parser.expr.types.Exp;
import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolType;
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

    private void preSearch(DAGNode dagNode, Stack<DAGNode> stack, ArrayList<DAGNode> dagNodes){
        stack.push(dagNode);
        dagNode.export();
        dagNodes.remove(dagNode);
        if(dagNode.getLeftNode() != null && !dagNode.getLeftNode().hasParent()){
            preSearch(dagNode.getLeftNode(), stack, dagNodes);
        }
        if(dagNode.getRightNode() != null && !dagNode.getRightNode().hasParent()){
            preSearch(dagNode.getRightNode(), stack, dagNodes);
        }
    }

    public void dump() {
        Stack<DAGNode> stack = new Stack<>();
        ArrayList<DAGNode> dagNodes = new ArrayList<>(dagTable.getDagNodes());
        // DAG图重新导出代码
        while (dagNodes.size() != 0) {
            // System.out.println(dagNodes.size());
            for (DAGNode dagNode : dagNodes) {
                // 没有父节点且不是叶子节点
                if (!dagNode.hasParent()) {
                    // stack.push(dagNode);
                    // dagNode.export();
                    // dagNodes.remove(dagNode);
                    preSearch(dagNode, stack, dagNodes);
                    // DAGNode curr = dagNode.getLeftNode();
                    // while (curr != null && !curr.hasParent()) {
                    //     stack.push(curr);
                    //     curr.export();
                    //     dagNodes.remove(curr);
                    //     curr = curr.getLeftNode();
                    // }
                    break;
                }
            }
        }
        ArrayList<BlockNode> newBlockNode = new ArrayList<>();

        // 新的四元式
        // HashMap<Symbol, FourExpr> symbolFourExprHashMap = new HashMap<>();
        HashMap<Symbol, ExprFactory> symbolExprFactoryMap = new HashMap<>();
        HashMap<Symbol, Symbol> symbolFirstSymbolMap = new HashMap<>();
        while (!stack.isEmpty()) {
            DAGNode top = stack.pop();
            if (top.isLeaf()) {
                continue;
            }
            // 操作符
            FourExpr.ExprOp exprOp = top.getExprOp();

            // 节点表中指向该节点的符号
            ArrayList<Symbol> resSymbol = dagTable.getDAGNodeOperands(top).stream().map(t -> (Symbol) t)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (resSymbol.isEmpty()) {
                resSymbol.add(Symbol.tempDAGSymbol());
            }
            ExprFactory exprFactory = null;
            if (top.getExprOp().isSingle()) {
                exprFactory = new ExprFactory(top.getLeftNode(), null, top);
            } else {
                exprFactory = new ExprFactory(top.getLeftNode(), top.getRightNode(), top);
            }
            Symbol firstSymbol = resSymbol.get(0);
            newBlockNode.add(exprFactory.produce(resSymbol.get(0)));
            for (Symbol symbol : resSymbol) {
                symbolExprFactoryMap.put(symbol, exprFactory);
                symbolFirstSymbolMap.put(symbol, firstSymbol);
            }
        }

        ArrayList<BlockNode> blockNodes = basicBlock.getContent();
        for (BlockNode blockNode : blockNodes) {
            if (!(blockNode instanceof FourExpr)) {
                newBlockNode.add(blockNode);
                continue;
            }
            FourExpr fourExpr = (FourExpr) blockNode;
            Symbol res = fourExpr.getRes();
            if (res == symbolFirstSymbolMap.get(res)) {
                continue;
            }
            if (symbolExprFactoryMap.containsKey(res)) {
                FourExpr newExpr = symbolExprFactoryMap.get(res).produce(res);
                assert newExpr != null;
                newBlockNode.add(newExpr);
                System.err.printf("REPLACE %s WITH %s\n", fourExpr.toString(), newExpr.toString());
            } else {
                newBlockNode.add(fourExpr);
            }
        }
        System.out.println(basicBlock.getContent().size());
        basicBlock.clearContent();
        for (BlockNode blockNode : newBlockNode) {
            basicBlock.addContent(blockNode);
        }
        System.out.println(basicBlock.getContent().size());
    }
}

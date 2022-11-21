package BackEnd.optimizer;

import Frontend.Symbol.Symbol;

import java.util.HashSet;
import java.util.stream.Collectors;

public class ConflictGraphNode implements Comparable<ConflictGraphNode> {
    private final Symbol symbol;
    // 无向边
    private final HashSet<ConflictGraphNode> conflictGraphNodes = new HashSet<>();

    // 记录着色过程中的连接边数目
    private int edgeCount;
    // 着色
    private int register = -1;
    // private boolean colored = false;

    public ConflictGraphNode(Symbol symbol) {
        this.symbol = symbol;
    }

    public void addConflictEdge(ConflictGraphNode conflictGraphNode) {
        this.conflictGraphNodes.add(conflictGraphNode);
    }

    public void initialCurrEdgeCount() {
        this.edgeCount = conflictGraphNodes.size();
    }

    public int getCurrEdgeCount() {
        return this.edgeCount;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    // 删除和anotherNode的连接边
    public void removeConnection(ConflictGraphNode anotherNode) {
        if (conflictGraphNodes.contains(anotherNode)) {
            edgeCount--;
        }
        assert edgeCount >= 0 : "连接边数不小于0";
    }

    public HashSet<Integer> getConflictRegister() {
        return conflictGraphNodes.stream().map(ConflictGraphNode::getRegister).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public int compareTo(ConflictGraphNode o) {
        return o.getCurrEdgeCount() - this.edgeCount;
    }

    @Override
    public String toString() {
        return this.symbol + ": " + this.edgeCount;
    }
}

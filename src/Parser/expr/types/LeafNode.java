package Parser.expr.types;

import Symbol.SymbolType;

// 表达式的叶子节点，包括LVal和Number
public interface LeafNode {
    SymbolType getSymbolType();

    int getDims();
}

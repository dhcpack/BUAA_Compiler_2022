package Frontend.Parser.expr.types;

import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

// 表达式的叶子节点，包括LVal,Number和funcExp  真正落到实处的叶子节点
public interface LeafNode {
    SymbolType getSymbolType();

    // Warning: for LVal, it is using Dim!!!
    // int a[1][2];
    // when using a[0], getDimCount() -> 1;
    int getDimCount();

    ArrayList<Integer> getDimSize();
}

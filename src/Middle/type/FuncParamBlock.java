package Middle.type;

import Frontend.Symbol.Symbol;

import java.util.HashSet;

public class FuncParamBlock extends BlockNode{
    public FuncParamBlock(HashSet<Symbol> defSet){
        this.setDefSet(defSet);
        this.setUseSet(new HashSet<>());
    }
}

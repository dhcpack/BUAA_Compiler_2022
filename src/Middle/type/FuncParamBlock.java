package Middle.type;

import Frontend.Symbol.Symbol;

import java.util.HashSet;
import java.util.StringJoiner;

public class FuncParamBlock extends BlockNode{
    public FuncParamBlock(HashSet<Symbol> defSet){
        this.setDefSet(defSet);
        this.setUseSet(new HashSet<>());
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(" ");
        stringJoiner.add("FuncParam");
        for (Symbol symbol:getDefSet()){
            stringJoiner.add(symbol.toString());
        }
        return stringJoiner.toString();
    }
}

package Middle.type;

import Config.SOPair;
import Frontend.Symbol.Symbol;

public class GetInt extends BlockNode {
    private final SOPair soPair;

    public GetInt(SOPair soPair) {
        this.soPair = soPair;
    }

    public Symbol getTarget(){
        if(soPair.isArray()){
            return soPair.getBase();
        } else {
            return (Symbol) soPair.getOffset();
        }
    }

    public boolean isArray(){
        return soPair.isArray();
    }

    public Operand getOffset(){
        return soPair.getOffset();
    }

    public Symbol getBase(){
        assert isArray();
        return soPair.getBase();
    }

    @Override
    public String toString() {
        if(soPair.isArray()){
            return "GETINT " + soPair.getOffset()+"[" +soPair.getBase()+"]";
        } else{
            return "GETINT "   + soPair.getOperand();
        }
    }
}

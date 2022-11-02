package Frontend.Parser.func.types;

public class MainFuncDef  {
    private final FuncDef funcDef;

    public MainFuncDef(FuncDef funcDef) {
        this.funcDef = funcDef;
    }

    public boolean missRightParenthesis() {
        return this.funcDef.missRightParenthesis();
    }

    public FuncDef getFuncDef() {
        return this.funcDef;
    }

    @Override
    public String toString() {
        return funcDef.printNormal(true) + "<MainFuncDef>";  // not print tag and not print <FuncType>
    }
}

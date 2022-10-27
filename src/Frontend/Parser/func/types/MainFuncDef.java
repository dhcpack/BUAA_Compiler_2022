package Frontend.Parser.func.types;

import Config.Reader;
import Config.Output;
import Config.SyntaxWriter;

public class MainFuncDef implements Output {
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
    public void output() {
        funcDef.printNormal(true);  // not print tag and not print <FuncType>
        SyntaxWriter.print("<MainFuncDef>");
    }
}

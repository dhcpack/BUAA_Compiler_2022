package Parser.decl.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

public class Def implements Output {
    // 常数定义 ConstDef → Var '=' ConstInitVal // 包含普通变 量、一维数组、二维数组共三种情况
    // 变量定义 VarDef → Var | Var '=' InitVal
    private final Var var;
    private final Token assign;
    private final InitVal initVal;
    private final boolean isConst;

    public Def(Var var, boolean isConst) {
        this.var = var;
        this.assign = null;
        this.initVal = null;
        this.isConst = isConst;
    }

    public Def(Var var, Token assign, InitVal initVal, boolean isConst) {
        this.var = var;
        this.initVal = initVal;
        this.assign = assign;
        this.isConst = isConst;
    }

    public boolean hasInitVal() {
        return this.initVal != null && this.assign != null;
    }

    @Override
    public void output() {
        var.output();
        if (hasInitVal()) {  // check have initial value or not
            IO.print(assign.toString());
            initVal.output();
        }
        if (isConst) {
            IO.print("<ConstDef>");
        } else {
            IO.print("<VarDef>");
        }
    }
}

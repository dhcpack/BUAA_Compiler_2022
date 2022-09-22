package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class FuncRParams implements Output {
    // 函数实参表 FuncRParams → Exp { ',' Exp } // 1.花括号内重复0次 2.花括号内重复多次 3. Exp需要覆盖数组传参和部分数组传参
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> seps;

    public FuncRParams(ArrayList<Exp> exps, ArrayList<Token> seps) {
        this.exps = exps;
        this.seps = seps;
    }

    @Override
    public void output() {
        int index = 0;
        exps.get(index++).output();

        for (Token sep:seps){
            IO.print(sep.toString());
            exps.get(index++).output();
        }
        IO.print("<FuncRParams>");
    }
}

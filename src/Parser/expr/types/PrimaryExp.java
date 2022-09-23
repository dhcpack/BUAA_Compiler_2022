package Parser.expr.types;

import Config.IO;

public class PrimaryExp implements UnaryExpInterface {
    // 基本表达式 PrimaryExp → BraceExp | LVal | Number
    private final PrimaryExpInterface primaryExpInterface;

    public PrimaryExp(PrimaryExpInterface primaryExpInterface) {
        this.primaryExpInterface = primaryExpInterface;
    }

    @Override
    public void output() {
        primaryExpInterface.output();
        IO.print("<PrimaryExp>");
    }
}

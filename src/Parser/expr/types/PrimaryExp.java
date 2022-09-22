package Parser.expr.types;

import Config.IO;

public class PrimaryExp implements UnaryExpInterface {
    private final PrimaryExpInterface primaryExpInterface;

    // 若是Exp，需要外加两个括号
    public PrimaryExp(PrimaryExpInterface primaryExpInterface) {
        this.primaryExpInterface = primaryExpInterface;
    }

    @Override
    public void output() {
        primaryExpInterface.output();
        IO.print("<PrimaryExp>");
    }
}

package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;

public class PrimaryExp implements UnaryExpInterface {
    // 基本表达式 PrimaryExp → BraceExp | LVal | Number
    private final PrimaryExpInterface primaryExpInterface;

    public PrimaryExp(PrimaryExpInterface primaryExpInterface) {
        this.primaryExpInterface = primaryExpInterface;
    }

    public int getLine() {
        return this.primaryExpInterface.getLine();
    }

    public PrimaryExpInterface getPrimaryExpInterface() {
        return primaryExpInterface;
    }

    @Override
    public void output() {
        primaryExpInterface.output();
        SyntaxWriter.print("<PrimaryExp>");
    }
}

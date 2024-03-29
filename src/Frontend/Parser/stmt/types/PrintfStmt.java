package Frontend.Parser.stmt.types;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.Exp;

import java.util.ArrayList;

public class PrintfStmt implements StmtInterface {
    // 'printf''('FormatString{','Exp}')'';'
    private final Token printf;
    private final Token left;
    private final Token formatString;
    private final ArrayList<Token> seps;
    private final ArrayList<Exp> exps;
    private final Token right;  // error check: right could be null

    public PrintfStmt(Token printf, Token left, Token formatString, ArrayList<Token> seps, ArrayList<Exp> exps,
                      Token right) {
        this.printf = printf;
        this.left = left;
        this.formatString = formatString;
        this.seps = seps;
        this.exps = exps;
        this.right = right;
    }

    public Token getPrintf() {
        return printf;
    }

    public Token getFormatString() {
        return formatString;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public boolean checkFormatString() {
        String content = formatString.getContent();
        if (content.length() < 2) {
            return false;
        }
        if ((int) content.charAt(0) != 34 || (int) content.charAt(content.length() - 1) != 34) {
            return false;
        }
        for (int i = 1; i < content.length() - 1; i++) {
            if (!((int) content.charAt(i) == 32 || (int) content.charAt(i) == 33 || (int) content.charAt(i) == 37
                    || 40 <= (int) content.charAt(i) && (int) content.charAt(i) <= 126)) {
                return false;
            }
            if (((int) content.charAt(i) == 92) && (i == content.length() - 1 || (int) content.charAt(i + 1) != 110)) {
                return false;  // check for \n
            }
            if (((int) content.charAt(i) == 37) && (i == content.length() - 1 || (int) content.charAt(i + 1) != 100)) {
                return false;  // check for %d
            }
        }
        return true;
    }

    public boolean checkCountMatch() {
        String[] s = formatString.getContent().split("%d");
        return s.length - 1 == exps.size();
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(printf).append(left).append(formatString);
        for (int i = 0; i < seps.size(); i++) {
            stringBuilder.append(seps.get(i)).append(exps.get(i));
        }
        stringBuilder.append(right);
        return stringBuilder.toString();
    }

    @Override
    public int getSemicolonLine() {
        return left.getLine();
    }
}

package Lexer;

public enum TokenType {
    MAINTK("main(?![a-zA-Z0-9_])"),
    CONSTTK("const(?![a-zA-Z0-9_])"),
    INTTK("int(?![a-zA-Z0-9_])"),
    BREAKTK("break(?![a-zA-Z0-9_])"),
    CONTINUETK("continue(?![a-zA-Z0-9_])"),
    IFTK("if(?![a-zA-Z0-9_])"),
    ELSETK("else(?![a-zA-Z0-9_])"),
    WHILETK("while(?![a-zA-Z0-9_])"),
    GETINTTK("getint(?![a-zA-Z0-9_])"),
    PRINTFTK("printf(?![a-zA-Z0-9_])"),
    RETURNTK("return(?![a-zA-Z0-9_])"),
    VOIDTK("void(?![a-zA-Z0-9_])"),

    IDENFR("[a-zA-Z_][a-zA-Z_0-9]*"),
    INTCON("[0-9]+"),
    STRCON("\".*?\""),

    AND("&&"),
    OR("\\|\\|"),
    LEQ("<="),
    GEQ(">="),
    EQL("=="),
    NEQ("!="),
    PLUS("\\+"),
    MINU("-"),
    MULT("\\*"),
    DIV("/"),
    MOD("%"),
    LSS("<"),
    GRE(">"),
    NOT("!"),
    ASSIGN("="),
    SEMICN(";"),
    COMMA(","),
    LPARENT("\\("),
    RPARENT("\\)"),
    LBRACK("\\["),
    RBRACK("]"),
    LBRACE("\\{"),
    RBRACE("}");

    private final String pattern;

    TokenType(String pattern) {
        this.pattern = "^" + pattern;
    }

    public String getPattern() {
        return pattern;
    }

}

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

// public enum Type {
//     // RESERVED KEYS
//     MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK, WHILETK,
//     GETINTTK, PRINTFTK, RETURNTK, VOIDTK,   12
//     // WORDS
//     IDENFR, INTCON, STRCON,
//     // SYMBOLS
//     SINGCOM, MULTCOM, AND, OR, PLUS, MINU, MULT, DIV, MOD, LEQ, GEQ, EQL, NEQ, NOT, LSS, GRE,
//     ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE, SPACE;  41
// }
public enum Type {
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

    // SINGCOM("//.*"),
    // MULTCOM("/\\*(.|\\n|\\r)*?\\*/"),
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

    Type(String pattern) {
        this.pattern = "^" + pattern;
    }

    public String getPattern() {
        return pattern;
    }

    // public static final HashMap<Type, Pattern> allPattern = new HashMap<Type, Pattern>(){{
    //     for (Type t : Type.values()) {
    //         put(t, Pattern.compile("^" + t.getPattern()));
    //     }
    // }};

    // public static HashMap<String, Pattern> getAllPattern() {
    //     for (Type t : Type.values()) {
    //         allPattern.put(t.toString(), Pattern.compile("^" + t.getPattern()));
    //     }
    //     return allPattern;
    // }

    // private static final String allString = Arrays.stream(Type.values())
    //         .map(t -> "(?<" + t.toString() + ">" + t.getPattern() + ")")
    //         .reduce((s1, s2) -> s1 + "|" + s2).toString();
    // // assert allString.isPresent();
    // public static final Pattern allPattern = Pattern.compile(allString);
}
// public enum Type {
//     MAINTK("MAINTK", "main"),
//     CONSTTK("CONSTTK", "const"),
//     INTTK("INTTK", "int"),
//     BREAKTK("BREAKTK", "break"),
//     CONTINUETK("CONTINUETK", "continue"),
//     IFTK("IFTK", "if"),
//     ELSETK("ELSETK", "else"),
//     WHILETK("WHILETK", "while"),
//     GETINTTK("GETINTTK", "getint"),
//     PRINTFTK("PRINTFTK", "printf"),
//     RETURNTK("RETURNTK", "return"),
//     VOIDTK("VOIDTK", "void"),
//
//     IDENFR("IDENFR", "^[_A-Za-z][_A-Za-z0-9]*"),
//     INTCON("INTCON", "[0-9]+"),
//     STRCON("STRCON", "\\\"[^\\\"]*\\\""),
//
//     AND("AND", "&&"),
//     OR("OR", "\\|\\|"),
//     LEQ("LEQ", "<="),
//     GEQ("GEQ", ">="),
//     EQL("EQL", "=="),
//     NEQ("NEQ", "!="),
//
//     PLUS("PLUS", "\\+"),
//     MINU("MINU", "-"),
//     MULT("MULT", "\\*"),
//     DIV("DIV", "/"),
//     MOD("MOD", "%"),
//     LSS("LSS", "<"),
//     GRE("GRE", ">"),
//     NOT("NOT", "!"),
//     ASSIGN("ASSIGN", "="),
//     SEMICN("SEMICN", ";"),
//     COMMA("COMMA", ","),
//     LPARENT("LPARENT", "\\("),
//     RPARENT("RPARENT", "\\)"),
//     LBRACK("LBRACK", "\\["),
//     RBRACK("RBRACK", "]"),
//     LBRACE("LBRACE", "\\{"),
//     RBRACE("RBRACE", "}");
//
//     private final String name;
//     private final Pattern pattern;
//
//     // Type(String pattern) {
//     //     this.pattern = Pattern.compile(pattern);
//     // }
//
//     Type(String name, String pattern) {
//         this.name = name;
//         this.pattern = Pattern.compile(pattern);
//     }
//
//     public String getName() {
//         System.out.println(LBRACE.toString());
//         return this.name;
//     }
//
//     public Pattern getPattern() {
//         return pattern;
//     }
// }

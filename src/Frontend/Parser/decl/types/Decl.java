package Frontend.Parser.decl.types;

import Frontend.Lexer.Token;
import Frontend.Parser.stmt.types.BlockItem;

import java.util.ArrayList;

public class Decl implements BlockItem{
    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    // 变量声明 VarDecl → BType VarDef { ',' VarDef } ';'
    private final Token constToken;
    private final Token BType;
    private final Def def;
    private final ArrayList<Token> separators;
    private final ArrayList<Def> defs;
    private final Token semicolon;  // error check: semicn could be null

    public Decl(Token constToken, Token Btype, Def def, ArrayList<Token> separators, ArrayList<Def> defs,
                Token semicolon) {
        this.constToken = constToken;
        this.BType = Btype;
        this.def = def;
        this.separators = separators;
        this.defs = defs;
        this.semicolon = semicolon;
    }

    public Token getConstToken() {
        return constToken;
    }

    public Token getBType() {
        return BType;
    }

    public ArrayList<Token> getSeparators() {
        return separators;
    }

    public Token getSemicolon() {
        return semicolon;
    }

    public int getLine() {
        return this.BType.getLine();
    }

    public boolean missSemicolon() {
        return this.semicolon == null;
    }

    public boolean isConst() {
        return this.constToken != null;
    }

    public Def getDef() {
        return def;
    }

    public ArrayList<Def> getDefs() {
        return defs;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (isConst()){  // print const
            res.append(constToken.toString());
        }
        res.append(BType.toString());  // print BType
        res.append(def);  // print def
        for (int i = 0; i < separators.size(); i++) {  // print sep and def
            res.append(separators.get(i).toString()).append(defs.get(i));
        }
        if (this.semicolon != null) {
            res.append(semicolon); // print ;
        }
        if (isConst()) {
            res.append("<ConstDecl>\n");
        } else {
            res.append("<VarDecl>\n");
        }
        return res.toString();
    }
}

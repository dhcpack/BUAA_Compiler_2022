package Frontend.Parser.decl.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;
import Frontend.Parser.stmt.types.BlockItem;

import java.util.ArrayList;

public class Decl implements BlockItem, Output {
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
    public void output() {
        if (isConst()) SyntaxWriter.print(constToken.toString());  // print const
        SyntaxWriter.print(BType.toString());  // print BType
        def.output();  // print def
        for (int i = 0; i < separators.size(); i++) {  // print sep and def
            SyntaxWriter.print(separators.get(i).toString());
            defs.get(i).output();
        }
        if (this.semicolon != null) {
            SyntaxWriter.print(semicolon.toString());  // print ;
        }
        if (isConst()) {
            SyntaxWriter.print("<ConstDecl>");
        } else {
            SyntaxWriter.print("<VarDecl>");
        }
    }
}

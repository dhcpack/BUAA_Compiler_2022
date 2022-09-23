package Parser.stmt.types;

import Config.IO;
import Lexer.Token;

public class Stmt implements BlockItem {
    // Stmt --> StmtInterface ';'
    private final StmtInterface stmt;
    private final Token semicn;

    public Stmt(StmtInterface stmt, Token semicn) {
        this.stmt = stmt;
        this.semicn = semicn;
    }

    @Override
    public void output() {
        if(stmt!=null){  // if stmt not null then print stmt
            stmt.output();
        }
        if(semicn!=null){  // if has semicn then print semicn
            IO.print(semicn.toString());
        }
        IO.print("<Stmt>");
    }
}

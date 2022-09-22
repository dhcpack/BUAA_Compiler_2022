package Parser.stmt.types;

import Config.IO;
import Lexer.Token;

public class Stmt implements BlockItem {
    private final StmtInterface stmt;
    private final Token semicn;

    public Stmt(StmtInterface stmt, Token semicn) {
        this.stmt = stmt;
        this.semicn = semicn;
    }

    @Override
    public void output() {
        if(stmt!=null){
            stmt.output();
        }
        if(semicn!=null){
            IO.print(semicn.toString());
        }
        IO.print("<Stmt>");
    }
}

```
    // statement
    // semicn is stored in Stmt
    
    语句块 Block → '{' { BlockItem } '}'
    语句块项 BlockItem → Decl | Stmt
    语句 Stmt --> StmtInterface ';'
    
    StmtInterface --> 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                    | 'while' '(' Cond ')' Stmt 
                    | 'break' ';' | 'continue' ';' 
                    | 'return' [Exp] ';' 
                    | 'printf''('FormatString{','Exp}')'';'
                    | Block 
                    | LVal '=' 'getint''('')'';'
                    | LVal '=' Exp ';'
                    | [Exp] ';'
```
```
    // statement
    语句块 Block → '{' { BlockItem } '}' // 1.花括号内重复0次 2.花括号内重复多次 
    语句块项 BlockItem → Decl | Stmt // 覆盖两种语句块项
    语句 Stmt → 
              'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else 
            | 'while' '(' Cond ')' Stmt 
            | 'break' ';' | 'continue' ';' 
            | 'return' [Exp] ';' // 1.有Exp 2.无Exp  
            | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp 
            | Block 
            | LVal '=' 'getint''('')'';'
            | LVal '=' Exp ';' // 每种类型的语句都要覆
            | [Exp] ';' //有无Exp两种情况
```
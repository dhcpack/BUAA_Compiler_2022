```
    // declare
    // semicn is stored in Decl
    
    声明 Decl → ConstDecl | VarDecl
    变量声明 VarDecl → 'int' VarDef { ',' VarDef } ';'
    常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    
    常数定义 ConstDef → Var '=' ConstInitVal
    变量定义 VarDef → Var | Var '=' InitVal 
    
    常量变量 Var -> Ident { '[' ConstExp ']' }
    
    常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' 
    变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
```

```
    // expressions
    左值表达式 LVal → Ident {'[' Exp ']'}
    数值 Number → IntConst
       
    一元表达式 UnaryExp → PrimaryExp | FuncExp | UnaryOp UnaryExp
    单目运算符 UnaryOp → '+' | '−' | '!'
    带括号的表达式 BraceExp -> '(' Exp ')'
    基本表达式 PrimaryExp → BraceExp | LVal | Number
    
    函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
    函数实参表 FuncRParams → Exp { ',' Exp }
     
    表达式 Exp --> AddExp
    常量表达式 const Exp --> AddExp
    条件表达式 Cond → LOrExp
    乘除模表达式 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
    加减表达式 AddExp → MulExp {('+' | '−') MulExp}
    关系表达式 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp} 
    相等性表达式 EqExp → RelExp {('==' | '!=') RelExp}
    逻辑与表达式 LAndExp → EqExp {'&&' EqExp}
    逻辑或表达式 LOrExp → LAndExp {'||' LAndExp}
```

```
    // functions
    函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block
    
    函数类型 FuncType → 'void' | 'int'
    
    函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
    函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
```

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

```
    // CompUnit
    编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef
```
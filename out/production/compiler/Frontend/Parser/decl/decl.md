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
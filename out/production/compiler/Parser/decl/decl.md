```
    // declare
    声明 Decl → ConstDecl | VarDecl // 覆盖两种声明 
    常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    常数定义 ConstDef → Var '=' ConstInitVal // 包含普通变 量、一维数组、二维数组共三种情况
    基本类型 BType → 'int' // 存在即可  
    常量变量 Var -> Ident { '[' ConstExp ']' }
    变量声明 VarDecl → BType VarDef { ',' VarDef } ';'
    变量定义 VarDef → Var | Var '=' InitVal 
    常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' 
    变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'   // const or not
```
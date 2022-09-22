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

```
    // expressions
    左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    数值 Number → IntConst // 存在即可 
       
    一元表达式 UnaryExp → {UnaryOp} PrimaryExp | FuncExp // 存在即可 
    单目运算符 UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中
    基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖 
    函数调用   FuncExp -> Ident '(' {Exp} ')'
     
    // Exp、const Exp --> AddExp
    条件表达式 Cond → LOrExp // 存在即可 
    乘除模表达式 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp} // 1.UnaryExp 2.* 3./ 4.% 均需覆盖 
    加减表达式 AddExp → MulExp {('+' | '−') MulExp} // 1.MulExp 2.+ 需覆盖 3.- 需覆盖 
    关系表达式 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp} // 1.AddExp 2.< 3.> 4.<= 5.>= 均需覆盖 
    相等性表达式 EqExp → RelExp {('==' | '!=') RelExp} // 1.RelExp 2.== 3.!= 均 需覆盖 
    逻辑与表达式 LAndExp → EqExp {'&&' EqExp} // 1.EqExp 2.&& 均需覆盖 
    逻辑或表达式 LOrExp → LAndExp {'||' LAndExp} // 1.LAndExp 2.|| 均需覆盖 
```

```
    // functions
    函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参 
    主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数 
    函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数 
    函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号 内重复多次 
    函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量 2.一维数组变量 3.二维数组变量 
```

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
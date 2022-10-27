```
    // functions
    函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block
    
    函数类型 FuncType → 'void' | 'int'
    
    函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
    函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
```
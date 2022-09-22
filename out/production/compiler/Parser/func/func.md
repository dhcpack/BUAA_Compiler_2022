```
    // functions
    函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参 
    主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数 
    函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数 
    函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号 内重复多次 
    函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量 2.一维数组变量 3.二维数组变量 
```
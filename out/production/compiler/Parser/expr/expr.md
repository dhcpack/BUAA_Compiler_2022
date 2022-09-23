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
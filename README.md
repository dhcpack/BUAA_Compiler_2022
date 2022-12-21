[TOC]





一. 参考编译器介绍：总结所阅读的编译器的总体结构、接口设计、文件组织等内容
二. 编译器总体设计：介绍自己的将要实现的编译器的总体结构、接口设计、文件组织等内容
三. 词法分析设计：编码前的设计、编码完成之后的修改
四. 语法分析设计：编码前的设计、编码完成之后的修改
五. 错误处理设计：编码前的设计、编码完成之后的修改
六. 代码生成设计：编码前的设计、编码完成之后的修改
七. 代码优化设计:  编码前的设计、编码完成之后的修改，未选择MIPS代码生成的同学无需完成此项内容





sge 四句
slti 一句

beq

beqz

## 一、参考编译器介绍





## 二、编译器总体设计
本编译器由Java语言编写，能将SysY语言翻译成mips汇编语言。主线上对输入程序扫描四遍，流程图如下：
![总体架构](.\img\总体架构.jpg)

### 第一遍：词法分析

​	功能：读入字符串格式的输入程序，进行词法分析，将分析结果保存到ArrayList\<Token>中

​	该部分文件在Frontend.Lexer包中，代码逻辑比较简单，包含的文件有：

	- InputHandler.java: 输入串指针。封装输入串，维护一个输入串指针，向外提供多种指针移动和字符串读取方法；
	- TokenType.java: 单词枚举类，用于保存单词类别码和对应的正则表达式；
	- Lexer.java: 词法分析器，提供一个静态函数lex，进行正则匹配和保存结果。

​	词法分析结束后，得到单词数组，传入到下一阶段进一步处理。

### 第二遍：语法分析

​	功能：读入单词数组，进行语法分析，建立语法树。

​	该部分文件在Frontend.Parser包中，此处的关键是恰当改写文法为LL(1)，消除左递归和避免回溯，保证递归下降分析法的可行性，文件结构如下：

```c
- Parser
    - decl
    	- types
    		decltypes
    	- DeclParser.java
    - expr
    	- types
    		exprtypes
    	- ExprParser.java
    - func
    	- types
    		functypes
    	- FuncParser.java
    - stmt
    	- types
    		stmttypes
    	- StmtParser.java
    - CompUnit.java
    - Parser.java
    - TokenHandler.java
```

​	这里将文法分为四个大类，decl、expr、func、stmt，type包下的每个文件包含一个对文法非终结符建模的类，每一类中都有一个独立的parser，采用递归下降的方法对本类中的文法进行语法分析。最外层有一个总的parser，对四个文法类进行汇总，最终结果会保存在CompUnit类中，传递给下一遍进行后续处理。另外，TokenHandler和第一遍中的InputHandler的意义相似，封装输入序列并维护一个指针，向外提供多种指针移动和单词读取方法。



### 第三遍：语义分析、中间代码生成（符号表建立、错误处理）

​	功能：进行语义分析，和中间代码生成。建立符号表和进行错误处理也在该遍中完成。

​	该遍任务较多，功能复杂，任务之间耦合性相对较大，代码书写比较困难，是编译器构建过程中最关键的一部分，文件结构如下：

```c
- Exceptions
    allExceptions
- Frontend
    - Symbol
    	- Errors.java
    	- Symbol.java
    	- SymbolTable.java
    	- SymbolType.java
    - Util
    	- ConstExpCalculator.java
    - SymbolTableBuilder.java
- Middle
    - optimizer
    	- DefUseCalcUtil.java
    	- DeleteUselessMiddleCode.java
    	- ExtractCommonExpr.java
    	- MergeBranch.java
    - type
    	allMiddleTypes
    - MiddleCode.java
```

​	文件树中的allExceptions、allMiddleTypes分别是对错误和中间代码中元素进行抽象建模的类，为了简化文件树没有一一列出。该部分的主程序在SymbolTable.java文件中，仍然采用递归下降的方法检查和翻译各个语法结构，最终的翻译结果会保存到MiddleCode.java中。ConstExpCalculator.java的作用是进行常量表达式的计算，主要是计算常量的初始值，全局数组的初始值以及数组维数等，MiddleCode依次经过optimizer中的三步针对中间代码的优化，优化结果传递到下一遍中进一步处理。

### 第四遍：目标代码生成

​	功能：翻译中间代码，掌管和分配寄存器。

​	相较于第三遍，该遍任务单一，目的性明确，完成代码的编写相对容易。但要想生成更高质量的汇编代码，需要进一步花心思思考恰当的寄存器分配策略和代码合并简化策略，这部分花费的时间是最多的。这部分的文件结构如下：

```c
- BackEnd
    - instructions
    	allmipsinstrustions
    - optimizer
    	- ConflictGraph.java
    	- ConflictGraphNode.java
    	- DeleteUselessJump.java
    	- DeleteUselessMips.java
    	- OptimizeBranch.java
    - MipsCode.java
    - Register.java
    - Translator.java
```

​	文件树中的allmipsinstructions代表对mips汇编指令建模描绘的类，为了简化文件树没有详细列出。翻译中间代码为mips指令的主程序在Translator.java中，接收上一遍传入的MiddleCode作为输入，在Register.java中临时寄存器分配策略（OPT）和ConflictGraph.java中全局寄存器分配策略（图着色）的指导下进行寄存器分配和目标代码生成。最终得到的汇编代码MipsCode依次经过optimizer中的三步针对目标代码的优化，输出到mips.txt中




## 三、词法分析设计

​	词法分析程序在Frontend.Lexer包中。写此部分的代码时，理论课上讲了一种基于自动机的词法分析方法，通过学长的博客又了解到了通过正则匹配进行词法分析这种方法，权衡实现难度，我最终选择了使用正则表达式进行单词匹配。该部分逻辑简单，任务和意图明确，编码前后的设计方案没有做修改，具体设计细节如下：

​	为了便于确定符号所处的行数，输入符号串被按行存储保存在InputHandler中，它同时维护了一个字符串指针，提供了以下的方法，为后续的正则匹配带来了极大的便利。

```java
public boolean moveForward(int step) {}  // 字符指针后移step位
public String getForwardWord(int step) {}  // 返回后面step个字符
public String getCurrentLine() {}  // 返回当前行剩下的字符
public boolean reachEnd() {}  // 到达末尾
public boolean skipBlanks() {}  // 跳过空格
public boolean skipComments() {}  // 跳过注释
public int getLine() {}  // 返回当前行数
```

​	TokenType中定义了每个单词和相应的正则表达式，这里需要注意的是，对于保留字，需要正好匹配，否则需要识别成标识符IDENFR，这里我们需要使用正则表达式中的**负向先行断言**。正则匹配的顺序采用保留字-->标识符-->运算符分隔符等。

​	完成以上的准备工作，词法分析程序就水到渠成了，Lex主函数的实现如下：

```java
public class Lexer {
    public static TokenList lex(InputHandler inputHandler) {
        TokenList tokenList = new TokenList();
        while (!inputHandler.reachEnd()) {
            // skip blanks
            if (inputHandler.skipBlanks()) break;
            // skip comments
            if (inputHandler.skipComments()) continue;
            // traverse all patterns and make regex match
            for (TokenType tokenType : TokenType.values()) {
                Matcher matcher = Pattern.compile(tokenType.getPattern()).matcher(inputHandler.getCurrentLine());
                if (matcher.find()) {
                    tokenList.add(new Token(tokenType, matcher.group(0), inputHandler.getLineNumber()));
                    inputHandler.moveForward(matcher.group(0).length());
                    break;
                }
            }
        }
        return tokenList;
    }
}
```







## 四、语法分析设计

​	语法分析设计时采用递归下降的方法建立语法树，程序在Parser包中，语法成分共分为四大部分`decl, expr, func, stmt`，在四种语法成分中，首先构建

## 五、 错误处理设计
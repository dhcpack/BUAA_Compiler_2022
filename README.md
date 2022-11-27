一. 参考编译器介绍：总结所阅读的编译器的总体结构、接口设计、文件组织等内容
二. 编译器总体设计：介绍自己的将要实现的编译器的总体结构、接口设计、文件组织等内容
三. 词法分析设计：编码前的设计、编码完成之后的修改
四. 语法分析设计：编码前的设计、编码完成之后的修改
五. 错误处理设计：编码前的设计、编码完成之后的修改
六. 代码生成设计：编码前的设计、编码完成之后的修改
七. 代码优化设计:  编码前的设计、编码完成之后的修改，未选择MIPS代码生成的同学无需完成此项内容

# 一、参考编译器介绍

# 二、编译器总体设计

# 三、词法分析设计

​	词法分析程序在Lexer包中，主要包括InputHandler类（封装读入的字符串，提供多种读取功能），Lexer类（词法分析主函数），Token类（单词模型），TokenList类（保存解析出来的单词）以及TokenType类（枚举类，枚举单词类型，并定义每个单词的正则表达式）。

​	编译器的输入输出统一在Config.IO类中实现，逐行读取字符串后，将其传入InputHandler中。InputHandler维护一个字符指针，并向外提供了以下方法，方便对字符串的处理：

```java
public boolean moveForward(int step) {}  // 字符指针后移
public String getForwardWord(int step) {}  // 返回后面step个字符
public String getCurrentLine() {}  // 返回当前行剩下的字符
public boolean reachEnd() {}  // 到达末尾
public boolean skipBlanks() {}  // 跳过空格
public boolean skipComments() {}  // 跳过注释
```

​	TokenType类中定义了每个单词类型对应的正则表达式，需要特殊注意的是，对于int、main等保留字单词，需要继续向后检查后面的字符是否是数字、字母或下划线，如果是的话，则它们共同构成一个普通token而不再是保留字，这里可以采用正则匹配中的negative look ahead方法（例如`main(?![a-zA-Z0-9_])`）。

​	Lexer类定义了词法分析的主函数，函数结构如下

```java
while (!inputHandler.reachEnd()) {
    if (inputHandler.skipBlanks()) break;  // skip blanks
    if (inputHandler.skipComments()) continue;  // skip comments
    
    // traverse all patterns and make regex match
    for (TokenType tokenType : TokenType.values()) {
        // make match between (inputHandler.getCurrentLine, TokenType.getRegex);
        // add to TokenList
    }
}
```

# 四、语法分析设计

​	语法分析设计时采用递归下降的方法建立语法树，程序在Parser包中，语法成分共分为四大部分`decl, expr, func, stmt`，在四种语法成分中，首先构建

# 五. 错误处理设计
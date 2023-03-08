# BUAA_Compiler_2022

### 北京航空航天大学2022秋编译技术课程设计

C语言子集SysY ==> Mips

----

<center><font size=5>编译技术申优文档</font></center>


----------

[TOC]

-----

## 写在前面

​	本文是2022年秋编译技术课程的申优文档，内容是对词法分析、语法分析、错误处理和符号表设计以及代码生成过程中的关键点做简要的说明和记录，并对我在代码优化和竞速过程中采取以及尝试的优化策略做详细的介绍和说明。

​	编译器的整体架构：

![总体架构](https://github.com/mooneater1021/BUAA_Compiler_2022/blob/main/img/%E6%80%BB%E4%BD%93%E6%9E%B6%E6%9E%84.jpg)



## 词法分析

​	词法分析的总体策略时字符串扫描+正则匹配。为了方便读取输入的程序字符串，我新建了一个类InputHandler，并在该类中维护一个字符串指针，提供了一下的读取字符串方法：

```java
public boolean moveForward(int step) {}  // 字符指针后移step位
public String getForwardWord(int step) {}  // 返回后面step个字符
public String getCurrentLine() {}  // 返回当前行剩下的字符
public boolean reachEnd() {}  // 到达末尾
public boolean skipBlanks() {}  // 跳过空格
public boolean skipComments() {}  // 跳过注释
public int getLine() {}  // 返回当前行数
```

### 难点分析

​	正则表达式书写时，关键之处在于对于关键字，必须完全地与正则表达式匹配，其下一个字符不可以是数字字母和下划线，这里可以采用正则表达式中的**负向先行断言**。此外**正则匹配的顺序**也需要额外注意，一是保留字需要先于标识符被匹配；二是对于<=，>=，==等前缀包含其它符号的符号，需要先于包含的符号被匹配。

## 语法分析

​	语法分析采用递归下降的方法编写，首先需要改写文法消除文法中的左递归：

```
// 原文法
AddExp := MulExp | AddExp ('+' | '-') MulExp
// 改写后的文法
AddExp → MulExp {('+' | '−') MulExp}
```

​	之后可以使用递归下降的算法进行分析：

```java
public AddExp parseAddExp() {
    MulExp mulExp = parseMulExp();
    ArrayList<MulExp> exps = new ArrayList<>();
    ArrayList<Token> seps = new ArrayList<>();
    Token token = tokenHandler.getForwardToken();
    while (token.getType() == TokenType.PLUS || token.getType() == TokenType.MINU) {
        seps.add(tokenHandler.getTokenAndMove());
        exps.add(parseMulExp());
        token = tokenHandler.getForwardToken();  // refresh token
    }
    return new AddExp(mulExp, exps, seps);
}
```

​	最终的分析结果被保存在顶层语法成分`<CompUnit>`中，传递给下一级进行分析。

### 难点分析

​	语法分析的难点在于改写文法，以及如何让自己改写的文法输出与评测机一致的结果。

​	改写文法可以采用课上所讲的算法，由于留该项作业时理论课进度正好也讲到这一知识点，因此难度并不大。一个重要的问题是如何**在调整完文法后，仍旧输出评测机要求的结果**。

​	举例来说，对于文法`AddExp := MulExp | AddExp (′+′ | ′−′) MulExp`，无法直接使用递归下降的方法进行语法分析，如果对其进行改写的话，就可能导致输出语法成分的顺序发生改变。

```
对于表达式1 + 1，
使用原文法分析的结果：<MulExp><AddExp><MulExp><AddExp>

修改为右递归 ==> AddExp := MulExp | MulExp ('+' | '-') AddExp
此时分析结果为：<MulExp><MulExp><AddExp><AddExp>

使用扩充的BNF表示改写文法 ==> AddExp := MulExp {('+' | '−') MulExp}
此时分析结果为：<MulExp><MulExp><AddExp>
```

​	这一问题虽然不影响分析结果，但是无法通过评测，因此注意需要在适当位置加入额外的输出语句，保证语法成分解析正确。



## 错误处理和符号表管理

​	在错误处理编码的过程中，可以将错误分为语法错误和语义错误。在具体实现时，可以在不同的位置处理这两种错误。对于**语法错误**，他们是在程序中处于独立的状态，不需要建立符号表，就能够完全判断出他们的出错地点，因此考虑可以在**语法分析部分**直接检测出错误；另一种错误如b（名字重定义），c（未定义的名字），d（函数参数个数不匹配）等**语义错误**，其涉及到对整个程序的把握，即需要建立符号表，需要**在遍历语法树、创建符号表的基础之上做错误处理检查**。

​	好的符号表设计，保存了充足的信息可以为接下来的语义分析和代码生成带来很多的便利，我在设计符号表时，采用了树形结构的栈式符号表，每个符号表存在到父符号表的一个指针，符号表的定义以及保存的符号信息如下：

```java
public class SymbolTable {
    private final SymbolTable parent;
    private final HashSet<SymbolTable> childSymbolTables = new HashSet<>();
    private final HashMap<String, Symbol> symbols = new HashMap<>();
    private final SymbolTable funcSymbolTable;  // 当前符号表所属的funcSymbolTable
    private int stackSize = 0;
}

public class Symbol implements LeafNode, Operand, Cloneable {
    public enum Scope {
        GLOBAL,
        LOCAL,
        TEMP,
        PARAM,  // 只记录函数形参中的array类型变量
    }

    // for all
    private String name;
    private final SymbolType symbolType;
    private Token ident;
    private final boolean isConst;
    private Scope scope;
    private int size;
    private int address;
    private boolean hasAddress = false;

    // int
    private int constInitInt;
    private boolean hasConstInitInt = false;

    // 数组
    private final ArrayList<Integer> dimSize;
    private ArrayList<Integer> constInitArray;
    private boolean hasConstInitArray = false;
    private final int dimCount;

    // 函数
    private final ArrayList<Symbol> params;
    private final SymbolType returnType;

    // 是否是内联变量
    private boolean inlineVariable = false;
    private FuncBlock outerFunc = null;
    private FuncBlock inlineFunc = null;
    private int funcInliningIndex = 0;
}
```

​	这些信息包含了在代码生成和代码优化过程中需要的一切相关信息，是程序运行过程中产生的一切符号的“身份证件”，有力地支撑后续的翻译工作。



## 代码生成

### 中间代码生成部分

​	这部分的实现是在遍历语法树的同时生成中间代码，关键之处在于中间代码的设计和控制流的翻译过程。一般来说，程序的语言结构主要分为顺序结构，选择结构和控制结构。对于顺序结构，我选择用**四元式**进行表达，对于控制结构和循环结构，我选择**跳转语句**和**跳转标签**进行翻译。中间代码结构举例如下：

```java
// 顺序结构：四元式表达
a = b + c
ADD B C A

// 条件控制结构：跳转+标签
if(a || b){
    print(1);
}

IF_BEGIN:
    BNEZ a 成立
    BNEZ b 成立
    J 不成立
成立：
	PRINT 1
	J 不成立
不成立：

// 循环控制结构：跳转+标签
while(a != 5){
    print(1);
}

WHILE_CHECK:
	BNE a 5 WHILE_END
    j WHILE_BODY
WHILE_BODY:
  	PRINT 1
    J WHILE_CHECK
WHILE_END:
```

#### 难点分析

​	这部分的一个难点在于如何安排基本块以及如何设置跳转指令的跳转目标。我的设计是在中间代码翻译类中维护一个全局基本块变量`currBlock`，一切顺序程序的翻译结果都将被接到`currBlock`上，而与跳转相关的结构，如if、While等语句的翻译，则是通过更改当前基本块`currBlock`来改变其它顺序指令所加入的基本块，并通过在基本块中加入跳转语句，来影响和改变语句的执行过程，进而达到跳转的效果。

### 目标代码生成部分

​	形式上将中间代码翻译成目标代码其实是难度不大的，因为中间代码中设计的四元式序列以及跳转语句在汇编代码中都有相应的代替选择。目标代码的生成程序在BackEnd.Translator.java中，仍然采用类似于递归下降的算法，逐层地翻译各个中间代码对象。

#### 难点分析

​	首先，第一个难点是如何恰当**使用和分配存储空间**。在变量的存储空间的申请和分配方面，对于全局变量、常量、全局数组以及字符串常量，这些信息是对全局可见的，因此将它们存放在data区，并将$gp寄存器赋值为全局区的基地址。其它变量存放在text区。在函数内存空间的申请和分配方面，在调用某个函数时，首先记录该函数的栈指针地址，传参的方式时将参数保存在被调用函数栈指针前几个位置，对于数组参数则将数组的地址传给调用的函数。进入函数时，会相应地从栈地址前几个位置来取调用参数。

![空间分配](..\compiler\img\空间分配.jpg)

​	第二个难点时采用什么样的**寄存器分配策略**，以及在什么时候将寄存器中的数据**存回内存**。在代码优化前，我才用了相对比较简单的LRU寄存器分配策略。在Register类中维护一个队列，队列从前到后的顺序表示寄存器最晚到最近被使用。在正常顺序翻译的基本块内，如果寄存器不足，则将队列列首的寄存器弹出，对应的变量存进内存，并将该寄存器分配给即将要使用的变量。

​	对于何时将寄存器的值重新存到内存中，由于我最初没有编写图着色寄存器分配算法，所有寄存器和变量均不是一直处于绑定状态，因此需要将寄存器存回内存的地方主要有三处：

 	1. 当寄存器被LRU时需要将寄存器存回内存
 	2. 当翻译到跳转语句时，需要将所有寄存器存回内存
 	3. 当翻译到函数调用语句时，需要将所有寄存器存回内存



## 代码优化

### 中间代码优化

#### 死代码删除

​	死代码删除需要建立在**活跃变量分析**的基础之上，在按照编译原理书上讲解的算法进行活跃变量分析后，判断每条指令的运算结果是否活跃（在该条指令的out集合中），如果指令的结果不活跃，意味着当前指令属于死代码，可以删除。需要注意的是，对于getInt指令，不能简单删除整条指令，仍需要在指令所在位置加入读取输入的代码占位，以防输入信息读取错误。



#### 常量传播

​	在代码中，有两类符号可以看作是常量。其一是**本身在定义时就以`const`关键字修饰的符号**，一定是常量，可以在编译器进行计算和替换；其二是**在整个代码运行阶段都没有发生改变的变量**，也可以在编译器直接代入它们的初始值，直接进行计算。

​	对于寻找在整个代码运行阶段都没有发生改变的变量，采取的方法是在变量定义时将变量和对应的初始值加到哈希表中，之后在翻译到赋值语句等对左端数值做出改变的语句时，将左端符号移除哈希表。当所有代码翻译完成后，遍历每条中间代码，对中间代码中所有在哈希表中的符号替换为相应的数值。



#### 编译期计算

​	编译期计算比较容易实现。在翻译到每个二元操作符如+、-、*、/时，如果其左右操作数都是常量，则可以直接进行计算，使用这个计算结果替换这两个操作数和二元操作符。



#### 函数内联

​	首先需要**判断函数是否可以内联**，由于 SysY 语法中不包含函数声明语句，因此不需要生成函数调用关系图即可以判断函数是否可以内联。在生成中间代码过程中，如果发现函数定义的语句块调用了自己，证明该函数是递归函数，因此不可内联。**不包含调用自己的函数是可以内联的。**

​	函数内联的实现是在翻译到`FuncExp`时，如果判断得出函数可以内联，则不翻译调用函数的跳转语句，而是将内联函数的函数体嵌入到此位置。在内联函数嵌入之前，需要做两步操作，一是将函数的实参赋值给函数的形参；二是由于`return int`可以在函数中多次出现，因此需要在在内联函数外部申请一个变量用于保存内联函数的返回值，内联函数所有返回值都赋值给这个变量。

​	函数内联实现的**难点在于对内联函数符号表的处理**，原函数和内联函数可能会定义相同的符号，这意味着直接将函数内联替换FuncExp可能会导致出现符号重定义错误。因此，考虑对内联函数的符号做一个映射，统一映射到`INLINE_(原符号名字)_(序号)`。当开始函数内联时，开启一个全局标记，当检测到全局标记时，将所有符号名字按照上述模式做映射。



#### 循环翻译优化

​	循环是程序中很重要的组成部分，且虽然语句量不一定多，但占据了程序运行中的大量时间，因此针对循环的优化可以有效提高程序的运行效率。

​	一个比较容易实现的优化点是将while语句翻译为do-while语句，原理如下：

```c
while(<cond>) {
    <stmt>
}
==> 
while_check:
	BEQ <cond> while_end
    <stmt>
    j while_check
while_end:
一共需要2n次跳转


if(<cond>){
    do {
        <stmt>
    } while(<cond>)
}
==>
j do_check
do_body:
	<stmt>
do_check:
 	BNE <cond> do_body
do_end:
一共需要n+1次跳转
```

​	通过将while转化为do-while，可以将2n次跳转转化为n+1次，极大地降低了循环语句的跳转次数。



#### 窥孔优化

##### 表达式合并优化

​	该优化是针对于以下指令

```
ADD, t1, t2, 0  t2->t1
SUB, t1, t2, 0  t2->t1
MUL, t1, t2, 1  t2->t1
MOVE, t1, t2    t2->t1
```

​	对于以上的这些指令，可以使用t2来代替t1，来减少指令条数。需要注意的是，如果t1跨基本块仍旧活跃，需要在基本块结尾将t2的值重新赋给t1，这一检查可以通过活跃变量分析检查基本块结尾处的活跃变量来完成。



##### 合并分支语句

​	该优化是针对于以下的指令序列

```
EQ/NEQ/LT/LE/GT/GE, a, b, 0
Branch a ? label1 : label2

==>
BEQZ/BNEZ/BLTZ/BLEZ/BGTZ/BGEZ a, label1, label2
```

​	对于以上的指令序列，可以将第一条的判断语句和第二条的跳转语句做合并，使用判断并跳转语句来做替换，可以减少指令条数。



### 目标代码优化

#### 图着色全局寄存器分配策略

​	为跨基本块仍然活跃的变量分配全局寄存器，主要是函数中的局部变量。

​	全局寄存器采用图着色算法来管理和分配。图着色算法是理论课的重点知识，使用理论课学到的算法先得到变量冲突图，接着依次从图中拿到节点做着色操作，为着同一个颜色的变量分配同一个寄存器。图着色分配后，相当于变量与这个寄存器**处于时刻绑定状态**，对于除了非函数调用之外的跨基本块跳转语句，都可以不free全局寄存器。



#### OPT临时寄存器分配策略

​	为只在基本块内活跃的变量分配临时寄存器，主要是运算过程中的临时变量，以及整个程序可见的全局变量。

​	对于临时寄存器，采用理论最优的OPT全局寄存器分配策略。对于我们的指令规模，可以使用暴力扫描变量的下次使用时间，将下次使用时间距当前最长的变量保存到内存中，并释放它的寄存器。

​	另一个寄存器分配方面可以优化的点在于**对于基本块少而大的计算密集型函数**，可以将大部分寄存器设置为临时寄存器，仅留下少量全局寄存器来分给跨基本块仍活跃的变量。因为在操作系统课已经学过，OPT分配策略是理论最优的，因此给包含大基本块的计算密集型函数分配更多的临时寄存器用来保存计算的中间值是合理的，能最大程度地提高性能。



#### 指令选择优化

​	指令选择优化看似比较简单，但是选择对了指令也能够极大地提高性能。一些指令替换方法如下：

```
sge ==> 4句基础代码
slti ==> 1句基础代码
==> 使用slti代替sge

seq $t1, $t2, 0 ==> 4句基础代码
beq $t1, $0, label1 ==> 1句基础代码
beqz $t2, label1 ==> 1句基础代码
==> 使用beqz代替seq和beq，同理sne\slt\sle\sgt\sge所在的比较后跳转指令也可以做相应的替换
```



#### 乘除优化

​	乘除优化的实现参考论文写就好了（参考论文可以见Division by Invariant Integers using Multiplication），由于在竞速排序中，乘除指令被赋予了很高的权重，因此考虑将乘除指令翻译成翻译成多条移位等位运算来做等价代换。



#### 窥孔优化

##### 无用跳转语句的删除

​	当跳转到的基本块正好是接下来的基本块时，可以删除跳转语句。

​	此外，在做了图着色寄存器分配后，正常的跨基本块跳转只需要保存并释放临时寄存器，当跳转指令跳转到的基本块正好是下一个基本块且下一个基本块只能通过该跳转指令跳转到时，意味着这两个基本块实质上可以合并在一起，因此也就无需再保存和释放临时寄存器了，这样可以减少不必要的sw lw指令。



##### 无用mips指令的删除

​	在这里，我删除的mips指令包括`addiu $t0, $t0, 0`和`move $t0, $t0`。这两种指令显然是无用的。



#### 一个没做成功的优化--DAG图优化

​	DAG图优化可以消除代码段中的公共子表达式，我曾尝试了一整天想要实现这一优化，然而发现DAG图导出中间代码时的指令顺序是会影响运行结果的。此外，对于lw、sw和getint指令，需要额外定义操作符来把他们加到DAG图中。如果指令序列中出现多个getint指令，还要确保多个getint的指令相对顺序与之前一致。虽然理论课上所讲的代码原理相对简单，但是具体实现起来其实是比较困难的。






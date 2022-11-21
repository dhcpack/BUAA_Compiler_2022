package Frontend;

import Exceptions.IllegalBreakContinueException;
import Exceptions.IllegalReturnException;
import Exceptions.IllegalSymbolException;
import Exceptions.MismatchParamCountException;
import Exceptions.MismatchParamTypeException;
import Exceptions.MismatchPrintfException;
import Exceptions.MissRbrackException;
import Exceptions.MissReturnException;
import Exceptions.MissRparentException;
import Exceptions.MissSemicnException;
import Exceptions.ModifyConstException;
import Exceptions.MyAssert;
import Exceptions.RedefinedTokenException;
import Exceptions.UndefinedTokenException;
import Frontend.Lexer.Token;
import Frontend.Lexer.TokenType;
import Frontend.Parser.CompUnit;
import Frontend.Parser.decl.types.Decl;
import Frontend.Parser.decl.types.Def;
import Frontend.Parser.decl.types.InitVal;
import Frontend.Parser.decl.types.Var;
import Frontend.Parser.expr.types.AddExp;
import Frontend.Parser.expr.types.BraceExp;
import Frontend.Parser.expr.types.Cond;
import Frontend.Parser.expr.types.ConstExp;
import Frontend.Parser.expr.types.EqExp;
import Frontend.Parser.expr.types.Exp;
import Frontend.Parser.expr.types.FuncExp;
import Frontend.Parser.expr.types.FuncRParams;
import Frontend.Parser.expr.types.UnaryOp;
import Frontend.Symbol.Errors;
import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolTable;
import Frontend.Symbol.SymbolType;
import Middle.type.BlockNode;
import Middle.type.Immediate;
import Frontend.Parser.expr.types.LAndExp;
import Frontend.Parser.expr.types.LOrExp;
import Frontend.Parser.expr.types.LVal;
import Frontend.Parser.expr.types.MulExp;
import Frontend.Parser.expr.types.Number;
import Frontend.Parser.expr.types.PrimaryExp;
import Frontend.Parser.expr.types.PrimaryExpInterface;
import Frontend.Parser.expr.types.RelExp;
import Frontend.Parser.expr.types.UnaryExp;
import Frontend.Parser.expr.types.UnaryExpInterface;
import Frontend.Parser.func.types.FuncDef;
import Frontend.Parser.func.types.FuncFParam;
import Frontend.Parser.func.types.MainFuncDef;
import Frontend.Parser.stmt.types.AssignStmt;
import Frontend.Parser.stmt.types.BlockItem;
import Frontend.Parser.stmt.types.BlockStmt;
import Frontend.Parser.stmt.types.BreakStmt;
import Frontend.Parser.stmt.types.ContinueStatement;
import Frontend.Parser.stmt.types.ExpStmt;
import Frontend.Parser.stmt.types.GetIntStmt;
import Frontend.Parser.stmt.types.IfStmt;
import Frontend.Parser.stmt.types.PrintfStmt;
import Frontend.Parser.stmt.types.ReturnStmt;
import Frontend.Parser.stmt.types.Stmt;
import Frontend.Parser.stmt.types.StmtInterface;
import Frontend.Parser.stmt.types.WhileStmt;
import Frontend.Util.ConstExpCalculator;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.FuncCall;
import Middle.type.GetInt;
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Operand;
import Middle.type.Pointer;
import Middle.type.PrintInt;
import Middle.type.PrintStr;
import Middle.type.Return;


import java.util.ArrayList;
import java.util.Stack;
import java.util.stream.Collectors;

/*
 *
 * 错误处理+生成符号表+生成中间代码
 * 符号表需要下降一层: 定义的新函数，blockStmt，whileStmt，ifStmt
 *
 */
public class SymbolTableBuilder {
    private SymbolTable currSymbolTable = new SymbolTable(null, null);
    private final Errors errors = new Errors();
    private final CompUnit compUnit;
    private final MiddleCode middleCode = new MiddleCode();

    // about block
    private BasicBlock currBlock;
    private int blockCount = 0;  // 根据这个来命名
    private int blockId = 0;  // 根据这个来排序

    // about function
    private FuncBlock currFunc = null;  // currentFunc = null代表在全局
    private TokenType currFuncType = null;

    // about loop
    private final Stack<BasicBlock> whileHead = new Stack<>();  // for continue
    private final Stack<BasicBlock> whileNext = new Stack<>();  // for break
    private int loopDepth = 0;

    public SymbolTableBuilder(CompUnit compUnit) {
        this.compUnit = compUnit;
    }

    public Errors getErrors() {
        return errors;
    }

    public CompUnit getCompUnit() {
        return compUnit;
    }

    public MiddleCode getMiddleCode() {
        return middleCode;
    }

    // CompUnit → {Decl} {FuncDef} MainFuncDef
    public void checkCompUnit() {
        ArrayList<Decl> decls = compUnit.getGlobalVariables();
        ArrayList<FuncDef> funcDefs = compUnit.getFunctions();
        MainFuncDef mainFunction = compUnit.getMainFunction();
        // check decl
        for (Decl decl : decls) {
            checkDecl(decl, Symbol.Scope.GLOBAL);
        }

        // check func def
        for (FuncDef funcDef : funcDefs) {
            checkFunc(funcDef, false);
        }

        // check main func
        checkMainFunc(mainFunction);

    }

    // Decl → ConstDecl | VarDecl
    // 变量声明 VarDecl → 'int' VarDef { ',' VarDef } ';'
    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public void checkDecl(Decl decl, Symbol.Scope scope) {
        if (decl.missSemicolon()) {
            errors.add(new MissSemicnException(decl.getLine()));
        }
        Def def = decl.getDef();
        checkDef(def, scope);
        ArrayList<Def> defs = decl.getDefs();
        for (Def ndef : defs) {
            checkDef(ndef, scope);
        }
    }

    // 常数定义 ConstDef → Var '=' ConstInitVal
    // 变量定义 VarDef → Var | Var '=' InitVal
    public void checkDef(Def def, Symbol.Scope scope) {
        /*
         *  int a = a * a;
         * */
        if (def.getDimCount() == 0) {  // not an array; int or pointer
            if (def.hasInitVal()) {  // 已经初始化
                InitVal initVal = def.getInitVal();
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                symbol.setScope(scope);
                try {  // 全部扔到ConstExpCalculator里面去算，能算就算，得到AssertionError就在运行中计算
                    int val;
                    if (!initVal.isConst()) {
                        val = new ConstExpCalculator(currSymbolTable, errors).calcExp(initVal.getExp());
                    } else {
                        val = new ConstExpCalculator(currSymbolTable, errors).calcConstExp(initVal.getConstExp());
                    }
                    if (symbol.isConst()) {  // 为常量赋初始值
                        symbol.setConstInitInt(val);
                    }
                    if (currFunc == null) {  // pre decl, not in a function  // 全局
                        symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                        middleCode.addInt(def.getVar().getIdent().getContent(), symbol.getAddress(), val);
                        symbol.setScope(Symbol.Scope.GLOBAL);
                    } else {  // decl in a function  // 局部
                        symbol.setAddress(currSymbolTable.getStackSize());
                        currBlock.addContent(new Middle.type.FourExpr(new Immediate(val), symbol, FourExpr.ExprOp.DEF));
                        symbol.setScope(Symbol.Scope.LOCAL);
                    }
                } catch (MyAssert error) {
                    // System.err.println("cannot calculate, calc in the process");
                    Operand val;
                    if (initVal.isConst()) {
                        val = checkConstExp(initVal.getConstExp(), false);
                    } else {
                        val = checkExp(initVal.getExp(), false);
                    }

                    symbol.setAddress(currSymbolTable.getStackSize());
                    currBlock.addContent(new Middle.type.FourExpr(val, symbol, FourExpr.ExprOp.DEF));
                    symbol.setScope(Symbol.Scope.LOCAL);
                }
                // if (initVal.isConst() || currFunc == null) {  // 初始化数值可以直接计算出结果
                //     int val;
                //     if (!initVal.isConst()) {
                //         val = new ConstExpCalculator(currSymbolTable, errors).calcExp(initVal.getExp());
                //     } else {
                //         val = new ConstExpCalculator(currSymbolTable, errors).calcConstExp(initVal.getConstExp());
                //     }
                //     symbol.setConstInitInt(val);
                //     if (currFunc == null) {  // pre decl, not in a function  // 全局
                //         symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                //         middleCode.addInt(def.getVar().getIdent().getContent(), symbol.getAddress(), val);
                //         symbol.setScope(Symbol.Scope.GLOBAL);
                //     } else {  // decl in a function  // 局部
                //         symbol.setAddress(currSymbolTable.getStackSize());
                //         currBlock.addContent(new Middle.type.FourExpr(new Immediate(val), symbol, FourExpr.ExprOp.DEF));
                //         symbol.setScope(Symbol.Scope.LOCAL);
                //     }
                // } else {  // 初始化数值不可以直接计算出结果，用FourExpr表示
                //     try{
                //         int val;
                //         if (!initVal.isConst()) {
                //             val = new ConstExpCalculator(currSymbolTable, errors).calcExp(initVal.getExp());
                //         } else {
                //             val = new ConstExpCalculator(currSymbolTable, errors).calcConstExp(initVal.getConstExp());
                //         }
                //         symbol.setConstInitInt(val);
                //         if (currFunc == null) {  // pre decl, not in a function  // 全局
                //             symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                //             middleCode.addInt(def.getVar().getIdent().getContent(), symbol.getAddress(), val);
                //             symbol.setScope(Symbol.Scope.GLOBAL);
                //         } else {  // decl in a function  // 局部
                //             symbol.setAddress(currSymbolTable.getStackSize());
                //             currBlock.addContent(new Middle.type.FourExpr(new Immediate(val), symbol, FourExpr.ExprOp.DEF));
                //             symbol.setScope(Symbol.Scope.LOCAL);
                //         }
                //     } catch (AssertionError error){
                //         System.err.println("cannot calculate, calc in the process");
                //         Operand val;
                //         if (initVal.isConst()) {
                //             val = checkConstExp(initVal.getConstExp(), false);
                //         } else {
                //             val = checkExp(initVal.getExp(), false);
                //         }
                //
                //         symbol.setAddress(currSymbolTable.getStackSize());
                //         currBlock.addContent(new Middle.type.FourExpr(val, symbol, FourExpr.ExprOp.DEF));
                //         symbol.setScope(Symbol.Scope.LOCAL);
                //     }
                // }
            } else {  // 没有初始化
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                symbol.setScope(scope);
                if (currFunc == null) {  // pre decl, not in a function
                    symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                    middleCode.addInt(def.getVar().getIdent().getContent(), symbol.getAddress(), 0);
                    symbol.setScope(Symbol.Scope.GLOBAL);
                    symbol.setConstInitInt(0);
                } else {  // decl in a function
                    symbol.setAddress(currSymbolTable.getStackSize());
                    currBlock.addContent(new Middle.type.FourExpr(new Immediate(0), symbol, FourExpr.ExprOp.DEF));
                    symbol.setScope(Symbol.Scope.LOCAL);
                    // 未初始化的局部变量
                }
                assert !symbol.isConst() : "没有初始化的符号应该不是常量吧";
            }
        } else {  // 数组
            if (def.hasInitVal()) {  // 已经初始化
                InitVal initVal = def.getInitVal();
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                symbol.setScope(scope);
                ArrayList<AddExp> initExp = flatArrayInitVal(initVal);

                try {  // 全部扔到ConstExpCalculator里面去算，能算就算，得到AssertionError就在运行中计算
                    ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors);
                    ArrayList<Integer> initNum = initExp.stream().map(constExpCalculator::calcAddExp)
                            .collect(Collectors.toCollection(ArrayList::new));
                    if (symbol.isConst()) {
                        symbol.setConstInitArray(initNum);
                    }
                    if (currFunc == null) {  // 全局变量
                        symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                        middleCode.addArray(symbol.getIdent().getContent(), symbol.getAddress(), initNum);
                        symbol.setScope(Symbol.Scope.GLOBAL);
                    } else {  // 局部变量
                        int offset = 0;
                        for (Integer num : initNum) {
                            Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                            currBlock.addContent(new Memory(symbol, new Immediate(offset * 4), ptr));  // 局部数组
                            currBlock.addContent(new Pointer(Pointer.Op.STORE, ptr, new Immediate(num)));
                            offset++;
                        }
                        symbol.setAddress(currSymbolTable.getStackSize());
                        symbol.setScope(Symbol.Scope.LOCAL);
                    }
                } catch (MyAssert error) {
                    // System.err.println("cannot calculate array, calc in the process");
                    int offset = 0;
                    for (AddExp addExp : initExp) {
                        Operand exp = checkAddExp(addExp, false);
                        Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                        currBlock.addContent(new Memory(symbol, new Immediate(offset * 4), ptr));  // 局部数组
                        currBlock.addContent(new Pointer(Pointer.Op.STORE, ptr, exp));
                        offset++;
                    }
                    symbol.setAddress(currSymbolTable.getStackSize());
                    symbol.setScope(Symbol.Scope.LOCAL);
                }
                // // int stackSize = 1;
                // // ArrayList<Integer> dimSize = symbol.getDimSize();
                // // for (Integer s : dimSize) {
                // //     stackSize *= s;
                // // }
                // if (initVal.isConst() || currFunc == null) {  // 初始化数值可以直接计算出结果
                //     ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors);
                //     ArrayList<Integer> initNum = initExp.stream().map(constExpCalculator::calcAddExp)
                //             .collect(Collectors.toCollection(ArrayList::new));
                //     symbol.setConstInitArray(initNum);
                //     if (currFunc == null) {  // 全局变量
                //         symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                //         middleCode.addArray(symbol.getIdent().getContent(), symbol.getAddress(), initNum);
                //         symbol.setScope(Symbol.Scope.GLOBAL);
                //     } else {  // 局部变量
                //         int offset = 0;
                //         for (Integer num : initNum) {
                //             Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                //             currBlock.addContent(new Memory(symbol, new Immediate(offset * 4), ptr));  // 局部数组
                //             currBlock.addContent(new Pointer(Pointer.Op.STORE, ptr, new Immediate(num)));
                //             offset++;
                //         }
                //         symbol.setAddress(currSymbolTable.getStackSize());
                //         symbol.setScope(Symbol.Scope.LOCAL);
                //     }
                // } else {  // 初始化数值不可以直接计算出结果，用FourExpr表示
                //     try{
                //         ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors);
                //         ArrayList<Integer> initNum = initExp.stream().map(constExpCalculator::calcAddExp)
                //                 .collect(Collectors.toCollection(ArrayList::new));
                //         symbol.setConstInitArray(initNum);
                //         if (currFunc == null) {  // 全局变量
                //             symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                //             middleCode.addArray(symbol.getIdent().getContent(), symbol.getAddress(), initNum);
                //             symbol.setScope(Symbol.Scope.GLOBAL);
                //         } else {  // 局部变量
                //             int offset = 0;
                //             for (Integer num : initNum) {
                //                 Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                //                 currBlock.addContent(new Memory(symbol, new Immediate(offset * 4), ptr));  // 局部数组
                //                 currBlock.addContent(new Pointer(Pointer.Op.STORE, ptr, new Immediate(num)));
                //                 offset++;
                //             }
                //             symbol.setAddress(currSymbolTable.getStackSize());
                //             symbol.setScope(Symbol.Scope.LOCAL);
                //         }
                //     } catch (AssertionError error){
                //         System.err.println("cannot calculate array, calc in the process");
                //         int offset = 0;
                //         for (AddExp addExp : initExp) {
                //             Operand exp = checkAddExp(addExp, false);
                //             Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                //             currBlock.addContent(new Memory(symbol, new Immediate(offset * 4), ptr));  // 局部数组
                //             currBlock.addContent(new Pointer(Pointer.Op.STORE, ptr, exp));
                //             offset++;
                //         }
                //         symbol.setAddress(currSymbolTable.getStackSize());
                //         symbol.setScope(Symbol.Scope.LOCAL);
                //     }
                // }
            } else {  // 没有初始化
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                symbol.setScope(scope);
                if (currFunc == null) {
                    ArrayList<Integer> initZero = new ArrayList<>();
                    int totalCount = symbol.getSize() / 4;  // 数组应该初始化为多少个零
                    for (int i = 0; i < totalCount; i++) initZero.add(0);
                    symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                    middleCode.addArray(symbol.getIdent().getContent(), symbol.getAddress(), initZero);
                    symbol.setScope(Symbol.Scope.GLOBAL);
                    // symbol.setConstInitArray(initZero);
                } else {
                    symbol.setAddress(currSymbolTable.getStackSize());
                    symbol.setScope(Symbol.Scope.LOCAL);
                    // nothing to do
                    // 函数中的局部变量，没有初始化
                }
                assert !symbol.isConst();
            }
        }
    }

    private ArrayList<AddExp> flatArrayInitVal(InitVal initVal) {
        ArrayList<AddExp> res = new ArrayList<>();
        if (initVal.isLeaf()) {
            if (initVal.getExp() != null) {
                res.add(initVal.getExp().getAddExp());
            } else {
                res.add(initVal.getConstExp().getAddExp());
            }
        } else {
            for (InitVal init : initVal.getInitVals()) {
                res.addAll(flatArrayInitVal(init));
            }
        }
        return res;
    }

    //  常量变量 Var -> Ident { '[' ConstExp ']' }
    public Symbol checkVar(Var var) {
        // check redefine
        boolean redefine = false;
        Token ident = var.getIdent();
        if (currSymbolTable.contains(ident.getContent(), false)) {
            errors.add(new RedefinedTokenException(ident.getLine()));
            redefine = true;
        }

        // check missRBrack
        if (var.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }

        // check const Exp and **calc it**; save the result into ARRAY dimSize
        ArrayList<ConstExp> dimExp = var.getDimExp();
        ArrayList<Integer> dimSize = new ArrayList<>();
        ConstExpCalculator dimSizeCalculator = new ConstExpCalculator(currSymbolTable, errors);
        for (ConstExp constExp : dimExp) {
            dimSize.add(dimSizeCalculator.calcConstExp(constExp));
            // TODO: put error check in calcUti. DONE!
            // checkConstExp(constExp);
        }
        var.setDimSize(dimSize);

        // add to symbol table
        if (!redefine) {
            Symbol symbol = new Symbol(var.getDimCount() == 0 ? SymbolType.INT : SymbolType.ARRAY, ident, dimSize,
                    var.getDimCount(), var.isConst(), null);  // checkVal没有设置scope
            currSymbolTable.addSymbol(symbol);  // 会同时为Symbol申请空间
            // symbol.setAddress(currSymbolTable.getStackSize());  // setAddress移动到调用LVal的函数中做，便于根据GLOBAL or LOCAL设定不同的address
            return symbol;
        }
        assert false : "redefine";
        return null;
    }

    // 常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    // 变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    // public void checkInitVal(InitVal initVal) {
    //     if (initVal.getExp() != null) {  // only expr
    //         checkExp(initVal.getExp());
    //     } else if (initVal.getConstExp() != null) {  // only const expr
    //         checkConstExp(initVal.getConstExp());
    //     } else {  // initial Vals
    //         ArrayList<InitVal> initVals = initVal.getInitVals();
    //         for (InitVal initVal1 : initVals) {
    //             checkInitVal(initVal1);
    //         }
    //     }
    // }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public void checkFunc(FuncDef funcDef, boolean isMainFunc) {
        currFuncType = funcDef.getReturnType().getType();
        Token ident = funcDef.getIdent();
        // check redefine
        boolean redefine = false;
        if (currSymbolTable.contains(ident.getContent(), false)) {  // check redefine
            errors.add(new RedefinedTokenException(ident.getLine()));
            redefine = true;
            assert false;
            // return;  //  TODO: stop here or not?
        }

        currSymbolTable = new SymbolTable(currSymbolTable, null);  // 下降一层(函数符号表)

        // check missing Parenthesis
        if (funcDef.missRightParenthesis()) {
            errors.add(new MissRparentException(funcDef.getLeftParenthesis().getLine()));
        }

        // check FuncFParams
        ArrayList<FuncFParam> funcFParams = funcDef.getFuncFParams();
        ArrayList<Symbol> params = new ArrayList<>();
        // int addr = 4;  // 函数栈基地址处放ra，从4开始放参数（也从4开始取参数）
        for (FuncFParam funcFParam : funcFParams) {
            Symbol symbol = checkFuncFParam(funcFParam); // checkFuncFParam could return null
            if (symbol != null) {
                params.add(symbol);
                symbol.setAddress(currSymbolTable.getStackSize());
                // symbol.setAddress(addr);
                // addr += symbol.getSize();
            }
        }
        // System.out.println();

        FuncBlock funcBlock = new FuncBlock(funcDef.getReturnType()
                .getType() == TokenType.INTTK ? FuncBlock.ReturnType.INT : FuncBlock.ReturnType.VOID,
                funcDef.getIdent().getContent(), params, currSymbolTable, isMainFunc);
        middleCode.addFunc(funcBlock);  // 加入到中间代码入口表中
        currFunc = funcBlock;


        // add to Frontend.Symbol Table(parent symbol table)
        Token returnType = funcDef.getReturnType();
        if (!redefine) {
            currSymbolTable.getParent().addSymbol(new Symbol(SymbolType.FUNCTION,  // func 加到父符号表中
                    returnType.getType() == TokenType.INTTK ? SymbolType.INT : SymbolType.VOID, params, ident));
        }

        // 给末尾没有return语句的函数加return(直接在所有函数结尾无脑加jr $ra)
        BlockStmt funcBody = funcDef.getBlockStmt();
        if (funcBody.getReturn() == null) {
            if(returnType.getType() == TokenType.VOIDTK){
                funcBody.getBlockItems()
                        .add(new Stmt(new ReturnStmt(Token.tempToken(TokenType.RETURNTK, funcBody.getRightBrace().getLine()))));
            } else {
                funcBody.getBlockItems()
                        .add(new Stmt(new ReturnStmt(Token.tempToken(TokenType.RETURNTK, funcBody.getRightBrace().getLine()))));
            }

        }

        // check func block
        BasicBlock body = checkBlockStmt(funcDef.getBlockStmt(), true, funcBlock.getLabel());

        // BasicBlock body = new BasicBlock(funcBlock.getLabel());
        // body.addContent(new Jump(block));
        currFunc.setBody(body);
        // checkFuncBlock(funcBlock, funcDef.getBlockStmt());

        // check return right or not
        // 函数return int， 但是没有return语句
        if (returnType.getType() == TokenType.INTTK && !funcDef.returnInt()) {
            errors.add(new MissReturnException(funcDef.getRightBrace().getLine()));
        }

        currSymbolTable = currSymbolTable.getParent();  // 上升一层
        currFunc = null;  // 上升一层
        // currBlock = null;
    }

    // MainFuncDef → 'int' 'main' '(' ')' Block
    public void checkMainFunc(MainFuncDef mainFuncDef) {
        checkFunc(mainFuncDef.getFuncDef(), true);
    }

    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    // TODO: WARNING!!! this func could return null
    // TODO: WARNING!!! 函数参数每一个的大小都是4，数组参数会传入指针
    // TODO: WARNING!!! 这个函数会将函数形参放在符号表中，但不会给Symbol设置地址
    public Symbol checkFuncFParam(FuncFParam funcFParam) {
        // check redefine
        boolean redefine = false;
        Token ident = funcFParam.getIdent();
        if (currSymbolTable.contains(ident.getContent(), false)) {
            redefine = true;
            errors.add(new RedefinedTokenException(ident.getLine()));
        }

        // check miss RBrack
        if (funcFParam.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }

        // add to symbol table
        // 函数参数每一个的大小都是4
        if (!redefine) {
            if (funcFParam.isArray()) {
                ArrayList<ConstExp> dimExp = funcFParam.getDimExp();
                ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors);
                ArrayList<Integer> dimSize = dimExp.stream().map(constExpCalculator::calcConstExp)
                        .collect(Collectors.toCollection(ArrayList::new));
                dimSize.add(0, -20231164);  // 第一维省略
                // TODO: WARNING!!! 这里会给数组类型的函数参数符号的Scope设置为PARAM，在translateMemory中会被用到
                Symbol array = new Symbol(SymbolType.ARRAY, ident, dimSize, dimSize.size(), false, Symbol.Scope.PARAM);
                currSymbolTable.addSymbol(array);
                // array.setAddress(currSymbolTable.getStackSize());  // 在checkFunc中设置地址
                return array;
            } else {
                Symbol val = new Symbol(SymbolType.INT, ident, false, Symbol.Scope.LOCAL);
                currSymbolTable.addSymbol(val);
                // val.setAddress(currSymbolTable.getStackSize());
                return val;
            }
        } else {
            assert false : "TO BE FIXED: 函数参数重名";
            return null;
        }
    }

    // 语句块项 BlockItem → Decl | Stmt
    public void checkBlockItem(BlockItem blockItem) {
        if (blockItem instanceof Decl) {
            checkDecl((Decl) blockItem, Symbol.Scope.LOCAL);
        } else if (blockItem instanceof Stmt) {
            checkStmt((Stmt) blockItem);
        } else {
            assert false;
        }
    }

    // many stmts
    public void checkStmt(Stmt stmt) {
        // check miss semicolon
        StmtInterface stmtInterface = stmt.getStmt();
        if (stmt.missSemicolon()) {
            errors.add(new MissSemicnException(stmtInterface.getSemicolonLine()));
        }
        if (stmtInterface instanceof AssignStmt) {
            checkAssignStmt((AssignStmt) stmtInterface);
        } else if (stmtInterface instanceof BlockStmt) {
            checkBlockStmt((BlockStmt) stmtInterface, false, null);
        } else if (stmtInterface instanceof BreakStmt) {
            checkBreakStmt((BreakStmt) stmtInterface);
        } else if (stmtInterface instanceof ContinueStatement) {
            checkContinueStmt((ContinueStatement) stmtInterface);
        } else if (stmtInterface instanceof ExpStmt) {
            checkExpStmt((ExpStmt) stmtInterface);
        } else if (stmtInterface instanceof GetIntStmt) {
            checkGetIntStmt((GetIntStmt) stmtInterface);
        } else if (stmtInterface instanceof IfStmt) {
            checkIfStmt((IfStmt) stmtInterface);
        } else if (stmtInterface instanceof PrintfStmt) {
            checkPrintfStmt((PrintfStmt) stmtInterface);
        } else if (stmtInterface instanceof ReturnStmt) {
            checkReturnStmt((ReturnStmt) stmtInterface);
        } else if (stmtInterface instanceof WhileStmt) {
            checkWhileStmt((WhileStmt) stmtInterface);
        }
    }

    // LVal '=' Exp ;
    // TODO: check LVal using right or not（LVal是否正确使用，和Exp是否匹配int）
    public void checkAssignStmt(AssignStmt assignStmt) {
        // check LVal, and LVal could not be const
        Symbol lVal = checkLVal(assignStmt.getLVal(), true);
        // check right Val
        Operand operand = checkExp(assignStmt.getExp(), false);
        if (lVal.getSymbolType() == SymbolType.POINTER) {  // 如果返回的是指针，直接存在内存里
            currBlock.addContent(new Pointer(Pointer.Op.STORE, lVal, operand));  // 数组存内存
        } else if (lVal.getSymbolType() == SymbolType.INT) {
            currBlock.addContent(new FourExpr(operand, lVal, FourExpr.ExprOp.ASS));
        } else {
            assert false;
        }
        return;
    }

    // Block → '{' { BlockItem } '}'
    public BasicBlock checkBlockStmt(BlockStmt blockStmt, boolean isFunc, String funcLabel) {
        if (!isFunc) {
            currSymbolTable = new SymbolTable(currSymbolTable, currFunc.getFuncSymbolTable());  // 下降一层
        }
        BasicBlock basicBlock;
        if (isFunc) {
            basicBlock = new BasicBlock(funcLabel, blockId++);
        } else {
            basicBlock = new BasicBlock("B_" + blockCount++, blockId++);
        }
        if (currBlock != null) {
            currBlock.addContent(new Jump(basicBlock));
        }
        currBlock = basicBlock;
        // blockDepth++;
        // traverse all blockItems and check
        ArrayList<BlockItem> blockItems = blockStmt.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            checkBlockItem(blockItem);
        }
        BasicBlock nextBlock = new BasicBlock("B_" + blockCount++, blockId++);
        // blockDepth--;
        if (!isFunc) {
            basicBlock.addContent(new Jump(nextBlock));
        }
        if (currBlock.getLastContent() == null || !(currBlock.getLastContent() instanceof Jump || currBlock.getLastContent() instanceof Return)) {
            currBlock.addContent(new Jump(nextBlock));
        }
        currBlock = nextBlock;
        if (!isFunc) {
            currSymbolTable = currSymbolTable.getParent();  // 上升一层
        }
        return basicBlock;
    }

    // break;
    public void checkBreakStmt(BreakStmt breakStmt) {
        if (loopDepth == 0) {
            errors.add(new IllegalBreakContinueException(breakStmt.getSemicolonLine()));
        }
        BasicBlock nextBlock = whileNext.peek();
        currBlock.addContent(new Jump(nextBlock));
    }

    // continue;
    public void checkContinueStmt(ContinueStatement continueStatement) {
        if (loopDepth == 0) {
            errors.add(new IllegalBreakContinueException(continueStatement.getSemicolonLine()));
        }
        BasicBlock headBlock = whileHead.peek();
        currBlock.addContent(new Jump(headBlock));
    }

    // Exp ';'
    public void checkExpStmt(ExpStmt expStmt) {
        checkExp(expStmt.getExp(), false);
    }

    // LVal '=' 'getint''('')'
    // TODO: check LVal using right or not（LVal是否正确使用，和Exp是否匹配，int）
    public void checkGetIntStmt(GetIntStmt getIntStmt) {
        // check miss Right Parenthesis
        if (getIntStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(getIntStmt.getLine()));
        }
        // check LVal, and LVal could not be const
        Symbol symbol = checkLVal(getIntStmt.getLVal(), true);
        currBlock.addContent(new GetInt(symbol));  // TODO: WARNING!!! GetInt会根据global local temp来设计sw, lw指令，在translateGetInt
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public void checkIfStmt(IfStmt ifStmt) {
        // check cond Stmt
        Operand cond = checkCond(ifStmt.getCond());
        if (ifStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(ifStmt.getIfLine()));
        }
        // 通过ID保证中间代码和mips代码中基本块的顺序和遍历顺序一致
        currSymbolTable = new SymbolTable(currSymbolTable, currFunc.getFuncSymbolTable());  // 下降一层
        BasicBlock ifBody = new BasicBlock("IF_BODY_" + blockCount++);
        BasicBlock ifEnd = new BasicBlock("IF_END_" + blockCount++);
        // currBlock.addContent(new Jump());
        // ArrayList<Stmt> stmts = ifStmt.getStmts();
        if (ifStmt.hasElse()) {
            BasicBlock ifElse = new BasicBlock("IF_ELSE_" + blockCount++);
            currBlock.addContent(new Branch(cond, ifBody, ifElse, true));
            currBlock = ifBody;
            ifBody.setIndex(blockId++);
            checkStmt(ifStmt.getStmts().get(0));
            currBlock.addContent(new Jump(ifEnd));
            currBlock = ifElse;
            ifElse.setIndex(blockId++);
            checkStmt(ifStmt.getStmts().get(1));
            currBlock.addContent(new Jump(ifEnd));
        } else {
            currBlock.addContent(new Branch(cond, ifBody, ifEnd, true));
            currBlock = ifBody;
            ifBody.setIndex(blockId++);
            checkStmt(ifStmt.getStmts().get(0));
            currBlock.addContent(new Jump(ifEnd));
        }
        currBlock = ifEnd;
        ifEnd.setIndex(blockId++);  // 最后给ifEnd设置Id
        currSymbolTable = currSymbolTable.getParent();  // 上升一层
    }

    public void checkPrintfStmt(PrintfStmt printfStmt) {
        if (!printfStmt.checkCountMatch()) {  // 检查Exp个数是否匹配
            errors.add(new MismatchPrintfException(printfStmt.getPrintf().getLine()));
        }
        if (!printfStmt.checkFormatString()) {  // 检查formatString
            errors.add(new IllegalSymbolException(printfStmt.getFormatString().getLine()));
        }
        if (printfStmt.missRightParenthesis()) {  // 检查是否缺少左括号
            errors.add(new MissRparentException(printfStmt.getFormatString().getLine()));
        }

        // check printf("");
        if (printfStmt.getFormatString().getContent().equals("")) {
            return;
        }
        // 检查所有Exp 得到LeftNode
        ArrayList<Exp> exps = printfStmt.getExps();
        ArrayList<Exp> reverseExps = new ArrayList<>();  // gcc 要倒着check 真的很奇怪
        for (Exp exp : exps) {
            reverseExps.add(0, exp);
        }
        // ArrayList<LeafNode> outputs = exps.stream().map(this::checkExp)
        //         .collect(Collectors.toCollection(ArrayList::new));
        String formatString = printfStmt.getFormatString().getContent();
        // String[] s = printfStmt.getFormatString().getContent().split("%d");
        formatString = formatString.substring(1, formatString.length() - 1);
        String formatChar = "%d";
        ArrayList<BlockNode> printBlocks = new ArrayList<>();
        ArrayList<Operand> printExps = new ArrayList<>();
        int index = 0, prev = 0;
        for (Exp exp : reverseExps) {
            index = formatString.indexOf(formatChar, index);  // find %d and point to %
            if (index > prev) {
                String str = formatString.substring(prev, index);
                String strName = middleCode.addAsciiz(str);
                printBlocks.add(new PrintStr(strName));
            }
            printExps.add(checkExp(exp, false));
            printBlocks.add(new PrintInt());
            index += 2;
            prev = index;
        }
        if (index != formatString.length()) {
            String str = formatString.substring(index);
            String strName = middleCode.addAsciiz(str);
            printBlocks.add(new PrintStr(strName));
        }
        int i = printExps.size() - 1;
        for (BlockNode print : printBlocks) {
            if (print instanceof PrintStr) {
                currBlock.addContent(print);
            } else {
                ((PrintInt) print).setVal(printExps.get(i--));
                currBlock.addContent(print);
            }
        }
        return;
    }

    public void checkReturnStmt(ReturnStmt returnStmt) {
        assert currFunc != null;  // 不在Decl区
        if (currFuncType == TokenType.VOIDTK && returnStmt.getReturnExp() != null) {
            errors.add(new IllegalReturnException(returnStmt.getReturnToken().getLine()));
        }
        if (returnStmt.getReturnExp() != null) {
            Operand returnVal = checkExp(returnStmt.getReturnExp(), false);
            currBlock.addContent(new Return(returnVal));
        } else {
            currBlock.addContent(new Return());
        }
        // return int是否正确在checkFunc中检查
    }

    // 'while' '(' Cond ')' Stmt
    public void checkWhileStmt(WhileStmt whileStmt) {
        loopDepth++;
        // check missing Right Parenthesis
        if (whileStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(whileStmt.getLine()));
        }

        // 通过ID保证中间代码和mips代码中基本块的顺序和遍历顺序一致
        BasicBlock whileBlock = new BasicBlock("WHILE_" + blockCount++);
        BasicBlock whileBody = new BasicBlock("WHILE_BODY_" + blockCount++);
        BasicBlock whileEnd = new BasicBlock("WHILE_END_" + blockCount++);
        whileHead.push(whileBlock);
        whileNext.push(whileEnd);

        // step into while
        currBlock.addContent(new Jump(whileBlock));
        currBlock = whileBlock;
        currBlock.setIndex(blockId++);

        // check Cond
        Operand cond = checkCond(whileStmt.getCond());

        // step into whileBody
        currBlock.addContent(new Branch(cond, whileBody, whileEnd, true));
        currBlock = whileBody;
        whileBody.setIndex(blockId++);

        currSymbolTable = new SymbolTable(currSymbolTable, currFunc.getFuncSymbolTable());  // 下降一层
        // check Stmt
        checkStmt(whileStmt.getStmt());
        currSymbolTable = currSymbolTable.getParent();  // 上升一层
        loopDepth--;
        whileNext.pop();
        whileHead.pop();
        currBlock.addContent(new Jump(whileBlock));
        currBlock = whileEnd;
        whileEnd.setIndex(blockId++);
    }

    public Operand checkConstExp(ConstExp constExp, boolean returnPointer) {
        return checkAddExp(constExp.getAddExp(), returnPointer);
    }

    public Operand checkExp(Exp exp, boolean returnPointer) {
        return checkAddExp(exp.getAddExp(), returnPointer);
    }

    // 四元式表达
    // AddExp → MulExp {('+' | '−') MulExp}
    public Operand checkAddExp(AddExp addExp, boolean returnPointer) {
        Operand left = checkMulExp(addExp.getFirstExp(), returnPointer);
        ArrayList<MulExp> mulExps = addExp.getExps();
        // ArrayList<LeafNode> nodes = addExp.getExps().stream().map(this::checkMulExp)
        //         .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> seps = addExp.getSeps();
        for (int i = 0; i < seps.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.INT);
            Operand right = checkMulExp(mulExps.get(i), returnPointer);
            if (seps.get(i).getType() == TokenType.PLUS) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.ADD));
            } else if (seps.get(i).getType() == TokenType.MINU) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.SUB));
            } else {
                assert false;
            }
            left = temp;
        }
        return left;
    }

    // MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
    public Operand checkMulExp(MulExp mulExp, boolean returnPointer) {
        Operand left = checkUnaryExp(mulExp.getFirstExp(), returnPointer);
        ArrayList<UnaryExp> unaryExps = mulExp.getExps();
        // ArrayList<LeafNode> nodes = mulExp.getExps().stream().map(this::checkUnaryExp)
        //         .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> seps = mulExp.getSeps();
        for (int i = 0; i < seps.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.INT);
            Operand right = checkUnaryExp(unaryExps.get(i), returnPointer);
            if (seps.get(i).getType() == TokenType.MULT) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.MUL));
            } else if (seps.get(i).getType() == TokenType.DIV) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.DIV));
            } else if (seps.get(i).getType() == TokenType.MOD) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.MOD));
            } else {
                assert false;
            }
            left = temp;
        }
        return left;
    }

    // TODO: 优化UnaryOp合并
    // private ArrayList<UnaryOp> getUnaryOp(UnaryExp unaryExp) {
    //
    // }


    public Operand checkUnaryExp(UnaryExp unaryExp, boolean returnPointer) {
        UnaryExpInterface unaryExpInterface = unaryExp.getUnaryExpInterface();
        UnaryOp unaryOp = unaryExp.getOp();
        if (unaryExpInterface instanceof FuncExp) {
            return checkFuncExp((FuncExp) unaryExpInterface);
        } else if (unaryExpInterface instanceof PrimaryExp) {
            return checkPrimaryExp((PrimaryExp) unaryExpInterface, returnPointer);
        } else {
            assert unaryOp != null;
            Operand midRes = checkUnaryExp((UnaryExp) unaryExpInterface, returnPointer);
            if (unaryOp.getToken().getType() == TokenType.PLUS) {
                return midRes;
            }
            Symbol res = Symbol.tempSymbol(SymbolType.INT);
            if (unaryOp.getToken().getType() == TokenType.MINU) {
                currBlock.addContent(new FourExpr(midRes, res, FourExpr.ExprOp.NEG));
            } else if (unaryOp.getToken().getType() == TokenType.NOT) {
                currBlock.addContent(new FourExpr(midRes, res, FourExpr.ExprOp.NOT));
            }
            return res;
        }
    }

    public Operand checkPrimaryExp(PrimaryExp primaryExp, boolean returnPointer) {
        return checkPrimaryExpInterFace(primaryExp.getPrimaryExpInterface(), returnPointer);
    }

    // TODO: WARNING!!! 只有在解析函数调用的参数时，returnPointer才是true！！！！returnPointer可以作为解析函数参数的表征
    public Operand checkPrimaryExpInterFace(PrimaryExpInterface primaryExpInterface, boolean returnPointer) {
        if (primaryExpInterface instanceof BraceExp) {
            return checkBraceExp((BraceExp) primaryExpInterface, returnPointer);
        } else if (primaryExpInterface instanceof LVal) {
            // TODO: check LVal using right or not（LVal是否正确使用，和Exp是否匹配，int）
            Symbol lVal = checkLVal((LVal) primaryExpInterface, false);  // 计算后的symbol

            if (lVal.getSymbolType() == SymbolType.POINTER) {
                if (returnPointer) {  // 正在解析函数调用的参数
                    // Symbol lValInitSymbol = ((LVal) primaryExpInterface).getSymbol();  // 该参数原始的symbol
                    // ArrayList<Symbol> currFuncParam = currFunc.getParams();  // 主函数的参数
                    // if (currFuncParam.contains(lValInitSymbol)) {  // 如果调用函数时的实参恰好是主函数的形参
                    //     assert lValInitSymbol.getSymbolType() == SymbolType.ARRAY;  // 主函数形参需要是array类型的
                    //     // 主函数的形参本身就是一个指向数组的pointer了，需要将这个pointer从内存中取出来，传递给子函数
                    //     BlockNode lastBlock = currBlock.getLastContent();
                    //     assert lastBlock != null && lastBlock instanceof Memory;
                    //     Memory lastMemory = (Memory) lastBlock;
                    //     if(lValInitSymbol == lastMemory.getBase()){
                    //         Symbol temp = Symbol.tempSymbol(SymbolType.POINTER);
                    //         currBlock.addContent(new Pointer(Pointer.Op.LOAD, lVal, temp));  // 则该地址存的就是一个数组指针
                    //         return temp;
                    //     } else {
                    //         Symbol temp = Symbol.tempSymbol(SymbolType.POINTER);
                    //         currBlock.addContent(new Pointer(Pointer.Op.LOAD, lVal, temp));  // 则该地址存的就是一个数组指针
                    //         return temp;
                    //     }
                    // } else {
                    //     return lVal;
                    // }
                    return lVal;
                } else {
                    Symbol temp = Symbol.tempSymbol(SymbolType.INT);
                    currBlock.addContent(new Pointer(Pointer.Op.LOAD, lVal, temp));  // 取出数值并返回
                    return temp;
                }
            } else if (lVal.getSymbolType() == SymbolType.INT) {
                return lVal;
            } else {
                assert false;
            }
        } else if (primaryExpInterface instanceof Number) {
            return checkNumber((Number) primaryExpInterface);
        }
        // not output
        assert false;
        return null;
    }

    // BraceExp = '(' Exp ')'
    public Operand checkBraceExp(BraceExp braceExp, boolean returnPointer) {
        // check missing Right Parenthesis
        if (braceExp.missRightParenthesis()) {
            errors.add(new MissRparentException(braceExp.getLine()));
        }
        return checkExp(braceExp.getExp(), returnPointer);
    }

    // LVal → Ident {'[' Exp ']'}
    // TODO: 会给LVal Symbol赋值
    // TODO: WARNING!!! 当不在符号表中存在时会返回null
    public Symbol checkLVal(LVal lVal, boolean checkConst) {  // checkConst represents check const or not
        // 查符号表!!!
        Token ident = lVal.getIdent();
        Symbol symbol = currSymbolTable.getSymbol(ident.getContent(), true);
        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            return null;  // error  TODO: WARNING!!!: NOT EXIST IN SYMBOL TABLE
        }

        // check const
        if (checkConst && symbol.isConst()) {
            errors.add(new ModifyConstException(ident.getLine()));
        }

        // check missing RBrack
        if (lVal.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }
        // TODO: check LVal using right or not
        /*
         * int a;
         * b = a[1];
         *
         * int a[1][2][3];
         * b =  a[1];
         * */
        lVal.setSymbol(symbol);  // TODO: 可以在未来用来检查lVal是否正确使用
        // return symbol;

        /*
         * translate to middle code
         * */
        if (symbol.getSymbolType() == SymbolType.INT) {  // int变量
            return symbol;
        } else if (symbol.getSymbolType() == SymbolType.ARRAY) {  // 数组变量
            ArrayList<Integer> dimSize = symbol.getDimSize();
            ArrayList<Integer> suffix = new ArrayList<>();
            suffix.add(1);
            for (int i = dimSize.size() - 1; i > 0; i--) {
                suffix.add(0, suffix.get(0) * dimSize.get(i));
            }
            // ArrayList<Operand> place = lVal.getExps().stream().map(this::checkExp)  // 数组最多二维
            //         .collect(Collectors.toCollection(ArrayList::new));  // 每一维的取值
            ArrayList<Exp> placeExp = lVal.getExps();
            Operand offset = new Immediate(0);
            for (int i = placeExp.size() - 1; i >= 0; i--) {
                Operand weight = new Immediate(suffix.get(i) * 4);  // 后缀积
                Symbol mid = Symbol.tempSymbol(SymbolType.INT);
                Operand currPlace = checkExp(placeExp.get(i), false);
                currBlock.addContent(new FourExpr(currPlace, weight, mid, FourExpr.ExprOp.MUL));
                currBlock.addContent(new FourExpr(mid, offset, mid, FourExpr.ExprOp.ADD));
                offset = mid;
            }

            Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);  // 数组变量
            // TODO: WARNING!!! 返回数组指针，在后续需要根据用途做处理
            currBlock.addContent(new Memory(symbol, offset, ptr));  // return the pointer to array
            return ptr;
        }
        assert false;
        return null;
    }

    public Operand checkNumber(Number number) {
        return new Immediate(number.getNumber());
    }

    // 函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
    // 函数实参表 FuncRParams → Exp { ',' Exp }
    // 会给Func的ReturnType赋值
    // 当func不在符号表中存在时会返回null
    // 当Rparam不在符号表中时会返回FuncExp
    public Operand checkFuncExp(FuncExp funcExp) {
        // check missing RightParenthesis
        if (funcExp.missRightParenthesis()) {
            errors.add(new MissRparentException(funcExp.getLine()));
        }

        // 查符号表
        Token ident = funcExp.getIdent();
        Symbol symbol = currSymbolTable.getSymbol(ident.getContent(), true);
        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            return null;  // error  TODO: WARNING!!!: NOT EXIST IN SYMBOL TABLE
        }
        // if (!symbol.isFunc()) {
        //     return null;  // TODO: 帖子，什么错误类型？？
        // }
        assert symbol.isFunc();  // assert symbol is a function
        funcExp.setReturnType(symbol.getReturnType());  // 设置function的returnType  TODO: 可以在未来用来检查funcExp是否正确使用

        // 形参表
        ArrayList<Symbol> Fparams = symbol.getParams();

        // 实参表
        FuncRParams funcRParams = funcExp.getParams();
        ArrayList<Exp> RParamExp = new ArrayList<>();
        ArrayList<Operand> Rparams = new ArrayList<>();  // LeafNode is LVal or Number or funcExp
        if (funcRParams != null) {
            RParamExp = funcRParams.getExps();
            if (Fparams.size() != RParamExp.size()) {
                errors.add(new MismatchParamCountException(funcExp.getLine()));  // param count mismatch
                assert false : "参数个数不匹配";
            }
            for (int i = 0; i < Fparams.size(); i++) {
                Symbol fParam = Fparams.get(i);  // 形参
                Exp rParamExp = RParamExp.get(i);  // 实参
                Operand res;
                // TODO: 调用的函数的实参和主函数的形参做一个对比 在PrimaryExp中做了check!!!
                if (fParam.getSymbolType() == SymbolType.INT) {
                    res = checkExp(rParamExp, false);
                } else {
                    res = checkExp(rParamExp, true);  // 如果是数组则返回数组指针
                }
                if (res == null) {
                    assert false : "check";
                    return null;
                }
                Rparams.add(res);
            }  // check Exp会给所有的LVal和FuncExp设置SymbolType
        }

        // match check
        if (Fparams.size() != Rparams.size()) {
            errors.add(new MismatchParamCountException(funcExp.getLine()));  // param count mismatch
        } else {
            for (int i = 0; i < Fparams.size(); i++) {
                Symbol fParam = Fparams.get(i);
                Operand rParam = Rparams.get(i);

                if (rParam instanceof Immediate) {
                    if (fParam.getSymbolType() != SymbolType.INT) {
                        errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                        break;
                        // TODO: do what?
                    }
                } else {
                    Symbol rp = (Symbol) rParam;
                    if (fParam.getSymbolType() == SymbolType.ARRAY) {  // 形参是数组，对应实参应该是指针
                        if (rp.getSymbolType() != SymbolType.POINTER) {
                            errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                            break;
                        }
                    } else if (fParam.getSymbolType() == SymbolType.INT) {  // 形参是int，对应实参应该也是int
                        if (rp.getSymbolType() != SymbolType.INT) {
                            errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                            break;
                        }
                    } else {
                        assert false : "形参只能是数组或int";
                    }
                }

                // TODO: 需要检查维数和每一维的大小是否匹配
                // // 由于之前checkExp已经给每个LeafNode赋过SymbolType了，这里一定可以得到SymbolType
                // if (fParam.getSymbolType() != rParam.getSymbolType()) {  // param type mismatch
                //     errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                //     break;
                // }
                // if (fParam.getSymbolType() == SymbolType.ARRAY) {  // 检查数组参数的维数是否正确
                //     assert rParam instanceof LVal;  // 数组一定是LVal
                //     if (fParam.getDimCount() != rParam.getDimCount()) {  // 维数不正确
                //         errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                //         break;
                //     }
                //
                //     // 检查每一维的数值是否正确
                //     /*
                //      *  int f(int a[][2]) {}
                //      *  int main(){
                //      *       int a[2][3];
                //      *       f(a);  // mismatch!!!
                //      *  }
                //      * */
                //     ArrayList<Integer> fDimSize = fParam.getDimSize();
                //     ArrayList<Integer> rDimSize = rParam.getDimSize();
                //     for (int j = 1; j < fDimSize.size(); j++) {  // 跳过第一维
                //         if (!Objects.equals(fDimSize.get(j), rDimSize.get(j))) {
                //             errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                //             break;
                //         }
                //     }
                // }
            }
        }
        FuncBlock funcBlock = middleCode.getFunc(symbol.getName());
        if (funcBlock.getReturnType() == FuncBlock.ReturnType.INT) {
            Symbol res = Symbol.tempSymbol(SymbolType.INT);
            currBlock.addContent(new FuncCall(funcBlock, Rparams, res));
            return res;
        } else {
            currBlock.addContent(new FuncCall(funcBlock, Rparams));
            return new Immediate(0);
        }
    }

    // Cond → LOrExp
    // return Symbol
    public Operand checkCond(Cond cond) {
        return checkLOrExp(cond.getLOrExp());
    }

    // LOrExp → LAndExp {'||' LAndExp}
    // 短路求值
    // return Symbol
    public Operand checkLOrExp(LOrExp lOrExp) {
        BasicBlock lOrExpBlock = new BasicBlock("L_OR_EXP_" + blockCount++);
        currBlock.addContent(new Jump(lOrExpBlock));
        currBlock = lOrExpBlock;
        currBlock.setIndex(blockId++);
        Operand and = checkLAndExp(lOrExp.getFirstExp());
        ArrayList<LAndExp> andExps = lOrExp.getExps();
        BasicBlock orEnd = new BasicBlock("OR_END_" + blockCount++);
        if (andExps.size() == 0) {
            currBlock.addContent(new Jump(orEnd));
            currBlock = orEnd;
            currBlock.setIndex(blockId++);
            return and;
        }
        Symbol orMidRes = Symbol.tempSymbol(SymbolType.INT);
        currBlock.addContent(new FourExpr(and, orMidRes, FourExpr.ExprOp.ASS));
        BasicBlock falseBlock = new BasicBlock("OR_" + blockCount++);
        currBlock.addContent(new Branch(orMidRes, orEnd, falseBlock, false));

        currBlock = falseBlock;
        currBlock.setIndex(blockId++);
        for (LAndExp lAndExp : andExps) {
            and = checkLAndExp(lAndExp);
            // Symbol orMidRes = Symbol.tempSymbol(SymbolType.INT);  maybe we can use former temp var res
            currBlock.addContent(new FourExpr(and, orMidRes, orMidRes, FourExpr.ExprOp.OR));
            falseBlock = new BasicBlock("OR_" + blockCount++);
            currBlock.addContent(new Branch(orMidRes, orEnd, falseBlock, false));
            currBlock = falseBlock;
            currBlock.setIndex(blockId++);
        }
        currBlock.addContent(new Jump(orEnd));
        currBlock = orEnd;
        currBlock.setIndex(blockId++);
        return orMidRes;
    }

    // LAndExp → EqExp {'&&' EqExp}
    // 短路求值
    // return Symbol
    // TODO: 看看能不能省略midRes，直接根据Operand跳转
    public Operand checkLAndExp(LAndExp lAndExp) {
        BasicBlock lAndExpBlock = new BasicBlock("L_AND_EXP_" + blockCount++);
        currBlock.addContent(new Jump(lAndExpBlock));
        currBlock = lAndExpBlock;
        currBlock.setIndex(blockId++);
        ArrayList<EqExp> eqExps = lAndExp.getExps();
        Operand eq = checkEqExp(lAndExp.getFirstExp());
        BasicBlock andEnd = new BasicBlock("AND_END_" + blockCount++);
        if (eqExps.size() == 0) {
            currBlock.addContent(new Jump(andEnd));
            currBlock = andEnd;
            currBlock.setIndex(blockId++);
            return eq;
        }
        Symbol andMidRes = Symbol.tempSymbol(SymbolType.INT);
        currBlock.addContent(new FourExpr(eq, andMidRes, FourExpr.ExprOp.ASS));
        BasicBlock trueBlock = new BasicBlock("AND_" + blockCount++);
        currBlock.addContent(new Branch(eq, trueBlock, andEnd, true));

        currBlock = trueBlock;
        currBlock.setIndex(blockId++);
        for (EqExp eqExp : eqExps) {
            eq = checkEqExp(eqExp);
            currBlock.addContent(new FourExpr(eq, andMidRes, andMidRes, FourExpr.ExprOp.AND));
            trueBlock = new BasicBlock("AND_" + blockCount++);
            currBlock.addContent(new Branch(andMidRes, trueBlock, andEnd, true));
            currBlock = trueBlock;
            currBlock.setIndex(blockId++);
        }
        currBlock.addContent(new Jump(andEnd));
        currBlock = andEnd;
        currBlock.setIndex(blockId++);
        return andMidRes;
    }

    // EqExp → RelExp {('==' | '!=') RelExp}
    // 翻译成四元式
    // return Symbol
    public Operand checkEqExp(EqExp eqExp) {
        Operand left = checkRelExp(eqExp.getFirstExp());
        ArrayList<RelExp> relExps = eqExp.getExps();
        // ArrayList<LeafNode> nodes = eqExp.getExps().stream().map(this::checkRelExp)
        //         .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> seps = eqExp.getSeps();
        for (int i = 0; i < relExps.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.INT);
            Operand right = checkRelExp(relExps.get(i));
            if (seps.get(i).getType() == TokenType.EQL) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.EQ));
            } else if (seps.get(i).getType() == TokenType.NEQ) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.NEQ));
            } else {
                assert false;
            }
            left = temp;
        }
        return left;
    }

    // RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
    // 四元式表达
    // return Symbol
    public Operand checkRelExp(RelExp relExp) {
        Operand left = checkAddExp(relExp.getFirstExp(), false);
        ArrayList<AddExp> exps = relExp.getExps();
        // ArrayList<LeafNode> nodes = relExp.getExps().stream().map(this::checkAddExp)
        //         .collect(Collectors.toCollection(ArrayList::new));  // 一个一个算 要不然可能会造成寄存器的浪费和冲突
        ArrayList<Token> seps = relExp.getSeps();
        for (int i = 0; i < exps.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.INT);
            Operand right = checkAddExp(exps.get(i), false);
            if (seps.get(i).getType() == TokenType.LSS) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.LT));
            } else if (seps.get(i).getType() == TokenType.LEQ) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.LE));
            } else if (seps.get(i).getType() == TokenType.GRE) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.GT));
            } else if (seps.get(i).getType() == TokenType.GEQ) {
                currBlock.addContent(new FourExpr(left, right, temp, FourExpr.ExprOp.GE));
            } else {
                assert false;
            }
            left = temp;
        }
        return left;
    }
}

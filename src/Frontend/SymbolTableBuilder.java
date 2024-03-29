package Frontend;

import Config.SIPair;
import Config.SOPair;
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
import Frontend.Parser.expr.types.UnaryOp;
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
import Frontend.Symbol.Errors;
import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolTable;
import Frontend.Symbol.SymbolType;
import Frontend.Util.ConstExpCalculator;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.FuncCall;
import Middle.type.GetInt;
import Middle.type.Immediate;
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Operand;
import Middle.type.Pointer;
import Middle.type.PrintInt;
import Middle.type.PrintStr;
import Middle.type.Return;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private final SymbolTable topSymbolTable = currSymbolTable;
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

    // about Const
    private final HashMap<Symbol, Integer> nonModifiedInt = new HashMap<>();  // 没有更改过的INT

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
            checkDecl(decl);
        }

        // check func def
        for (FuncDef funcDef : funcDefs) {
            checkFunc(funcDef, false);
        }

        // check main func
        checkMainFunc(mainFunction);

        replaceNonModified();
    }

    public void replaceNonModified() {
        LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = middleCode.getFuncToSortedBlock();
        for (ArrayList<BasicBlock> basicBlocks : funcToSortedBlock.values()) {
            for (BasicBlock basicBlock : basicBlocks) {
                for (BlockNode blockNode : basicBlock.getContent()) {
                    if (blockNode instanceof FourExpr) {
                        FourExpr fourExpr = (FourExpr) blockNode;
                        if (fourExpr.isSingle()) {
                            if (nonModifiedInt.containsKey(fourExpr.getLeft())) {
                                fourExpr.setLeft(new Immediate(nonModifiedInt.get(fourExpr.getLeft())));
                            }
                        } else {
                            if (nonModifiedInt.containsKey(fourExpr.getLeft())) {
                                fourExpr.setLeft(new Immediate(nonModifiedInt.get(fourExpr.getLeft())));
                            }
                            if (nonModifiedInt.containsKey(fourExpr.getRight())) {
                                fourExpr.setRight(new Immediate(nonModifiedInt.get(fourExpr.getRight())));
                            }
                        }
                    } else if (blockNode instanceof PrintInt) {
                        PrintInt printInt = (PrintInt) blockNode;
                        if (nonModifiedInt.containsKey(printInt.getVal())) {
                            printInt.setVal(new Immediate(nonModifiedInt.get(printInt.getVal())));
                        }
                    }
                }
            }

        }
    }

    // Decl → ConstDecl | VarDecl
    // 变量声明 VarDecl → 'int' VarDef { ',' VarDef } ';'
    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public void checkDecl(Decl decl) {
        if (decl.missSemicolon()) {
            errors.add(new MissSemicnException(decl.getLine()));
        }
        Def def = decl.getDef();
        checkDef(def);
        ArrayList<Def> defs = decl.getDefs();
        for (Def ndef : defs) {
            checkDef(ndef);
        }
    }

    public void checkDef(Def def) {
        /*
         *  int a = a * a;
         * */
        if (def.getDimCount() == 0) {  // not an array; int or pointer
            if (def.hasInitVal()) {  // 已经初始化
                InitVal initVal = def.getInitVal();
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                if (symbol == null) return;
                if (initVal.isConst() || currFunc == null) {  // 初始化数值可以直接计算出结果
                    int val;
                    if (!initVal.isConst()) {
                        val = new ConstExpCalculator(currSymbolTable, errors, inlining, currentIndex,
                                funcSymbolTableMap.get(inlineFunc), topSymbolTable).calcExp(initVal.getExp());
                    } else {
                        val = new ConstExpCalculator(currSymbolTable, errors, inlining, currentIndex,
                                funcSymbolTableMap.get(inlineFunc), topSymbolTable).calcConstExp(initVal.getConstExp());
                    }
                    if (initVal.isConst()) {
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
                    nonModifiedInt.put(symbol, val);
                } else {  // 初始化数值不可以直接计算出结果，用FourExpr表示
                    Operand val;
                    if (initVal.isConst()) {
                        val = checkConstExp(initVal.getConstExp(), noCheck);
                        if (val == null) {
                            return;
                        }
                    } else {
                        val = checkExp(initVal.getExp(), noCheck);
                        if (val == null) {
                            return;
                        }
                    }
                    if (val instanceof Immediate) {
                        nonModifiedInt.put(symbol, ((Immediate) val).getNumber());
                    }
                    symbol.setAddress(currSymbolTable.getStackSize());
                    currBlock.addContent(new Middle.type.FourExpr(val, symbol, FourExpr.ExprOp.DEF));
                    symbol.setScope(Symbol.Scope.LOCAL);
                }
            } else {  // 没有初始化
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                if (symbol == null) return;
                if (currFunc == null) {  // pre decl, not in a function
                    symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                    middleCode.addInt(def.getVar().getIdent().getContent(), symbol.getAddress(), 0);
                    symbol.setScope(Symbol.Scope.GLOBAL);
                    if (symbol.isConst()) {
                        symbol.setConstInitInt(0);
                    }
                } else {  // decl in a function
                    symbol.setAddress(currSymbolTable.getStackSize());
                    currBlock.addContent(new Middle.type.FourExpr(new Immediate(0), symbol, FourExpr.ExprOp.DEF));
                    symbol.setScope(Symbol.Scope.LOCAL);
                    // 未初始化的局部变量
                }
            }
        } else {  // 数组
            if (def.hasInitVal()) {  // 已经初始化
                InitVal initVal = def.getInitVal();
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                if (symbol == null) return;
                ArrayList<AddExp> initExp = flatArrayInitVal(initVal);
                if (initVal.isConst() || currFunc == null) {  // 初始化数值可以直接计算出结果
                    ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors, inlining,
                            currentIndex, funcSymbolTableMap.get(inlineFunc), topSymbolTable);
                    ArrayList<Integer> initNum = initExp.stream().map(constExpCalculator::calcAddExp)
                            .collect(Collectors.toCollection(ArrayList::new));
                    if (symbol.isConst()) {
                        symbol.setConstInitArray(initNum);
                    }
                    if (currFunc == null) {  // 全局变量
                        symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                        middleCode.addArray(symbol.getName(), symbol.getAddress(), initNum);
                        symbol.setScope(Symbol.Scope.GLOBAL);
                    } else {  // 局部变量
                        int offset = 0;
                        for (Integer num : initNum) {
                            // 局部数组
                            currBlock.addContent(
                                    new Pointer(Pointer.Op.STORE, symbol, new Immediate(offset * 4), new Immediate(num)));
                            offset++;
                        }
                        symbol.setAddress(currSymbolTable.getStackSize());
                        symbol.setScope(Symbol.Scope.LOCAL);
                    }
                } else {  // 初始化数值不可以直接计算出结果，用FourExpr表示
                    int offset = 0;
                    for (AddExp addExp : initExp) {
                        Operand exp = checkAddExp(addExp, noCheck);
                        if (exp == null) return;
                        Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                        // 局部数组
                        currBlock.addContent(new Pointer(Pointer.Op.STORE, symbol, new Immediate(offset * 4), exp));
                        offset++;
                    }
                    symbol.setAddress(currSymbolTable.getStackSize());
                    symbol.setScope(Symbol.Scope.LOCAL);
                }
            } else {  // 没有初始化
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                if (symbol == null) return;
                if (currFunc == null) {
                    ArrayList<Integer> initZero = new ArrayList<>();
                    int totalCount = symbol.getSize() / 4;  // 数组应该初始化为多少个零
                    for (int i = 0; i < totalCount; i++) initZero.add(0);
                    symbol.setAddress(currSymbolTable.getStackSize() - symbol.getSize());
                    middleCode.addArray(symbol.getName(), symbol.getAddress(), initZero);
                    symbol.setScope(Symbol.Scope.GLOBAL);
                    if (symbol.isConst()) {
                        symbol.setConstInitArray(initZero);
                    }
                } else {
                    symbol.setAddress(currSymbolTable.getStackSize());
                    symbol.setScope(Symbol.Scope.LOCAL);
                    // nothing to do
                    // 函数中的局部变量，没有初始化
                }
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
        if (!inlining && currSymbolTable.contains(ident.getContent(), false)) {
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
        ConstExpCalculator dimSizeCalculator = new ConstExpCalculator(currSymbolTable, errors, inlining, currentIndex,
                funcSymbolTableMap.get(inlineFunc), topSymbolTable);
        for (ConstExp constExp : dimExp) {
            dimSize.add(dimSizeCalculator.calcConstExp(constExp));
            // TODO: put error check in calcUti. DONE!
            // checkConstExp(constExp);
        }
        var.setDimSize(dimSize);

        // add to symbol table
        if (!redefine) {
            Symbol symbol = new Symbol(var.getDimCount() == 0 ? SymbolType.INT : SymbolType.ARRAY,
                    transferIdent(ident.getContent()).getString(), dimSize, var.getDimCount(), var.isConst(), null);  // checkVal没有设置scope
            if (inlining) {
                symbol.setInlining(currFunc, inlineFunc, currentIndex);
            }
            currSymbolTable.addSymbol(symbol);  // 会同时为Symbol申请空间
            // symbol.setAddress(currSymbolTable.getStackSize());  // setAddress移动到调用LVal的函数中做，便于根据GLOBAL or LOCAL设定不同的address
            return symbol;
        } else {
            return null;
        }
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public void checkFunc(FuncDef funcDef, boolean isMainFunc) {
        currFuncType = funcDef.getReturnType().getType();
        Token ident = funcDef.getIdent();
        // check redefine
        boolean redefine = false;
        if (!inlining && currSymbolTable.contains(ident.getContent(), false)) {
            errors.add(new RedefinedTokenException(ident.getLine()));
            redefine = true;  //  TODO: stop here or not?
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

        // check return right or not
        // 函数return int， 但是没有return语句
        if (returnType.getType() == TokenType.INTTK && !funcDef.returnInt()) {
            errors.add(new MissReturnException(funcDef.getRightBrace().getLine()));
        }

        // 给末尾没有return语句的函数加return(直接在所有函数结尾无脑加jr $ra)
        BlockStmt funcBody = funcDef.getBlockStmt();
        if (funcBody.getReturn() == null) {
            if (returnType.getType() == TokenType.VOIDTK) {
                funcBody.getBlockItems()
                        .add(new Stmt(new ReturnStmt(Token.tempToken(TokenType.RETURNTK, funcBody.getRightBrace().getLine()))));
            } else {
                funcBody.getBlockItems()
                        .add(new Stmt(new ReturnStmt(Token.tempToken(TokenType.RETURNTK, funcBody.getRightBrace().getLine()))));
            }

        }

        // check func block
        BasicBlock body = checkBlockStmt(funcDef.getBlockStmt(), true, funcBlock.getLabel());

        currFunc.setBody(body);
        funcBlockDefMap.put(currFunc, funcDef);


        funcSymbolTableMap.put(currFunc, currSymbolTable);
        currSymbolTable = currSymbolTable.getParent();  // 上升一层
        currFunc = null;  // 上升一层
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
        if (!inlining && currSymbolTable.contains(ident.getContent(), false)) {
            redefine = true;
            errors.add(new RedefinedTokenException(ident.getLine()));
        }

        // check miss RBrack
        if (funcFParam.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }

        // add to symbol table
        // 函数参数每一个的大小都是4
        // if (!redefine) {
        if (funcFParam.isArray()) {
            ArrayList<ConstExp> dimExp = funcFParam.getDimExp();
            ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors, inlining, currentIndex,
                    funcSymbolTableMap.get(inlineFunc), topSymbolTable);
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
        // } else {
        //     assert false : "TO BE FIXED: 函数参数重名";
        //     return null;
        // }
    }

    // 语句块项 BlockItem → Decl | Stmt
    public void checkBlockItem(BlockItem blockItem) {
        if (blockItem instanceof Decl) {
            checkDecl((Decl) blockItem);
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
        SOPair soPair = checkLVal(assignStmt.getLVal(), true, noCheck);
        if (soPair == null) {  // 符号表中不存在
            return;
        }
        Operand operand = checkExp(assignStmt.getExp(), noCheck);
        if (operand == null) {
            return;
        }
        if (soPair.isArray()) {  // 如果返回的是指针，直接存在内存里
            currBlock.addContent(new Pointer(Pointer.Op.STORE, soPair.getBase(), soPair.getOffset(), operand));  // 数组存内存
            nonModifiedInt.remove(soPair.getBase());
        } else {  // int
            assert soPair.getOffset() instanceof Symbol;
            Symbol lVal = (Symbol) soPair.getOffset();
            // currBlock.addContent(new FourExpr(operand, lVal, FourExpr.ExprOp.ASS));
            ArrayList<BlockNode> currContent = currBlock.getContent();
            if (currContent.size() == 0) {
                currBlock.addContent(new FourExpr(operand, lVal, FourExpr.ExprOp.ASS));
            } else {
                BlockNode lastNode = currContent.get(currContent.size() - 1);
                if (lastNode instanceof FourExpr && ((FourExpr) lastNode).getRes() == operand && combinedOps.contains(
                        ((FourExpr) lastNode).getOp()) && !((FourExpr) lastNode).replaced()) {
                    ((FourExpr) lastNode).setRes(lVal);
                    ((FourExpr) lastNode).setReplaced();
                } else if (lastNode instanceof Pointer && ((Pointer) lastNode).isLoad() && ((Pointer) lastNode).getLoad() == operand) {
                    ((Pointer) lastNode).setLoad(lVal);
                } else {
                    currBlock.addContent(new FourExpr(operand, lVal, FourExpr.ExprOp.ASS));
                }
            }
            nonModifiedInt.remove(lVal);
        }
        return;
    }

    private HashSet<FourExpr.ExprOp> combinedOps = new HashSet<>(
            Arrays.asList(FourExpr.ExprOp.ADD, FourExpr.ExprOp.MUL, FourExpr.ExprOp.DIV, FourExpr.ExprOp.SUB));


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
        } else {
            BasicBlock nextBlock = whileNext.peek();
            currBlock.addContent(new Jump(nextBlock));
        }
    }

    // continue;
    public void checkContinueStmt(ContinueStatement continueStatement) {
        if (loopDepth == 0) {
            errors.add(new IllegalBreakContinueException(continueStatement.getSemicolonLine()));
        } else {
            BasicBlock headBlock = whileHead.peek();
            currBlock.addContent(new Jump(headBlock));
        }
    }

    // Exp ';'
    public void checkExpStmt(ExpStmt expStmt) {
        checkExp(expStmt.getExp(), noCheck);
    }

    // LVal '=' 'getint''('')'
    // TODO: check LVal using right or not（LVal是否正确使用，和Exp是否匹配，int）
    public void checkGetIntStmt(GetIntStmt getIntStmt) {
        // check miss Right Parenthesis
        if (getIntStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(getIntStmt.getLine()));
        }
        // check LVal, and LVal could not be const
        SOPair soPair = checkLVal(getIntStmt.getLVal(), true, noCheck);
        if (soPair == null) {  // 在符号表中不存在
            return;
        }
        if (soPair.isArray()) {
            nonModifiedInt.remove(soPair.getBase());
        } else {
            nonModifiedInt.remove((Symbol) soPair.getOperand());
        }
        // TODO: WARNING!!! GetInt会根据global local temp来设计sw, lw指令，在translateGetInt
        currBlock.addContent(new GetInt(soPair));
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public void checkIfStmt(IfStmt ifStmt) {
        // check cond Stmt
        BasicBlock ifBody = new BasicBlock("IF_BODY_" + blockCount++);
        BasicBlock ifElse = new BasicBlock("IF_ELSE_" + blockCount++);
        BasicBlock ifEnd = new BasicBlock("IF_END_" + blockCount++);
        checkCond(ifStmt.getCond(), ifBody, ifStmt.hasElse() ? ifElse : ifEnd);
        if (ifStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(ifStmt.getIfLine()));
        }
        // 通过ID保证中间代码和mips代码中基本块的顺序和遍历顺序一致
        currSymbolTable = new SymbolTable(currSymbolTable, currFunc.getFuncSymbolTable());  // 下降一层

        // currBlock.addContent(new Jump());
        // ArrayList<Stmt> stmts = ifStmt.getStmts();
        if (ifStmt.hasElse()) {
            currBlock = ifBody;
            ifBody.setIndex(blockId++);
            checkStmt(ifStmt.getStmts().get(0));
            currBlock.addContent(new Jump(ifEnd));
            currBlock = ifElse;
            ifElse.setIndex(blockId++);
            checkStmt(ifStmt.getStmts().get(1));
            currBlock.addContent(new Jump(ifEnd));
        } else {
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
            printExps.add(checkExp(exp, noCheck));
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
        // assert currFunc != null;  // 不在Decl区
        if (!inlining) {
            if (currFuncType == TokenType.VOIDTK && returnStmt.getReturnExp() != null) {
                errors.add(new IllegalReturnException(returnStmt.getReturnToken().getLine()));
            }
            if (returnStmt.getReturnExp() != null) {
                Operand returnVal = checkExp(returnStmt.getReturnExp(), noCheck);
                currBlock.addContent(new Return(returnVal));
            } else {
                currBlock.addContent(new Return());
            }
        } else {
            if (returnStmt.getReturnExp() != null) {
                Operand returnVal = checkExp(returnStmt.getReturnExp(), noCheck);
                currBlock.addContent(new FourExpr(returnVal, this.returnVal, FourExpr.ExprOp.ASS));
                currBlock.addContent(new Jump(inlineEnd));
            }
        }
        // return int是否正确在checkFunc中检查
    }

    // 'while' '(' Cond ')' Stmt
    public void checkWhileStmt(WhileStmt whileStmt) {
        loopDepth++;
        // TODO: 20221125 my birthday!!!  循环优化 while -> do-while
        /*
         * while( cond )
         * jump whileEnd
         * {
         *     stmt
         *     jump whileBlock
         * }
         * 2n次jump
         * ========================
         * if( cond )  [ifBlock]
         * {
         *   jump whileEnd
         *   do{        [doBody]
         *      } while(cond)  [doCheckBlock]
         *   jump whileBlock(doBlock)
         * }
         * [whileEnd]
         * n+1次jump
         * */


        // check missing Right Parenthesis
        if (whileStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(whileStmt.getLine()));
        }

        // 通过ID保证中间代码和mips代码中基本块的顺序和遍历顺序一致
        BasicBlock doBlock = new BasicBlock("DO_BODY_" + blockCount++);
        BasicBlock doCheckBlock = new BasicBlock("DO_CHECK_" + blockCount++);
        BasicBlock doEnd = new BasicBlock("DO_END_" + blockCount++);

        // check Cond
        checkCond(whileStmt.getCond(), doBlock, doEnd);

        // for break and continue
        whileHead.push(doCheckBlock);
        whileNext.push(doEnd);

        // step into doBlock
        currBlock = doBlock;
        currBlock.setIndex(blockId++);

        currSymbolTable = new SymbolTable(currSymbolTable, currFunc.getFuncSymbolTable());  // 下降一层
        // check Stmt
        checkStmt(whileStmt.getStmt());
        // step into doCheckBlock
        currBlock.addContent(new Jump(doCheckBlock));
        currBlock = doCheckBlock;
        currBlock.setIndex(blockId++);
        currSymbolTable = currSymbolTable.getParent();  // 上升一层
        loopDepth--;
        whileNext.pop();
        whileHead.pop();


        checkCond(whileStmt.getCond(), doBlock, doEnd);
        currBlock = doEnd;
        currBlock.setIndex(blockId++);
    }

    public Operand checkConstExp(ConstExp constExp, int checkType) {
        return checkAddExp(constExp.getAddExp(), checkType);
    }

    public Operand checkExp(Exp exp, int checkType) {
        return checkAddExp(exp.getAddExp(), checkType);
    }

    // 四元式表达
    // AddExp → MulExp {('+' | '−') MulExp}
    public Operand checkAddExp(AddExp addExp, int checkType) {
        Operand mulLeft = checkMulExp(addExp.getFirstExp(), checkType);
        if (mulLeft == null) {
            return null;
        }
        ArrayList<MulExp> mulExps = addExp.getExps();
        ArrayList<Token> seps = addExp.getSeps();
        if (seps.size() == 0) {
            return mulLeft;
        }

        int index = 0;
        Operand mulRight = null;
        while (index < mulExps.size()) {
            mulRight = checkMulExp(mulExps.get(index), noCheck);
            if (mulRight == null) {
                return null;
            }
            if (mulLeft instanceof Immediate && mulRight instanceof Immediate) {
                if (seps.get(index).getType() == TokenType.PLUS) {
                    mulLeft = new Immediate(((Immediate) mulLeft).getNumber() + ((Immediate) mulRight).getNumber());
                } else if (seps.get(index).getType() == TokenType.MINU) {
                    mulLeft = new Immediate(((Immediate) mulLeft).getNumber() - ((Immediate) mulRight).getNumber());
                }
            } else {
                break;
            }
            index++;
        }

        if (index == mulExps.size()) {
            return mulLeft;
        }
        Symbol tempSymbol = Symbol.tempSymbol(SymbolType.INT);
        if (seps.get(index).getType() == TokenType.PLUS) {
            currBlock.addContent(new FourExpr(mulLeft, mulRight, tempSymbol, FourExpr.ExprOp.ADD));
        } else if (seps.get(index).getType() == TokenType.MINU) {
            currBlock.addContent(new FourExpr(mulLeft, mulRight, tempSymbol, FourExpr.ExprOp.SUB));
        }
        index++;

        while (index < mulExps.size()) {
            Symbol midRes = Symbol.tempSymbol(SymbolType.INT);
            mulRight = checkMulExp(mulExps.get(index), noCheck);
            if (mulRight == null) {
                return null;
            }
            if (seps.get(index).getType() == TokenType.PLUS) {
                currBlock.addContent(new FourExpr(tempSymbol, mulRight, midRes, FourExpr.ExprOp.ADD));
            } else if (seps.get(index).getType() == TokenType.MINU) {
                currBlock.addContent(new FourExpr(tempSymbol, mulRight, midRes, FourExpr.ExprOp.SUB));
            }
            index++;
            tempSymbol = midRes;
        }
        return tempSymbol;
    }

    // MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
    public Operand checkMulExp(MulExp mulExp, int checkType) {
        Operand unaryLeft = checkUnaryExp(mulExp.getFirstExp(), checkType);
        if (unaryLeft == null) {  // 在符号表中不存在
            return null;
        }
        ArrayList<UnaryExp> unaryExps = mulExp.getExps();
        ArrayList<Token> seps = mulExp.getSeps();
        if (seps.size() == 0) {
            return unaryLeft;
        }

        int index = 0;
        Operand unaryRight = null;
        while (index < unaryExps.size()) {
            unaryRight = checkUnaryExp(unaryExps.get(index), noCheck);
            if (unaryRight == null) {  // 在符号表中不存在
                return null;
            }
            if (unaryLeft instanceof Immediate && unaryRight instanceof Immediate) {
                if (seps.get(index).getType() == TokenType.MULT) {
                    unaryLeft = new Immediate(((Immediate) unaryLeft).getNumber() * ((Immediate) unaryRight).getNumber());
                } else if (seps.get(index).getType() == TokenType.DIV) {
                    unaryLeft = new Immediate(((Immediate) unaryLeft).getNumber() / ((Immediate) unaryRight).getNumber());
                } else if (seps.get(index).getType() == TokenType.MOD) {
                    unaryLeft = new Immediate(((Immediate) unaryLeft).getNumber() % ((Immediate) unaryRight).getNumber());
                }
            } else {
                break;
            }
            index++;
        }

        if (index == unaryExps.size()) {
            return unaryLeft;
        }
        Symbol tempSymbol = Symbol.tempSymbol(SymbolType.INT);
        if (seps.get(index).getType() == TokenType.MULT) {
            currBlock.addContent(new FourExpr(unaryLeft, unaryRight, tempSymbol, FourExpr.ExprOp.MUL));
        } else if (seps.get(index).getType() == TokenType.DIV) {
            currBlock.addContent(new FourExpr(unaryLeft, unaryRight, tempSymbol, FourExpr.ExprOp.DIV));
        } else if (seps.get(index).getType() == TokenType.MOD) {
            currBlock.addContent(new FourExpr(unaryLeft, unaryRight, tempSymbol, FourExpr.ExprOp.MOD));
        }
        index++;

        while (index < unaryExps.size()) {
            unaryRight = checkUnaryExp(unaryExps.get(index), noCheck);
            if (unaryRight == null) {  // 在符号表中不存在
                return null;
            }
            Symbol midRes = Symbol.tempSymbol(SymbolType.INT);
            if (seps.get(index).getType() == TokenType.MULT) {
                currBlock.addContent(new FourExpr(tempSymbol, unaryRight, midRes, FourExpr.ExprOp.MUL));
            } else if (seps.get(index).getType() == TokenType.DIV) {
                currBlock.addContent(new FourExpr(tempSymbol, unaryRight, midRes, FourExpr.ExprOp.DIV));
            } else if (seps.get(index).getType() == TokenType.MOD) {
                currBlock.addContent(new FourExpr(tempSymbol, unaryRight, midRes, FourExpr.ExprOp.MOD));
            }
            index++;
            tempSymbol = midRes;
        }

        return tempSymbol;
    }


    public Operand checkUnaryExp(UnaryExp unaryExp, int checkType) {
        UnaryExpInterface unaryExpInterface = unaryExp.getUnaryExpInterface();
        UnaryOp unaryOp = unaryExp.getOp();
        if (unaryExpInterface instanceof FuncExp) {
            return checkFuncExp((FuncExp) unaryExpInterface, checkType);
        } else if (unaryExpInterface instanceof PrimaryExp) {
            return checkPrimaryExp((PrimaryExp) unaryExpInterface, checkType);
        } else {
            assert unaryOp != null;
            Operand midRes = checkUnaryExp((UnaryExp) unaryExpInterface, checkType);
            if (midRes == null) {
                return null;
            }
            if (checkType == checkFuncArray1 || checkType == checkFuncArray2) {
                errors.add(new MismatchParamTypeException(funcExpLine));
            }
            if (unaryOp.getToken().getType() == TokenType.PLUS) {
                return midRes;
            }
            if (midRes instanceof Immediate) {
                if (unaryOp.getToken().getType() == TokenType.MINU) {
                    return new Immediate(-((Immediate) midRes).getNumber());
                } else if (unaryOp.getToken().getType() == TokenType.NOT) {
                    return new Immediate(((Immediate) midRes).getNumber() == 0 ? 1 : 0);
                }
            }
            Symbol tempSymbol = Symbol.tempSymbol(SymbolType.INT);
            if (unaryOp.getToken().getType() == TokenType.MINU) {
                currBlock.addContent(new FourExpr(midRes, tempSymbol, FourExpr.ExprOp.NEG));
            } else if (unaryOp.getToken().getType() == TokenType.NOT) {
                currBlock.addContent(new FourExpr(midRes, tempSymbol, FourExpr.ExprOp.NOT));
            }
            return tempSymbol;
        }
    }

    public Operand checkPrimaryExp(PrimaryExp primaryExp, int checkType) {
        return checkPrimaryExpInterFace(primaryExp.getPrimaryExpInterface(), checkType);
    }

    // TODO: WARNING!!! 只有在解析函数调用的参数时，returnPointer才是true！！！！returnPointer可以作为解析函数参数的表征
    public Operand checkPrimaryExpInterFace(PrimaryExpInterface primaryExpInterface, int checkType) {
        if (primaryExpInterface instanceof BraceExp) {
            return checkBraceExp((BraceExp) primaryExpInterface, checkType);
        } else if (primaryExpInterface instanceof LVal) {
            // TODO: check LVal using right or not（LVal是否正确使用，和Exp是否匹配，int）
            SOPair soPair = checkLVal((LVal) primaryExpInterface, false, checkType);
            if (soPair == null) {  // 在符号表中不存在
                return null;
            }
            if (checkType == checkFuncArray1 || checkType == checkFuncArray2) {
                Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                currBlock.addContent(new Memory(soPair.getBase(), soPair.getOffset(), ptr));
                return ptr;
            }
            if (soPair.isArray()) {
                Symbol temp = Symbol.tempSymbol(SymbolType.INT);
                currBlock.addContent(new Pointer(Pointer.Op.LOAD, soPair.getBase(), soPair.getOffset(), temp));
                return temp;
            } else {
                return soPair.getOperand();
            }
        } else if (primaryExpInterface instanceof Number) {
            return checkNumber((Number) primaryExpInterface, checkType);
        }
        // not output
        assert false;
        return null;
    }

    // BraceExp = '(' Exp ')'
    public Operand checkBraceExp(BraceExp braceExp, int checkType) {
        // check missing Right Parenthesis
        if (braceExp.missRightParenthesis()) {
            errors.add(new MissRparentException(braceExp.getLine()));
        }
        return checkExp(braceExp.getExp(), checkType);
    }

    // LVal → Ident {'[' Exp ']'}
    // TODO: 会给LVal Symbol赋值
    // TODO: WARNING!!! 当不在符号表中存在时会返回null
    // checkConst represents check const or not
    public SOPair checkLVal(LVal lVal, boolean checkConst, int checkType) {
        // 查符号表!!!
        Token ident = lVal.getIdent();
        SIPair siPair = transferIdent(ident.getContent());
        Symbol symbol;
        if (siPair.getInteger() == origin || siPair.getInteger() == inlineLocal) {
            symbol = currSymbolTable.getSymbol(siPair.getString(), true);
        } else {
            symbol = topSymbolTable.getSymbol(siPair.getString(), false);
        }
        if (symbol == null && siPair.getInteger() == inlineLocal) {
            symbol = topSymbolTable.getSymbol(ident.getContent(), false);
        }

        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            return null;  // error  TODO: WARNING!!!: NOT EXIST IN SYMBOL TABLE
        }

        // check const
        if (checkConst && symbol.isConst()) {
            errors.add(new ModifyConstException(ident.getLine()));
            return null;
        }

        // check missing RBrack
        if (lVal.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }

        lVal.setSymbol(symbol);  // TODO: 可以在未来用来检查lVal是否正确使用
        // return symbol;

        /*
         * translate to middle code
         * */
        if (symbol.getSymbolType() == SymbolType.INT) {  // int变量
            if (symbol.isConst()) {
                return new SOPair(new Immediate(symbol.getInitInt()));
            }
            return new SOPair(symbol);
        } else if (symbol.getSymbolType() == SymbolType.ARRAY) {  // 数组变量
            ArrayList<Integer> dimSize = symbol.getDimSize();
            ArrayList<Integer> suffix = new ArrayList<>();
            suffix.add(1);
            for (int i = dimSize.size() - 1; i > 0; i--) {
                suffix.add(0, suffix.get(0) * dimSize.get(i));
            }

            ArrayList<Exp> placeExp = lVal.getExps();
            // check
            boolean flag = true;
            if (checkType == checkFuncInt) {
                flag = placeExp.size() == dimSize.size();
            } else if (checkType == checkFuncArray1) {
                flag = dimSize.size() - placeExp.size() == 1;
            } else if (checkType == checkFuncArray2) {
                flag = dimSize.size() - placeExp.size() == 2 && dimSize.get(1) == arrayNum;
            } else {
                flag = true;
            }
            if (!flag) {
                errors.add(new MismatchParamTypeException(funcExpLine));
            }

            try {
                ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors, inlining,
                        currentIndex, funcSymbolTableMap.get(inlineFunc), topSymbolTable);
                ArrayList<Integer> placeInteger = placeExp.stream().map(constExpCalculator::calcExp)
                        .collect(Collectors.toCollection(ArrayList::new));
                int place = 0;
                // placeExp从后往前计算
                for (int i = placeInteger.size() - 1; i >= 0; i--) {
                    place += placeInteger.get(i) * suffix.get(i);
                }
                if (symbol.isConst() && (checkType == noCheck || checkType == checkFuncInt)) {
                    return new SOPair(new Immediate(symbol.getConstInitArray().get(place)));
                } else {
                    return new SOPair(symbol, new Immediate(place * 4));
                }
            } catch (MyAssert ignored) {

            }

            Operand offset = new Immediate(0);
            for (int i = placeExp.size() - 1; i >= 0; i--) {
                Operand weight = new Immediate(suffix.get(i) * 4);  // 后缀积
                Symbol mid = Symbol.tempSymbol(SymbolType.INT);
                Operand currPlace = checkExp(placeExp.get(i), noCheck);
                currBlock.addContent(new FourExpr(currPlace, weight, mid, FourExpr.ExprOp.MUL));
                currBlock.addContent(new FourExpr(mid, offset, mid, FourExpr.ExprOp.ADD));
                offset = mid;
            }

            // Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);  // 数组变量
            // // TODO: WARNING!!! 返回数组指针，在后续需要根据用途做处理
            // currBlock.addContent(new Memory(symbol, offset, ptr));  // return the pointer to array
            return new SOPair(symbol, offset);
        }
        assert false;
        return null;
    }

    public Operand checkNumber(Number number, int checkType) {
        if (checkType == checkFuncArray1 || checkType == checkFuncArray2) {
            errors.add(new MismatchParamTypeException(funcExpLine));
        }
        return new Immediate(number.getNumber());
    }

    private final int noCheck = 0;
    private final int checkFuncArray1 = 1;
    private final int checkFuncArray2 = 2;
    private final int checkFuncInt = 3;
    private int arrayNum;
    private int funcExpLine;

    // 函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
    // 函数实参表 FuncRParams → Exp { ',' Exp }
    // 会给Func的ReturnType赋值
    // 当func不在符号表中存在时会返回null
    // 当Rparam不在符号表中时会返回FuncExp
    public Operand checkFuncExp(FuncExp funcExp, int checkType) {
        // check missing RightParenthesis
        if (funcExp.missRightParenthesis()) {
            errors.add(new MissRparentException(funcExp.getLine()));
        }

        // 查符号表
        Token ident = funcExp.getIdent();
        // 函数符号
        // TODO: 1127递归函数
        if (currFunc != null && ident.getContent().equals(currFunc.getFuncName())) {
            currFunc.setRecursive();
        }
        SIPair siPair = transferIdent(ident.getContent());
        Symbol symbol;
        if (siPair.getInteger() == origin || siPair.getInteger() == inlineLocal) {
            symbol = currSymbolTable.getSymbol(siPair.getString(), true);
        } else {
            symbol = topSymbolTable.getSymbol(siPair.getString(), false);
        }
        if (symbol == null && siPair.getInteger() == inlineLocal) {  // important再找一次
            symbol = topSymbolTable.getSymbol(ident.getContent(), false);
        }
        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            return null;
        }
        assert symbol.isFunc();  // assert symbol is a function

        funcExp.setReturnType(symbol.getReturnType());  // 设置function的returnType  TODO: 可以用来检查funcExp是否正确使用
        if ((checkType == checkFuncInt && funcExp.getReturnType() == SymbolType.VOID) || checkType == checkFuncArray1 || checkType == checkFuncArray2) {
            errors.add(new MismatchParamTypeException(funcExpLine));
        }
        // 形参表
        ArrayList<Symbol> Fparams = symbol.getParams();

        // 实参表
        FuncRParams funcRParams = funcExp.getParams();

        ArrayList<Operand> Rparams = new ArrayList<>();  // LeafNode is LVal or Number or funcExp

        if (funcRParams != null) {  // 有实参
            ArrayList<Exp> RParamExp = funcRParams.getExps();
            if (Fparams.size() != RParamExp.size()) {
                errors.add(new MismatchParamCountException(funcExp.getLine()));  // param count mismatch
                return null;
            }
            for (int i = 0; i < Fparams.size(); i++) {
                Symbol fParam = Fparams.get(i);  // 形参
                Exp rParamExp = RParamExp.get(i);  // 实参
                Operand res = null;
                int formerError = errors.getErrorCount();
                if (fParam.getSymbolType() == SymbolType.INT) {
                    funcExpLine = funcExp.getLine();
                    res = checkExp(rParamExp, checkFuncInt);
                } else if (fParam.getSymbolType() == SymbolType.ARRAY) {
                    ArrayList<Integer> dimSize = fParam.getDimSize();
                    if (dimSize.size() == 1) {
                        funcExpLine = funcExp.getLine();
                        res = checkExp(rParamExp, checkFuncArray1);
                    } else if (dimSize.size() == 2) {
                        arrayNum = dimSize.get(1);
                        funcExpLine = funcExp.getLine();
                        res = checkExp(rParamExp, checkFuncArray2);
                    } else {
                        assert false;
                    }
                } else {
                    assert false;
                }
                if (errors.getErrorCount() != formerError) {
                    return new Immediate(-20231164);
                }
                Rparams.add(res);
            }  // check Exp会给所有的LVal和FuncExp设置SymbolType
        } else {  // 无实参
            if (Fparams.size() != 0) {
                errors.add(new MismatchParamCountException(funcExp.getLine()));
                return new Immediate(-20231164);
            }
        }

        FuncBlock funcBlock = middleCode.getFunc(symbol.getName());
        // 调用的函数块
        if (funcBlock.isRecursive()) {
            if (funcBlock.getReturnType() == FuncBlock.ReturnType.INT) {
                Symbol res = Symbol.tempSymbol(SymbolType.INT);
                currBlock.addContent(new FuncCall(funcBlock, Rparams, res));
                return res;
            } else {
                currBlock.addContent(new FuncCall(funcBlock, Rparams));
                return new Immediate(0);
            }
        } else {
            Symbol formerReturnVal = returnVal;
            boolean formerInlining = inlining;
            BasicBlock formerInlineEnd = inlineEnd;
            FuncBlock formerInlineFunc = inlineFunc;
            int formerIndex = currentIndex;

            inlining = true;
            currentIndex = functionInlineIndex++;
            inlineEnd = new BasicBlock("INLINE_" + funcBlock.getFuncName() + "_END_" + currentIndex);
            inlineFunc = funcBlock;

            // 参数
            for (int i = 0; i < Rparams.size(); i++) {
                Symbol fparam = Fparams.get(i);
                Operand rParam = Rparams.get(i);
                Symbol mappedSymbol = fparam.clone(transferIdent(fparam.getName()).getString(), currFunc, funcBlock,
                        currentIndex);
                currSymbolTable.addSymbol(mappedSymbol);
                mappedSymbol.setAddress(currSymbolTable.getStackSize());
                if (fparam.getSymbolType() == SymbolType.ARRAY) {
                    currBlock.addContent(new FourExpr(rParam, mappedSymbol, FourExpr.ExprOp.ASS));
                    ((Symbol) rParam).setAddress(mappedSymbol.getAddress());
                    currBlock.addContent(new Pointer(Pointer.Op.STORE, Symbol.tempSymbol(SymbolType.SPECIAL_SP),
                            new Immediate(-mappedSymbol.getAddress()), rParam));
                    mappedSymbol.setMappedSymbol((Symbol) rParam);
                } else {
                    currBlock.addContent(new FourExpr(rParam, mappedSymbol, FourExpr.ExprOp.ASS));
                }
            }

            // TODO: 非递归函数 inline!!!
            if (funcBlock.getReturnType() == FuncBlock.ReturnType.INT) {
                returnVal = Symbol.tempSymbol(SymbolType.INT);
            } else {
                returnVal = null;
            }

            // 内联
            checkBlockStmt(funcBlockDefMap.get(funcBlock).getBlockStmt(), true,
                    "INLINE_" + funcBlock.getFuncName() + "_BEGIN_" + currentIndex);

            currBlock.addContent(new Jump(inlineEnd));
            inlineEnd.setIndex(blockId++);
            currBlock = inlineEnd;

            // 恢复调用前状态
            Symbol res = returnVal;
            returnVal = formerReturnVal;
            inlining = formerInlining;
            inlineEnd = formerInlineEnd;
            inlineFunc = formerInlineFunc;
            currentIndex = formerIndex;
            if (funcBlock.getReturnType() == FuncBlock.ReturnType.INT) {
                return res;
            } else {
                return new Immediate(0);
            }
        }
    }

    // about function inline
    private final HashMap<FuncBlock, FuncDef> funcBlockDefMap = new HashMap<>();
    private final HashMap<FuncBlock, SymbolTable> funcSymbolTableMap = new HashMap<>();
    private Symbol returnVal = null;
    private int functionInlineIndex = 1;
    private int currentIndex = functionInlineIndex;
    private boolean inlining = false;
    private BasicBlock inlineEnd = null;
    private FuncBlock inlineFunc = null;

    private static final int origin = 0;
    private static final int inlineLocal = 1;
    private static final int global = 2;

    public SIPair transferIdent(String former) {
        if (!inlining) {
            return new SIPair(former, origin);
        } else {
            if (funcSymbolTableMap.get(inlineFunc).defined(former)) {
                return new SIPair("INLINE_" + former + "_" + currentIndex, inlineLocal);
            }
            return new SIPair(former, global);  // global Var
        }
    }

    // Cond → LOrExp
    // return Symbol
    public void checkCond(Cond cond, BasicBlock trueBlock, BasicBlock elseBlock) {
        checkLOrExp(cond.getLOrExp(), trueBlock, elseBlock);
    }

    // LOrExp → LAndExp {'||' LAndExp}
    // 短路求值
    // return Symbol
    public void checkLOrExp(LOrExp lOrExp, BasicBlock trueBlock, BasicBlock elseBlock) {
        ArrayList<LAndExp> andExps = new ArrayList<>(lOrExp.getExps());

        andExps.add(0, lOrExp.getFirstExp());
        for (int i = 0; i < andExps.size(); i++) {
            LAndExp andExp = andExps.get(i);
            if (i == andExps.size() - 1) {  // last
                checkLAndExp(andExp, trueBlock, elseBlock);
                currBlock = trueBlock;
            } else {
                BasicBlock andEndBlock = new BasicBlock("AND_END_" + blockCount++);
                checkLAndExp(andExp, trueBlock, andEndBlock);  // 全部true走trueBlock，有错误走andEndBlock
                currBlock = andEndBlock;
                currBlock.setIndex(blockId++);
            }
        }
    }

    // LAndExp → EqExp {'&&' EqExp}
    // 短路求值
    // return Symbol
    // TODO: 看看能不能省略midRes，直接根据Operand跳转
    public void checkLAndExp(LAndExp lAndExp, BasicBlock trueBlock, BasicBlock falseBlock) {
        ArrayList<EqExp> eqExps = new ArrayList<>(lAndExp.getExps());

        eqExps.add(0, lAndExp.getFirstExp());
        for (int i = 0; i < eqExps.size(); i++) {
            EqExp eqExp = eqExps.get(i);
            Operand curr = checkEqExp(eqExp);
            if (i != eqExps.size() - 1) {
                BasicBlock tempTrue = new BasicBlock("AND_" + blockCount++);
                if (curr instanceof Immediate) {
                    if (((Immediate) curr).getNumber() == 0) {
                        currBlock.addContent(new Jump(falseBlock));
                        return;
                    } else {
                        currBlock.addContent(new Jump(tempTrue));
                        currBlock = tempTrue;
                        currBlock.setIndex(blockId++);
                    }
                } else {
                    currBlock.addContent(new Branch(curr, tempTrue, falseBlock, true));
                    currBlock = tempTrue;
                    currBlock.setIndex(blockId++);
                }
            } else {
                if (curr instanceof Immediate) {
                    if (((Immediate) curr).getNumber() == 0) {
                        currBlock.addContent(new Jump(falseBlock));
                    } else {
                        currBlock.addContent(new Jump(trueBlock));
                    }
                } else {
                    currBlock.addContent(new Branch(curr, trueBlock, falseBlock, true));
                }
            }
        }
    }

    // EqExp → RelExp {('==' | '!=') RelExp}
    // 翻译成四元式
    // return Symbol
    public Operand checkEqExp(EqExp eqExp) {
        Operand eqLeft = checkRelExp(eqExp.getFirstExp());
        if (eqLeft == null) {
            return null;
        }
        Operand eqRight = null;
        ArrayList<RelExp> relExps = eqExp.getExps();
        ArrayList<Token> seps = eqExp.getSeps();
        if (seps.size() == 0) {
            return eqLeft;
        }

        if (!(eqLeft instanceof Immediate)) {
            for (int i = 0; i < relExps.size(); i++) {
                Symbol temp = Symbol.tempSymbol(SymbolType.INT);
                eqRight = checkRelExp(relExps.get(i));
                if (eqRight == null) {
                    return null;
                }
                if (seps.get(i).getType() == TokenType.EQL) {
                    currBlock.addContent(new FourExpr(eqLeft, eqRight, temp, FourExpr.ExprOp.EQ));
                } else if (seps.get(i).getType() == TokenType.NEQ) {
                    currBlock.addContent(new FourExpr(eqLeft, eqRight, temp, FourExpr.ExprOp.NEQ));
                } else {
                    assert false;
                }
                eqLeft = temp;
            }
            return eqLeft;
        }

        int index = 0;
        while (index < relExps.size()) {
            eqRight = checkRelExp(relExps.get(index));
            if (eqRight == null) {
                return null;
            }
            if (eqRight instanceof Immediate) {
                if (seps.get(index).getType() == TokenType.EQL) {
                    eqLeft = new Immediate(((Immediate) eqLeft).getNumber() == ((Immediate) eqRight).getNumber() ? 1 : 0);
                } else if (seps.get(index).getType() == TokenType.NEQ) {
                    eqLeft = new Immediate(((Immediate) eqLeft).getNumber() != ((Immediate) eqRight).getNumber() ? 1 : 0);
                }
            } else {
                break;
            }
            index++;
        }

        if (index == relExps.size()) {
            return eqLeft;
        }


        Symbol tempSymbol = Symbol.tempSymbol(SymbolType.INT);
        if (seps.get(index).getType() == TokenType.EQL) {
            currBlock.addContent(new FourExpr(eqLeft, eqRight, tempSymbol, FourExpr.ExprOp.EQ));
        } else if (seps.get(index).getType() == TokenType.NEQ) {
            currBlock.addContent(new FourExpr(eqLeft, eqRight, tempSymbol, FourExpr.ExprOp.NEQ));
        } else {
            assert false;
        }

        for (int i = index + 1; i < relExps.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.INT);
            eqRight = checkRelExp(relExps.get(i));
            if (eqRight == null) {
                return null;
            }
            if (seps.get(i).getType() == TokenType.EQL) {
                currBlock.addContent(new FourExpr(tempSymbol, eqRight, temp, FourExpr.ExprOp.EQ));
            } else if (seps.get(i).getType() == TokenType.NEQ) {
                currBlock.addContent(new FourExpr(tempSymbol, eqRight, temp, FourExpr.ExprOp.NEQ));
            } else {
                assert false;
            }
            tempSymbol = temp;
        }
        return tempSymbol;
    }

    // RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
    // 四元式表达
    // return Symbol
    public Operand checkRelExp(RelExp relExp) {
        Operand addLeft = checkAddExp(relExp.getFirstExp(), noCheck);
        if (addLeft == null) {
            return null;
        }
        ArrayList<AddExp> exps = relExp.getExps();
        ArrayList<Token> seps = relExp.getSeps();
        if (seps.size() == 0) {
            return addLeft;
        }

        int index = 0;
        Operand addRight = null;
        while (index < exps.size()) {
            addRight = checkAddExp(exps.get(index), noCheck);
            if (addRight == null) {
                return null;
            }
            if (addLeft instanceof Immediate && addRight instanceof Immediate) {
                if (seps.get(index).getType() == TokenType.LSS) {
                    addLeft = new Immediate(((Immediate) addLeft).getNumber() < ((Immediate) addRight).getNumber() ? 1 : 0);
                } else if (seps.get(index).getType() == TokenType.LEQ) {
                    addLeft = new Immediate(((Immediate) addLeft).getNumber() <= ((Immediate) addRight).getNumber() ? 1 : 0);
                } else if (seps.get(index).getType() == TokenType.GRE) {
                    addLeft = new Immediate(((Immediate) addLeft).getNumber() > ((Immediate) addRight).getNumber() ? 1 : 0);
                } else if (seps.get(index).getType() == TokenType.GEQ) {
                    addLeft = new Immediate(((Immediate) addLeft).getNumber() >= ((Immediate) addRight).getNumber() ? 1 : 0);
                }
            } else {
                break;
            }
            index++;
        }

        if (index == exps.size()) {
            return addLeft;
        }
        Symbol tempSymbol = Symbol.tempSymbol(SymbolType.INT);
        if (seps.get(index).getType() == TokenType.LSS) {
            currBlock.addContent(new FourExpr(addLeft, addRight, tempSymbol, FourExpr.ExprOp.LT));
        } else if (seps.get(index).getType() == TokenType.LEQ) {
            currBlock.addContent(new FourExpr(addLeft, addRight, tempSymbol, FourExpr.ExprOp.LE));
        } else if (seps.get(index).getType() == TokenType.GRE) {
            currBlock.addContent(new FourExpr(addLeft, addRight, tempSymbol, FourExpr.ExprOp.GT));
        } else if (seps.get(index).getType() == TokenType.GEQ) {
            currBlock.addContent(new FourExpr(addLeft, addRight, tempSymbol, FourExpr.ExprOp.GE));
        }
        index++;

        while (index < exps.size()) {
            Symbol midRes = Symbol.tempSymbol(SymbolType.INT);
            addRight = checkAddExp(exps.get(index), noCheck);
            if (addRight == null) {
                return null;
            }
            if (seps.get(index).getType() == TokenType.LSS) {
                currBlock.addContent(new FourExpr(tempSymbol, addRight, midRes, FourExpr.ExprOp.LT));
            } else if (seps.get(index).getType() == TokenType.LEQ) {
                currBlock.addContent(new FourExpr(tempSymbol, addRight, midRes, FourExpr.ExprOp.LE));
            } else if (seps.get(index).getType() == TokenType.GRE) {
                currBlock.addContent(new FourExpr(tempSymbol, addRight, midRes, FourExpr.ExprOp.GT));
            } else if (seps.get(index).getType() == TokenType.GEQ) {
                currBlock.addContent(new FourExpr(tempSymbol, addRight, midRes, FourExpr.ExprOp.GE));
            }
            index++;
            tempSymbol = midRes;
        }
        return tempSymbol;
    }
}

package Frontend.Symbol;

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
import Frontend.Parser.expr.types.Immediate;
import Frontend.Parser.expr.types.LAndExp;
import Frontend.Parser.expr.types.LOrExp;
import Frontend.Parser.expr.types.LVal;
import Frontend.Parser.expr.types.LeafNode;
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
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Pointer;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/*
 *
 * 符号表需要下降一层: 定义的新函数，blockStmt，whileStmt，ifStmt
 *
 */
// TODO: LeafNode未进行合并计算，只返回表达式的首项，用来在FuncRParams中检查类型是否正确
public class SymbolTableBuilder {
    private SymbolTable currSymbolTable = new SymbolTable(null);
    private final Errors errors = new Errors();
    private final CompUnit compUnit;
    // private FuncDef currFunc;  // not using

    // about block
    private BasicBlock currBlock;
    private int blockCount = 0;
    private int blockDepth = 0;

    // about function
    private FuncBlock currFunc;
    private TokenType currFuncType = null;

    private final MiddleCode middleCode = new MiddleCode();

    // about loop
    private int loopDepth = 0;

    public SymbolTableBuilder(CompUnit compUnit) {
        this.compUnit = compUnit;
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
            checkFunc(funcDef);
        }

        // check main func
        checkMainFunc(mainFunction);
        errors.output();
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

    // 常数定义 ConstDef → Var '=' ConstInitVal
    // 变量定义 VarDef → Var | Var '=' InitVal
    public void checkDef(Def def) {
        /*
         *  int a = a * a;
         * */
        if (def.getDimCount() == 0) {  // not an array; int or pointer
            if (def.hasInitVal()) {
                InitVal initVal = def.getInitVal();
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                if (initVal.isConst() || currFunc == null) {
                    int val = new ConstExpCalculator(currSymbolTable, errors).calcExp(def.getInitVal().getExp());
                    if (currFunc == null) {  // pre decl, not in a function
                        middleCode.addInt(def.getVar().getIdent().getContent(), currSymbolTable.getStackSize(), val);
                    } else {  // decl in a function
                        currBlock.addContent(new Middle.type.FourExpr(new Immediate(val), symbol, FourExpr.ExprOp.DEF));
                    }
                } else {
                    LeafNode val = checkExp(initVal.getExp());
                    currBlock.addContent(new Middle.type.FourExpr(val, symbol, FourExpr.ExprOp.DEF));
                }
            } else {  // 没有初始化
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                if (currFunc == null) {  // pre decl, not in a function
                    middleCode.addInt(def.getVar().getIdent().getContent(), currSymbolTable.getStackSize(), 0);
                } else {  // decl in a function
                    currBlock.addContent(new Middle.type.FourExpr(new Immediate(0), symbol, FourExpr.ExprOp.DEF));
                }
            }
        } else {  // 数组
            if (def.hasInitVal()) {
                InitVal initVal = def.getInitVal();
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                ArrayList<AddExp> initExp = arrayInitFlatter(initVal);
                // int stackSize = 1;
                // ArrayList<Integer> dimSize = symbol.getDimSize();
                // for (Integer s : dimSize) {
                //     stackSize *= s;
                // }
                if (initVal.isConst() || currFunc == null) {
                    ConstExpCalculator constExpCalculator = new ConstExpCalculator(currSymbolTable, errors);
                    ArrayList<Integer> initNum = initExp.stream().map(constExpCalculator::calcAddExp)
                            .collect(Collectors.toCollection(ArrayList::new));
                    if (currFunc == null) {  // 全局变量
                        middleCode.addArray(symbol.getIdent().getContent(), currSymbolTable.getStackSize(), initNum);
                    } else {  // 局部变量
                        int offset = 0;
                        for (Integer num : initNum) {
                            Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                            currBlock.addContent(new Memory(symbol, new Immediate(offset * 4), ptr));
                            currBlock.addContent(new Pointer(Pointer.Op.STORE, ptr, new Immediate(num)));
                            offset++;
                        }
                    }
                } else {
                    int offset = 0;
                    for (AddExp addExp : initExp) {
                        LeafNode exp = checkAddExp(addExp);
                        Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
                        currBlock.addContent(new Memory(symbol, new Immediate(offset * 4), ptr));
                        currBlock.addContent(new Pointer(Pointer.Op.STORE, ptr, exp));
                        offset++;
                    }
                }
            } else {
                Symbol symbol = checkVar(def.getVar());  // 先检查initial Val，再检查Var
                if (currFunc == null) {
                    ArrayList<Integer> initZero = new ArrayList<>();
                    for (int i = 0; i < def.getDimCount(); i++) initZero.add(0);
                    middleCode.addArray(symbol.getIdent().getContent(), currSymbolTable.getStackSize(), initZero);
                } else {
                    // nothing to do
                    // 函数中的局部变量，没有初始化
                }
            }


        }
    }

    private ArrayList<AddExp> arrayInitFlatter(InitVal initVal) {
        ArrayList<AddExp> res = new ArrayList<>();
        if (initVal.isLeaf()) {
            if (initVal.getExp() != null) {
                res.add(initVal.getExp().getAddExp());
            } else {
                res.add(initVal.getConstExp().getAddExp());
            }
        } else {
            for (InitVal init : initVal.getInitVals()) {
                res.addAll(arrayInitFlatter(init));
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
                    var.getDimCount(), var.isConst());
            symbol.setAddress(currSymbolTable.getStackSize());
            currSymbolTable.addSymbol(symbol);  // 会同时为Symbol申请空间
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
    public void checkFunc(FuncDef funcDef) {
        currFuncType = funcDef.getReturnType().getType();
        Token ident = funcDef.getIdent();
        // check redefine
        boolean redefine = false;
        if (currSymbolTable.contains(ident.getContent(), false)) {  // check redefine
            errors.add(new RedefinedTokenException(ident.getLine()));
            redefine = true;
            // return;  // stop here or not?
        }

        currSymbolTable = new SymbolTable(currSymbolTable);  // 下降一层

        // check missing Parenthesis
        // this.currFunc = funcDef;  // unused
        if (funcDef.missRightParenthesis()) {
            errors.add(new MissRparentException(funcDef.getLeftParenthesis().getLine()));
        }

        // check FuncFParams
        ArrayList<FuncFParam> funcFParams = funcDef.getFuncFParams();
        ArrayList<Symbol> params = new ArrayList<>();
        for (FuncFParam funcFParam : funcFParams) {
            Symbol symbol = checkFuncFParam(funcFParam); // checkFuncFParam could return null
            if (symbol != null) {
                params.add(symbol);
            }
        }

        // add to Frontend.Symbol Table(parent symbol table)
        Token returnType = funcDef.getReturnType();
        if (!redefine) {
            currSymbolTable.getParent().addSymbol(new Symbol(SymbolType.FUNCTION,  // func 加到父符号表中
                    returnType.getType() == TokenType.INTTK ? SymbolType.INT : SymbolType.VOID, params, ident));
        }

        // check func stmt
        checkBlockStmt(funcDef.getBlockStmt(), true);

        // check return right or not
        // boolean returnInt = funcDef.returnInt();
        // if (funcType.getType() == TokenType.VOIDTK && returnInt) {
        //     errors.add(new IllegalReturnException(funcDef.getReturn().getLine()));
        // } else
        if (returnType.getType() == TokenType.INTTK && !funcDef.returnInt()) {
            errors.add(new MissReturnException(funcDef.getRightBrace().getLine()));
        }

        currSymbolTable = currSymbolTable.getParent();  // 上升一层
    }

    // MainFuncDef → 'int' 'main' '(' ')' Block
    public void checkMainFunc(MainFuncDef mainFuncDef) {
        checkFunc(mainFuncDef.getFuncDef());
    }

    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    // Warning: this func could return null
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
        ArrayList<Integer> dimNum = funcFParam.getDimNum();
        SymbolType symbolType = dimNum.size() == 0 ? SymbolType.INT : SymbolType.ARRAY;
        if (!redefine) {
            Symbol symbol = new Symbol(symbolType, funcFParam.getBracks(), dimNum, ident, false);
            currSymbolTable.addSymbol(symbol);
            return symbol;
        } else {
            return null;
        }
    }

    // 语句块项 BlockItem → Decl | Stmt
    public void checkBlockItem(BlockItem blockItem) {
        if (blockItem instanceof Decl) {
            checkDecl((Decl) blockItem);
        } else if (blockItem instanceof Stmt) {
            checkStmt((Stmt) blockItem);
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
            checkBlockStmt((BlockStmt) stmtInterface, false);
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
        checkLVal(assignStmt.getLVal(), true);
        // check right Val
        checkExp(assignStmt.getExp());
    }

    // Block → '{' { BlockItem } '}'
    public void checkBlockStmt(BlockStmt blockStmt, boolean isFuncBlock) {
        if (!isFuncBlock) {
            currSymbolTable = new SymbolTable(currSymbolTable);  // 下降一层
        }
        // traverse all blockItems and check
        ArrayList<BlockItem> blockItems = blockStmt.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            checkBlockItem(blockItem);
        }

        if (!isFuncBlock) {
            currSymbolTable = currSymbolTable.getParent();  // 上升一层
        }
    }

    // break;
    public void checkBreakStmt(BreakStmt breakStmt) {
        if (loopDepth == 0) {
            errors.add(new IllegalBreakContinueException(breakStmt.getSemicolonLine()));
        }
    }

    // continue;
    public void checkContinueStmt(ContinueStatement continueStatement) {
        if (loopDepth == 0) {
            errors.add(new IllegalBreakContinueException(continueStatement.getSemicolonLine()));
        }
    }

    // Exp ';'
    public void checkExpStmt(ExpStmt expStmt) {
        checkExp(expStmt.getExp());
    }

    // LVal '=' 'getint''('')'
    // TODO: check LVal using right or not（LVal是否正确使用，和Exp是否匹配，int）
    public void checkGetIntStmt(GetIntStmt getIntStmt) {
        // check miss Right Parenthesis
        if (getIntStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(getIntStmt.getLine()));
        }
        // check LVal, and LVal could not be const
        checkLVal(getIntStmt.getLVal(), true);
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public void checkIfStmt(IfStmt ifStmt) {
        // check cond Stmt
        checkCond(ifStmt.getCond());
        if (ifStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(ifStmt.getIfLine()));
        }
        currSymbolTable = new SymbolTable(currSymbolTable);  // 下降一层

        // check all stmts
        ArrayList<Stmt> stmts = ifStmt.getStmts();
        for (Stmt stmt : stmts) {
            checkStmt(stmt);
        }
        currSymbolTable = currSymbolTable.getParent();  // 上升一层
    }

    public void checkPrintfStmt(PrintfStmt printfStmt) {
        if (printfStmt.getPrintf().getLine() == 696) {
            System.out.println(1);
        }
        if (!printfStmt.checkCountMatch()) {  // 检查Exp个数是否匹配
            errors.add(new MismatchPrintfException(printfStmt.getPrintf().getLine()));
        }
        if (!printfStmt.checkFormatString()) {  // 检查formatString
            errors.add(new IllegalSymbolException(printfStmt.getFormatString().getLine()));
        }
        if (printfStmt.missRightParenthesis()) {  // 检查是否缺少左括号
            errors.add(new MissRparentException(printfStmt.getFormatString().getLine()));
        }

        // 检查所有Exp
        ArrayList<Exp> exps = printfStmt.getExps();
        for (Exp exp : exps) {
            checkExp(exp);
        }
    }

    public void checkReturnStmt(ReturnStmt returnStmt) {
        assert currFuncType != null;  // 不在Decl区
        if (currFuncType == TokenType.VOIDTK && returnStmt.getReturnExp() != null) {
            errors.add(new IllegalReturnException(returnStmt.getReturnToken().getLine()));
        }
        if (returnStmt.getReturnExp() != null) {
            checkExp(returnStmt.getReturnExp());
        }
        // 是否return正确在checkFunc中检查
    }

    // 'while' '(' Cond ')' Stmt
    public void checkWhileStmt(WhileStmt whileStmt) {
        loopDepth++;
        // check missing Right Parenthesis
        if (whileStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(whileStmt.getLine()));
        }

        // check Cond
        checkCond(whileStmt.getCond());

        currSymbolTable = new SymbolTable(currSymbolTable);  // 下降一层
        // check Stmt
        checkStmt(whileStmt.getStmt());
        currSymbolTable = currSymbolTable.getParent();  // 上升一层
        loopDepth--;
    }

    public void checkConstExp(ConstExp constExp) {
        checkAddExp(constExp.getAddExp());
    }

    public LeafNode checkExp(Exp exp) {
        return checkAddExp(exp.getAddExp());
    }

    // 四元式表达
    // AddExp → MulExp {('+' | '−') MulExp}
    public LeafNode checkAddExp(AddExp addExp) {
        LeafNode first = checkMulExp(addExp.getFirstExp());
        Symbol midRes = Symbol.tempSymbol(SymbolType.BOOL);
        currBlock.addContent(new FourExpr(first, midRes, FourExpr.ExprOp.ASS));
        ArrayList<LeafNode> nodes = addExp.getExps().stream().map(this::checkMulExp)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> seps = addExp.getSeps();
        for (int i = 0; i < nodes.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.BOOL);
            if (seps.get(i).getType() == TokenType.PLUS) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.ADD));
            } else if (seps.get(i).getType() == TokenType.MINU) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.SUB));
            } else {
                assert false;
            }
            midRes = temp;
        }
        return midRes;
    }

    // MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
    public LeafNode checkMulExp(MulExp mulExp) {
        LeafNode first = checkUnaryExp(mulExp.getFirstExp());
        Symbol midRes = Symbol.tempSymbol(SymbolType.BOOL);
        currBlock.addContent(new FourExpr(first, midRes, FourExpr.ExprOp.ASS));
        ArrayList<LeafNode> nodes = mulExp.getExps().stream().map(this::checkUnaryExp)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> seps = mulExp.getSeps();
        for (int i = 0; i < nodes.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.BOOL);
            if (seps.get(i).getType() == TokenType.MULT) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.MUL));
            } else if (seps.get(i).getType() == TokenType.DIV) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.DIV));
            } else if (seps.get(i).getType() == TokenType.MOD) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.MOD));
            } else {
                assert false;
            }
            midRes = temp;
        }
        return midRes;
    }

    public LeafNode checkUnaryExp(UnaryExp unaryExp) {
        return checkUnaryExpInterFace(unaryExp.getUnaryExpInterface());
    }

    public LeafNode checkUnaryExpInterFace(UnaryExpInterface unaryExpInterface) {
        if (unaryExpInterface instanceof PrimaryExp) {
            return checkPrimaryExp((PrimaryExp) unaryExpInterface);
        } else if (unaryExpInterface instanceof FuncExp) {  // TODO: 检查FuncExp的returnType是否匹配
            return checkFuncExp((FuncExp) unaryExpInterface);
        } else if (unaryExpInterface instanceof UnaryExp) {
            return checkUnaryExp((UnaryExp) unaryExpInterface);
        }
        // not output
        assert false;
        return null;
    }

    public LeafNode checkPrimaryExp(PrimaryExp primaryExp) {
        return checkPrimaryExpInterFace(primaryExp.getPrimaryExpInterface());
    }

    public LeafNode checkPrimaryExpInterFace(PrimaryExpInterface primaryExpInterface) {
        if (primaryExpInterface instanceof BraceExp) {
            return checkBraceExp((BraceExp) primaryExpInterface);
        } else if (primaryExpInterface instanceof LVal) {
            // TODO: check LVal using right or not（LVal是否正确使用，和Exp是否匹配，int）
            return checkLVal((LVal) primaryExpInterface, false);
        } else if (primaryExpInterface instanceof Number) {
            return checkNumber((Number) primaryExpInterface);
        }
        // not output
        assert false;
        return null;
    }

    // BraceExp = '(' Exp ')'
    public LeafNode checkBraceExp(BraceExp braceExp) {
        // check missing Right Parenthesis
        if (braceExp.missRightParenthesis()) {
            errors.add(new MissRparentException(braceExp.getLine()));
        }
        return checkExp(braceExp.getExp());
    }

    // LVal → Ident {'[' Exp ']'}
    // 会给LVal Symbol赋值
    // 当不在符号表中存在时会返回null
    public LeafNode checkLVal(LVal lVal, boolean checkConst) {  // checkConst represents check const or not
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

        /*
         * translate to middle code
         * */
        if (symbol.getSymbolType() == SymbolType.INT) {
            return symbol;
        } else if (symbol.getSymbolType() == SymbolType.ARRAY) {
            ArrayList<Integer> dimSize = symbol.getDimSize();
            ArrayList<Integer> suffix = new ArrayList<>();
            suffix.add(1);
            for (int i = dimSize.size() - 1; i > 0; i--) {
                suffix.add(0, suffix.get(0) * dimSize.get(i));
            }
            ArrayList<LeafNode> place = lVal.getExps().stream().map(this::checkExp)
                    .collect(Collectors.toCollection(ArrayList::new));  // 每一维的取值
            LeafNode offset = new Immediate(0);
            for (int i = place.size() - 1; i >= 0; i--) {
                LeafNode weight = new Immediate(suffix.get(i));  // 后缀积
                Symbol mid = Symbol.tempSymbol(SymbolType.INT);
                currBlock.addContent(new FourExpr(place.get(i), weight, mid, FourExpr.ExprOp.MUL));
                currBlock.addContent(new FourExpr(mid, offset, mid, FourExpr.ExprOp.AND));
                offset = mid;
            }
            // Symbol addr = Symbol.tempSymbol(SymbolType.INT);
            // currBlock.addContent(new FourExpr(new Immediate(symbol.getAddress()), offset, addr, FourExpr.ExprOp
            // .ADD));
            Symbol ptr = Symbol.tempSymbol(SymbolType.POINTER);
            currBlock.addContent(new Memory(symbol, offset, ptr));
            Symbol res = Symbol.tempSymbol(SymbolType.INT);
            currBlock.addContent(new Pointer(Pointer.Op.LOAD, ptr, res));
            return res;
        }
        assert false;
        return null;
    }

    public LeafNode checkNumber(Number number) {
        return number;
    }

    // 函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
    // 函数实参表 FuncRParams → Exp { ',' Exp }
    // 会给Func的ReturnType赋值
    // 当func不在符号表中存在时会返回null
    // 当Rparam不在符号表中时会返回FuncExp
    public LeafNode checkFuncExp(FuncExp funcExp) {
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
        if (!symbol.isFunc()) {
            return null;  // TODO: 帖子，什么错误类型？？
        }
        assert symbol.isFunc();  // assert symbol is a function
        funcExp.setReturnType(symbol.getReturnType());  // 设置function的returnType  TODO: 可以在未来用来检查funcExp是否正确使用

        // 形参表
        ArrayList<Symbol> Fparams = symbol.getParams();

        // 实参表
        FuncRParams funcRParams = funcExp.getParams();
        ArrayList<LeafNode> Rparams = new ArrayList<>();  // LeafNode is LVal or Number or funcExp
        if (funcRParams != null) {
            for (Exp exp : funcRParams.getExps()) {
                LeafNode res = checkExp(exp);
                if (res == null) {
                    return funcExp;
                }
                Rparams.add(res);  // TODO: LeafNode未进行合并计算，只返回表达式的首项，用来在FuncRParams中检查类型是否正确
            }  // check Exp会给所有的LVal和FuncExp设置SymbolType
        }

        // match check
        if (Fparams.size() != Rparams.size()) {
            errors.add(new MismatchParamCountException(funcExp.getLine()));  // param count mismatch
        } else {
            for (int i = 0; i < Fparams.size(); i++) {
                Symbol fParam = Fparams.get(i);
                LeafNode rParam = Rparams.get(i);

                // 由于之前checkExp已经给每个LeafNode赋过SymbolType了，这里一定可以得到SymbolType
                if (fParam.getSymbolType() != rParam.getSymbolType()) {  // param type mismatch
                    errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                    break;
                }
                if (fParam.getSymbolType() == SymbolType.ARRAY) {  // 检查数组参数的维数是否正确
                    assert rParam instanceof LVal;  // 数组一定是LVal
                    if (fParam.getDimCount() != rParam.getDimCount()) {  // 维数不正确
                        errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                        break;
                    }

                    // 检查每一维的数值是否正确
                    /*
                     *  int f(int a[][2]) {}
                     *  int main(){
                     *       int a[2][3];
                     *       f(a);  // mismatch!!!
                     *  }
                     * */
                    ArrayList<Integer> fDimSize = fParam.getDimSize();
                    ArrayList<Integer> rDimSize = rParam.getDimSize();
                    for (int j = 1; j < fDimSize.size(); j++) {  // 跳过第一维
                        if (!Objects.equals(fDimSize.get(j), rDimSize.get(j))) {
                            errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                            break;
                        }
                    }
                }
            }
        }
        FuncBlock funcBlock = middleCode.getFunc(symbol.getName());
        if (funcBlock.getReturnType() == FuncBlock.ReturnType.INT) {
            Symbol res = Symbol.tempSymbol(SymbolType.INT);
            currBlock.addContent(new FuncCall(funcBlock, Rparams, res));
            return res;
        }
        return new Immediate(0);
    }

    // Cond → LOrExp
    public LeafNode checkCond(Cond cond) {
        return checkLOrExp(cond.getLOrExp());
    }

    // LOrExp → LAndExp {'||' LAndExp}
    // 短路求值
    public LeafNode checkLOrExp(LOrExp lOrExp) {
        BasicBlock orEnd = new BasicBlock("B_OR_END_" + blockCount++);
        LeafNode and = checkLAndExp(lOrExp.getFirstExp());
        Symbol orMidRes = Symbol.tempSymbol(SymbolType.BOOL);
        currBlock.addContent(new FourExpr(and, orMidRes, FourExpr.ExprOp.ASS));
        BasicBlock falseBlock = new BasicBlock("B_OR_" + blockCount++);
        currBlock.addContent(new Branch(orMidRes, orEnd, falseBlock));

        currBlock = falseBlock;
        ArrayList<LAndExp> andExps = lOrExp.getExps();
        for (LAndExp lAndExp : andExps) {
            and = checkLAndExp(lAndExp);
            // Symbol orMidRes = Symbol.tempSymbol(SymbolType.INT);  maybe we can use former temp var res
            currBlock.addContent(new FourExpr(and, orMidRes, FourExpr.ExprOp.ASS));
            falseBlock = new BasicBlock("B_OR_" + blockCount++);
            currBlock.addContent(new Branch(orMidRes, orEnd, falseBlock));
            currBlock = falseBlock;
        }
        currBlock.addContent(new Jump(orEnd));
        return orMidRes;
    }

    // LAndExp → EqExp {'&&' EqExp}
    // 短路求值
    public LeafNode checkLAndExp(LAndExp lAndExp) {
        BasicBlock andEnd = new BasicBlock("B_AND_END_" + blockCount++);
        LeafNode eq = checkEqExp(lAndExp.getFirstExp());
        Symbol andMidRes = Symbol.tempSymbol(SymbolType.BOOL);
        currBlock.addContent(new FourExpr(eq, andMidRes, FourExpr.ExprOp.ASS));
        BasicBlock trueBlock = new BasicBlock("B_AND_" + blockCount++);
        currBlock.addContent(new Branch(andMidRes, trueBlock, andEnd));

        currBlock = trueBlock;
        ArrayList<EqExp> eqExps = lAndExp.getExps();
        for (EqExp eqExp : eqExps) {
            eq = checkEqExp(eqExp);
            currBlock.addContent(new FourExpr(eq, andMidRes, FourExpr.ExprOp.ASS));
            trueBlock = new BasicBlock("B_AND_" + blockCount++);
            currBlock.addContent(new Branch(andMidRes, trueBlock, andEnd));
            currBlock = trueBlock;
        }
        currBlock.addContent(new Jump(andEnd));
        return andMidRes;
    }

    // EqExp → RelExp {('==' | '!=') RelExp}
    // 翻译成四元式
    public LeafNode checkEqExp(EqExp eqExp) {
        LeafNode first = checkRelExp(eqExp.getFirstExp());
        Symbol midRes = Symbol.tempSymbol(SymbolType.BOOL);
        currBlock.addContent(new FourExpr(first, midRes, FourExpr.ExprOp.ASS));
        ArrayList<LeafNode> nodes = eqExp.getExps().stream().map(this::checkRelExp)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> seps = eqExp.getSeps();
        for (int i = 0; i < nodes.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.BOOL);
            if (seps.get(i).getType() == TokenType.EQL) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.EQ));
            } else if (seps.get(i).getType() == TokenType.NEQ) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.NEQ));
            } else {
                assert false;
            }
            midRes = temp;
        }
        return midRes;
    }

    // RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
    // 四元式表达
    public LeafNode checkRelExp(RelExp relExp) {
        LeafNode first = checkAddExp(relExp.getFirstExp());
        Symbol midRes = Symbol.tempSymbol(SymbolType.BOOL);
        currBlock.addContent(new FourExpr(first, midRes, FourExpr.ExprOp.ASS));
        ArrayList<LeafNode> nodes = relExp.getExps().stream().map(this::checkAddExp)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> seps = relExp.getSeps();
        for (int i = 0; i < nodes.size(); i++) {
            Symbol temp = Symbol.tempSymbol(SymbolType.BOOL);
            if (seps.get(i).getType() == TokenType.LSS) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.LT));
            } else if (seps.get(i).getType() == TokenType.LEQ) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.LE));
            } else if (seps.get(i).getType() == TokenType.GRE) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.GT));
            } else if (seps.get(i).getType() == TokenType.GEQ) {
                currBlock.addContent(new FourExpr(midRes, nodes.get(i), temp, FourExpr.ExprOp.GE));
            } else {
                assert false;
            }
            midRes = temp;
        }
        return midRes;
    }
}

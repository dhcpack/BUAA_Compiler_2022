package Symbol;

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
import Lexer.Token;
import Lexer.TokenType;
import Parser.CompUnit;
import Parser.decl.types.Decl;
import Parser.decl.types.Def;
import Parser.decl.types.InitVal;
import Parser.decl.types.Var;
import Parser.expr.types.AddExp;
import Parser.expr.types.BraceExp;
import Parser.expr.types.Cond;
import Parser.expr.types.ConstExp;
import Parser.expr.types.EqExp;
import Parser.expr.types.Exp;
import Parser.expr.types.FuncExp;
import Parser.expr.types.FuncRParams;
import Parser.expr.types.LAndExp;
import Parser.expr.types.LOrExp;
import Parser.expr.types.LVal;
import Parser.expr.types.LeafNode;
import Parser.expr.types.MulExp;
import Parser.expr.types.Number;
import Parser.expr.types.PrimaryExp;
import Parser.expr.types.PrimaryExpInterface;
import Parser.expr.types.RelExp;
import Parser.expr.types.UnaryExp;
import Parser.expr.types.UnaryExpInterface;
import Parser.func.types.FuncDef;
import Parser.func.types.FuncFParam;
import Parser.func.types.MainFuncDef;
import Parser.stmt.types.AssignStmt;
import Parser.stmt.types.BlockItem;
import Parser.stmt.types.BlockStmt;
import Parser.stmt.types.BreakStmt;
import Parser.stmt.types.ContinueStatement;
import Parser.stmt.types.ExpStmt;
import Parser.stmt.types.GetIntStmt;
import Parser.stmt.types.IfStmt;
import Parser.stmt.types.PrintfStmt;
import Parser.stmt.types.ReturnStmt;
import Parser.stmt.types.Stmt;
import Parser.stmt.types.StmtInterface;
import Parser.stmt.types.WhileStmt;


import java.util.ArrayList;
import java.util.Objects;

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
    private FuncDef currFunc;  // not using
    private TokenType currFuncType = null;

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
        if (def.hasInitVal()) {  // 先检查initial Val，再检查Def
            checkInitVal(def.getInitVal());
        }
        checkVar(def.getVar());
    }

    //  常量变量 Var -> Ident { '[' ConstExp ']' }
    public void checkVar(Var var) {
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

        // check const Exp
        ArrayList<ConstExp> dimExp = var.getDimExp();
        for (ConstExp constExp : dimExp) {
            checkConstExp(constExp);
        }

        // add to symbol table
        ArrayList<Integer> dimNums = var.getDimNum();  // 维数的数值
        SymbolType symbolType = dimNums.size() == 0 ? SymbolType.INT : SymbolType.ARRAY;
        if (!redefine) {
            currSymbolTable.addSymbol(new Symbol(symbolType, var.getBracks(), dimNums, ident, var.isConst()));
        }
    }

    // 常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    // 变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public void checkInitVal(InitVal initVal) {
        if (initVal.getExp() != null) {  // only expr
            checkExp(initVal.getExp());
        } else if (initVal.getConstExp() != null) {  // only const expr
            checkConstExp(initVal.getConstExp());
        } else {  // initial Vals
            ArrayList<InitVal> initVals = initVal.getInitVals();
            for (InitVal initVal1 : initVals) {
                checkInitVal(initVal1);
            }
        }
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public void checkFunc(FuncDef funcDef) {
        currFuncType = funcDef.getFuncType().getType();
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
        this.currFunc = funcDef;  // unused
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

        // add to Symbol Table(parent symbol table)
        Token funcType = funcDef.getFuncType();
        if (!redefine) {
            currSymbolTable.getParent().addSymbol(new Symbol(SymbolType.FUNCTION,  // func 加到父符号表中
                    funcType.getType() == TokenType.INTTK ? SymbolType.INT : SymbolType.VOID, params, ident));
        }

        // check func stmt
        checkBlockStmt(funcDef.getBlockStmt(), true);

        // check return right or not
        // boolean returnInt = funcDef.returnInt();
        // if (funcType.getType() == TokenType.VOIDTK && returnInt) {
        //     errors.add(new IllegalReturnException(funcDef.getReturn().getLine()));
        // } else
        if (funcType.getType() == TokenType.INTTK && !funcDef.returnInt()) {
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

    public LeafNode checkAddExp(AddExp addExp) {
        // TODO: only deal with first Exp
        LeafNode first = checkMulExp(addExp.getFirstExp());
        ArrayList<MulExp> mulExps = addExp.getExps();
        for (MulExp mulExp : mulExps) {
            checkMulExp(mulExp);
        }
        return first;
    }

    public LeafNode checkMulExp(MulExp mulExp) {
        // TODO: only deal with first Exp
        LeafNode first = checkUnaryExp(mulExp.getFirstExp());
        ArrayList<UnaryExp> unaryExps = mulExp.getExps();
        for (UnaryExp unaryExp : unaryExps) {
            checkUnaryExp(unaryExp);
        }
        return first;
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
        System.out.println("In checkUnaryExpInterFace: this line should not output");
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
        System.out.println("In checkPrimaryExpInterFace: this line should not output");
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
        return lVal;
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
                    if (fParam.getDimsCount() != rParam.getDimCount()) {  // 维数不正确
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
                    ArrayList<Integer> fDimNum = fParam.getDimNum();
                    ArrayList<Integer> rDimNum = rParam.getDimNum();
                    for (int j = 1; j < fDimNum.size(); j++) {  // 跳过第一维
                        if (!Objects.equals(fDimNum.get(j), rDimNum.get(j))) {
                            errors.add(new MismatchParamTypeException(funcRParams.getLine()));
                            break;
                        }
                    }
                }
            }
        }
        return funcExp;
    }

    // Cond → LOrExp
    public LeafNode checkCond(Cond cond) {
        return checkLOrExp(cond.getLOrExp());
    }

    // LOrExp → LAndExp {'||' LAndExp}
    public LeafNode checkLOrExp(LOrExp lOrExp) {
        LeafNode first = checkLAndExp(lOrExp.getFirstExp());
        ArrayList<LAndExp> andExps = lOrExp.getExps();
        for (LAndExp lAndExp : andExps) {
            checkLAndExp(lAndExp);
        }
        return first;
    }

    public LeafNode checkLAndExp(LAndExp lAndExp) {
        LeafNode first = checkEqExp(lAndExp.getFirstExp());
        ArrayList<EqExp> eqExps = lAndExp.getExps();
        for (EqExp eqExp : eqExps) {
            checkEqExp(eqExp);
        }
        return first;
    }

    public LeafNode checkEqExp(EqExp eqExp) {
        LeafNode first = checkRelExp(eqExp.getFirstExp());
        ArrayList<RelExp> relExps = eqExp.getExps();
        for (RelExp relExp : relExps) {
            checkRelExp(relExp);
        }
        return first;
    }

    public LeafNode checkRelExp(RelExp relExp) {
        LeafNode first = checkAddExp(relExp.getFirstExp());
        ArrayList<AddExp> addExps = relExp.getExps();
        for (AddExp addExp : addExps) {
            checkAddExp(addExp);
        }
        return first;
    }
}

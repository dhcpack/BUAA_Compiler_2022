package Symbol;

import Exceptions.IllegalBreakContinueException;
import Exceptions.IllegalReturnException;
import Exceptions.IllegalSymbolException;
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
import Parser.expr.types.ConstExp;
import Parser.expr.types.Exp;
import Parser.expr.types.FuncExp;
import Parser.expr.types.LVal;
import Parser.expr.types.MulExp;
import Parser.expr.types.Number;
import Parser.expr.types.PrimaryExp;
import Parser.expr.types.PrimaryExpInterface;
import Parser.expr.types.UnaryExp;
import Parser.expr.types.UnaryExpInterface;
import Parser.expr.types.UnaryOp;
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

public class SymbolTableBuilder {
    private SymbolTable currSymbolTable = new SymbolTable(null);
    private final Errors errors = new Errors();
    private final CompUnit compUnit;
    private FuncDef currFunc;

    private int loopDepth = 0;

    public SymbolTableBuilder(CompUnit compUnit) {
        this.compUnit = compUnit;
    }

    public void checkCompUnit() {
        ArrayList<Decl> decls = compUnit.getGlobalVariables();
        ArrayList<FuncDef> funcDefs = compUnit.getFunctions();
        MainFuncDef mainFunction = compUnit.getMainFunction();
        for (Decl decl : decls) {
            checkDecl(decl);
        }
        for (FuncDef funcDef : funcDefs) {
            checkFunc(funcDef);
        }
        checkMainFunc(mainFunction);
        errors.output();
    }

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
        checkVar(def.getVar());
        if (def.hasInitVal()) {
            checkInitVal(def.getInitVal());
        }
    }

    public void checkVar(Var var) {
        Token ident = var.getIdent();
        if (currSymbolTable.contains(ident.getContent(), false)) {
            errors.add(new RedefinedTokenException(ident.getLine()));
        }
        if (var.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }
        ArrayList<ConstExp> dims = var.getDims();
        for (ConstExp constExp : dims) {
            checkConstExp(constExp);
        }
        SymbolType symbolType = dims.size() == 0 ? SymbolType.INT : SymbolType.ARRAY;
        currSymbolTable.addSymbol(new Symbol(symbolType, dims, ident, var.isConst()));
    }

    public void checkInitVal(InitVal initVal) {
        if (initVal.getExp() != null) {
            checkExp(initVal.getExp());
        } else if (initVal.getConstExp() != null) {
            checkConstExp(initVal.getConstExp());
        } else {
            ArrayList<InitVal> initVals = initVal.getInitVals();
            for (InitVal initVal1 : initVals) {
                checkInitVal(initVal1);
            }
        }
    }

    public void checkFunc(FuncDef funcDef) {
        Token ident = funcDef.getIdent();
        if (currSymbolTable.contains(ident.getContent(), false)) {
            errors.add(new RedefinedTokenException(ident.getLine()));
            // return;  // stop here or not?
        }

        currSymbolTable = new SymbolTable(currSymbolTable);  // 下降一层
        this.currFunc = funcDef;
        if (funcDef.missRightParenthesis()) {
            errors.add(new MissRparentException(funcDef.getLeftParenthesis().getLine()));
        }
        ArrayList<FuncFParam> funcFParams = funcDef.getFuncFParams();
        ArrayList<Symbol> params = new ArrayList<Symbol>();
        for (FuncFParam funcFParam : funcFParams) {
            params.add(funcFParam.toSymbol());
            checkFuncFParam(funcFParam);
        }
        currSymbolTable.getParent().addSymbol(new Symbol(SymbolType.FUNCTION, params, ident));  // func 加到父符号表中
        checkBlockStmt(funcDef.getBlockStmt());
        Token funcType = funcDef.getFuncType();
        boolean returnInt = funcDef.returnInt();
        if (funcType.getType() == TokenType.VOIDTK && returnInt) {
            errors.add(new IllegalReturnException(funcDef.getReturn().getLine()));
        } else if (funcType.getType() == TokenType.INTTK && !returnInt) {
            errors.add(new MissReturnException(funcDef.getRightBrace().getLine()));
        }
        currSymbolTable = currSymbolTable.getParent();

    }

    public void checkMainFunc(MainFuncDef mainFuncDef) {
        checkFunc(mainFuncDef.getFuncDef());
    }

    public void checkFuncFParam(FuncFParam funcFParam) {
        Token ident = funcFParam.getIdent();
        if (currSymbolTable.contains(ident.getContent(), false)) {
            errors.add(new RedefinedTokenException(ident.getLine()));
        }
        if (funcFParam.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }
        ArrayList<ConstExp> dims = funcFParam.getDims();
        SymbolType symbolType = dims.size() == 0 ? SymbolType.INT : SymbolType.ARRAY;
        currSymbolTable.addSymbol(new Symbol(symbolType, dims, ident, false));
    }

    public void checkBlockItem(BlockItem blockItem) {
        if (blockItem instanceof Decl) {
            checkDecl((Decl) blockItem);
        } else if (blockItem instanceof Stmt) {
            checkStmt((Stmt) blockItem);
        }
    }

    public void checkStmt(Stmt stmt) {
        StmtInterface stmtInterface = stmt.getStmt();
        if (stmt.missSemicolon()) {
            errors.add(new MissSemicnException(stmtInterface.getSemicolonLine()));
        }
        if (stmtInterface instanceof AssignStmt) {
            checkAssignStmt((AssignStmt) stmtInterface);
        } else if (stmtInterface instanceof BlockStmt) {
            checkBlockStmt((BlockStmt) stmtInterface);
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

    public void checkAssignStmt(AssignStmt assignStmt) {
        checkLVal(assignStmt.getLVal());
        checkExp(assignStmt.getExp());
    }

    public void checkBlockStmt(BlockStmt blockStmt) {
        ArrayList<BlockItem> blockItems = blockStmt.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            checkBlockItem(blockItem);
        }
    }

    public void checkBreakStmt(BreakStmt breakStmt) {
        if (loopDepth == 0) {
            errors.add(new IllegalBreakContinueException(breakStmt.getSemicolonLine()));
        }
    }

    public void checkContinueStmt(ContinueStatement continueStatement) {
        if (loopDepth == 0) {
            errors.add(new IllegalBreakContinueException(continueStatement.getSemicolonLine()));
        }
    }

    public void checkExpStmt(ExpStmt expStmt) {
        checkExp(expStmt.getExp());
    }

    public void checkGetIntStmt(GetIntStmt getIntStmt) {
        LVal lVal = getIntStmt.getLVal();
        Token ident = lVal.getIdent();
        Symbol symbol = currSymbolTable.getSymbol(ident.getContent(), true);
        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            return;
        }
        if (symbol.isConst()) {
            errors.add(new ModifyConstException(ident.getLine()));
        }
        if (lVal.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }
    }

    public void checkIfStmt(IfStmt ifStmt) {
        return;
    }

    public void checkPrintfStmt(PrintfStmt printfStmt) {
        if (!printfStmt.checkFormatString()) {
            errors.add(new IllegalSymbolException(printfStmt.getFormatString().getLine()));
        }
        if (!printfStmt.checkCountMatch()) {
            errors.add(new MismatchPrintfException(printfStmt.getPrintf().getLine()));
        }
        if (printfStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(printfStmt.getExps().get(printfStmt.getExps().size() - 1).getLine()));
        }
    }

    public void checkReturnStmt(ReturnStmt returnStmt) {
        // Token funcType = funcDef.getFuncType();
        // boolean returnInt = funcDef.returnInt();
        // if (funcType.getType() == TokenType.VOIDTK && returnInt) {
        //     errors.add(new IllegalReturnException(funcDef.getReturn().getLine()));
        // } else if (funcType.getType() == TokenType.INTTK && !returnInt) {
        //     errors.add(new MissReturnException(funcDef.getRightBrace().getLine()));
        // }
        return;
    }

    public void checkWhileStmt(WhileStmt whileStmt) {
        loopDepth++;
        if (whileStmt.missRightParenthesis()) {
            errors.add(new MissRparentException(whileStmt.getLine()));
        }
        checkStmt(whileStmt.getStmt());
    }

    public void checkConstExp(ConstExp constExp) {
        checkAddExp(constExp.getAddExp());
    }

    public void checkExp(Exp exp) {
        checkAddExp(exp.getAddExp());
    }

    public void checkAddExp(AddExp addExp) {
        checkMulExp(addExp.getFirstExp());
        ArrayList<MulExp> mulExps = addExp.getExps();
        for (MulExp mulExp : mulExps) {
            checkMulExp(mulExp);
        }
    }

    public void checkMulExp(MulExp mulExp) {
        checkUnaryExp(mulExp.getFirstExp());
        ArrayList<UnaryExp> unaryExps = mulExp.getExps();
        for (UnaryExp unaryExp : unaryExps) {
            checkUnaryExp(unaryExp);
        }
    }

    public void checkUnaryExp(UnaryExp unaryExp) {
        checkUnaryExpInterFace(unaryExp.getUnaryExpInterface());
    }

    public void checkUnaryExpInterFace(UnaryExpInterface unaryExpInterface) {
        if (unaryExpInterface instanceof PrimaryExp) {
            checkPrimaryExp((PrimaryExp) unaryExpInterface);
        } else if (unaryExpInterface instanceof FuncExp) {
            checkFuncExp((FuncExp) unaryExpInterface);
        } else if (unaryExpInterface instanceof UnaryExp) {
            checkUnaryExp((UnaryExp) unaryExpInterface);
        }
    }

    public void checkPrimaryExp(PrimaryExp primaryExp) {
        checkPrimaryExpInterFace(primaryExp.getPrimaryExpInterface());
    }

    public void checkPrimaryExpInterFace(PrimaryExpInterface primaryExpInterface) {
        if (primaryExpInterface instanceof BraceExp) {
            checkBraceExp((BraceExp) primaryExpInterface);
        } else if (primaryExpInterface instanceof LVal) {
            checkLVal((LVal) primaryExpInterface);
        } else if (primaryExpInterface instanceof Number) {
            checkNumber((Number) primaryExpInterface);
        }
    }

    public void checkBraceExp(BraceExp braceExp) {
        if (braceExp.missRightParenthesis()) {
            errors.add(new MissRparentException(braceExp.getLine()));
        }
        checkExp(braceExp.getExp());
    }

    public void checkLVal(LVal lVal) {
        Token ident = lVal.getIdent();
        Symbol symbol = currSymbolTable.getSymbol(ident.getContent(), true);
        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            return;
        }
        if (symbol.isConst()) {
            errors.add(new ModifyConstException(ident.getLine()));
        }
        if (lVal.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }
    }

    public void checkNumber(Number number) {
        return;
    }


    public void checkFuncExp(FuncExp funcExp) {
        if (funcExp.missRightParenthesis()) {
            errors.add(new MissRparentException(funcExp.getLine()));
        }
        Token ident = funcExp.getIdent();
        Symbol symbol = currSymbolTable.getSymbol(ident.getContent(), true);
        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            return;
        }
        // assert
        ArrayList<Symbol> Fparams = symbol.getParams();

    }
}

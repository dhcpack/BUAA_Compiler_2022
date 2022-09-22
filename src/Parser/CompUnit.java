package Parser;

import Config.IO;
import Parser.decl.types.Decl;
import Parser.func.types.FuncDef;
import Parser.func.types.MainFuncDef;

import java.util.ArrayList;

public class CompUnit implements Output{
    private final ArrayList<Decl> globalVariables;
    private final ArrayList<FuncDef> functions;
    private final MainFuncDef mainFunction;

    public CompUnit(ArrayList<Decl> globalVariables, ArrayList<FuncDef> functions, MainFuncDef mainFunction) {
        this.globalVariables = globalVariables;
        this.functions = functions;
        this.mainFunction = mainFunction;
    }

    @Override
    public void output(){
        globalVariables.forEach(Decl::output);
        functions.forEach(FuncDef::output);
        mainFunction.output();
        IO.print("<CompUnit>");
    }
}

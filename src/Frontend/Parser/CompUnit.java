package Frontend.Parser;

import Config.Output;
import Config.SyntaxWriter;
import Frontend.Parser.decl.types.Decl;
import Frontend.Parser.func.types.FuncDef;
import Frontend.Parser.func.types.MainFuncDef;

import java.io.IOException;
import java.util.ArrayList;

public class CompUnit implements Output {
    // 编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef
    private final ArrayList<Decl> globalVariables;
    private final ArrayList<FuncDef> functions;
    private final MainFuncDef mainFunction;

    public CompUnit(ArrayList<Decl> globalVariables, ArrayList<FuncDef> functions, MainFuncDef mainFunction) {
        this.globalVariables = globalVariables;
        this.functions = functions;
        this.mainFunction = mainFunction;
    }

    public ArrayList<Decl> getGlobalVariables() {
        return globalVariables;
    }

    public ArrayList<FuncDef> getFunctions() {
        return functions;
    }

    public MainFuncDef getMainFunction() {
        return mainFunction;
    }

    @Override
    public void output() {
        for (Decl decl : globalVariables) {
            SyntaxWriter.print(decl.toString());
        }
        for (FuncDef funcDef : functions) {
            SyntaxWriter.print(funcDef.toString());
        }
        SyntaxWriter.print(mainFunction.toString());
        SyntaxWriter.print("<CompUnit>\n");
        try {
            SyntaxWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

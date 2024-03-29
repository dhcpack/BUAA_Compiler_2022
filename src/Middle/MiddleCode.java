package Middle;

import Config.MiddleWriter;
import Config.Output;
import Frontend.Symbol.Symbol;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.Jump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class MiddleCode implements Output {
    private final LinkedHashMap<String, Integer> nameToAddr = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> nameToVal = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> nameToAsciiz = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> AsciizToName = new LinkedHashMap<>();
    private final LinkedHashMap<String, ArrayList<Integer>> nameToArray = new LinkedHashMap<>();
    private final LinkedHashMap<String, FuncBlock> nameToFunc = new LinkedHashMap<>();
    private int AsciizNum = 1;

    public void addInt(String name, int addr, int initVal) {
        this.nameToAddr.put(name, addr);
        this.nameToVal.put(name, initVal);
    }

    public String addAsciiz(String content) {
        if (AsciizToName.containsKey(content)) {
            return AsciizToName.get(content);
        } else {
            String name = "STR_" + AsciizNum++;
            this.nameToAsciiz.put(name, content);
            this.AsciizToName.put(content, name);
            return name;
        }
    }

    public LinkedHashMap<String, Integer> getNameToAddr() {
        return nameToAddr;
    }

    public LinkedHashMap<String, Integer> getNameToVal() {
        return nameToVal;
    }

    public LinkedHashMap<String, String> getNameToAsciiz() {
        return nameToAsciiz;
    }

    public LinkedHashMap<String, String> getAsciizToName() {
        return AsciizToName;
    }

    public LinkedHashMap<String, ArrayList<Integer>> getNameToArray() {
        return nameToArray;
    }

    public LinkedHashMap<String, FuncBlock> getNameToFunc() {
        return nameToFunc;
    }

    public String getAsciizName(String content) {
        return this.AsciizToName.get(content);
    }

    public void addArray(String name, int addr, ArrayList<Integer> initVal) {
        this.nameToAddr.put(name, addr);
        this.nameToArray.put(name, initVal);
    }

    public void addFunc(FuncBlock funcBlock) {
        this.nameToFunc.put(funcBlock.getFuncName(), funcBlock);
    }

    public FuncBlock getFunc(String funcName) {
        return this.nameToFunc.get(funcName);
    }

    public boolean checkIsTest4() {
        int cnt = 0;
        for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> funcAndBlock : funcToSortedBlock.entrySet()) {
            for (BasicBlock block : funcAndBlock.getValue()) {
                for (BlockNode blockNode : block.getContent()) {
                    if (blockNode instanceof FourExpr && ((FourExpr) blockNode).getOp() == FourExpr.ExprOp.MUL) {
                        cnt++;
                    }
                }
            }
        }
        return cnt > 50000;
    }

    @Override
    public void output() {
        MiddleWriter.print("###### GLOBAL STRING ######");
        for (Map.Entry<String, String> nameAsciiz : nameToAsciiz.entrySet()) {
            MiddleWriter.print(String.format("%s : %s", nameAsciiz.getKey(), nameAsciiz.getValue()));
        }
        MiddleWriter.print("\n");

        MiddleWriter.print("###### GLOBAL ARRAY ######");
        for (Map.Entry<String, ArrayList<Integer>> nameArray : nameToArray.entrySet()) {
            String name = nameArray.getKey();
            StringJoiner initVal = new StringJoiner(" ");
            for (Integer val : nameArray.getValue()) {
                initVal.add(String.valueOf(val));
            }
            MiddleWriter.print(String.format("[0x%x]array %s: %s", nameToAddr.get(name), name, initVal));
        }
        MiddleWriter.print("\n");

        MiddleWriter.print("###### GLOBAL VAR ######");
        for (Map.Entry<String, Integer> nameVar : nameToVal.entrySet()) {
            MiddleWriter.print(String.format("[0x%x]%s: %d", nameToAddr.get(nameVar.getKey()), nameVar.getKey(),
                    nameVar.getValue()));
        }
        MiddleWriter.print("\n");

        MiddleWriter.print("###### TEXT ######");
        MiddleWriter.print("JUMP FUNC_main");

        if (visited.size() == 0) {
            getBlocks();
        }
        for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> funcAndBlock : funcToSortedBlock.entrySet()) {
            FuncBlock funcBlock = funcAndBlock.getKey();
            MiddleWriter.print(
                    String.format("# func %s : stack size 0x%x", funcBlock.getFuncName(), funcBlock.getStackSize()));
            StringJoiner params = new StringJoiner(", ");
            for (Symbol param : funcBlock.getParams()) {
                params.add(param.toString());
            }
            MiddleWriter.print(String.format("# param: %s", params));
            for (BasicBlock block : funcAndBlock.getValue()) {
                MiddleWriter.print(block.getLabel() + ":");
                for (BlockNode blockNode : block.getContent()) {
                    MiddleWriter.print("\t" + blockNode);
                }
            }
        }
        try {
            MiddleWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private final HashSet<BasicBlock> visited = new HashSet<>();
    private final LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = new LinkedHashMap<>();

    // 通过dfs得到每个函数包含的基本块并排序
    public void getBlocks() {
        for (FuncBlock funcBlock : nameToFunc.values()) {
            BasicBlock body = funcBlock.getBody();
            ArrayList<BasicBlock> sortedBlock = new ArrayList<>();
            dfsBlock(body, sortedBlock);
            sortedBlock.sort(BasicBlock::compareTo);
            funcToSortedBlock.put(funcBlock, sortedBlock);
            funcBlock.setBlocks(sortedBlock);
        }
    }

    private void dfsBlock(BasicBlock block, ArrayList<BasicBlock> sortedBlock) {
        if (visited.contains(block)) {
            return;
        }
        visited.add(block);
        sortedBlock.add(block);
        for (BlockNode blockNode : block.getContent()) {
            if (blockNode instanceof Jump) {
                BasicBlock target = ((Jump) blockNode).getTarget();
                dfsBlock(target, sortedBlock);
            } else if (blockNode instanceof Branch) {
                BasicBlock thenBlock = ((Branch) blockNode).getThenBlock();
                BasicBlock elseBlock = ((Branch) blockNode).getElseBlock();
                if (((Branch) blockNode).isThenFirst()) {
                    dfsBlock(thenBlock, sortedBlock);
                    dfsBlock(elseBlock, sortedBlock);
                } else {
                    dfsBlock(elseBlock, sortedBlock);
                    dfsBlock(thenBlock, sortedBlock);
                }
            }
        }
    }

    // private final HashMap<Symbol, Integer> symbolUsageMap = new HashMap<>();

    public LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> getFuncToSortedBlock() {
        if (visited.size() == 0) {
            getBlocks();
        }
        return this.funcToSortedBlock;
    }

    // public HashMap<Symbol, Integer> getSymbolUsageMap() {
    //     if (visited.size() == 0) {
    //         getBlocks();
    //     }
    //     return this.symbolUsageMap;
    // }
}

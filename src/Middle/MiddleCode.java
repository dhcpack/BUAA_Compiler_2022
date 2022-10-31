package Middle;

import BackEnd.instructions.J;
import Config.MiddleWriter;
import Config.Output;
import Frontend.Symbol.Symbol;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FuncBlock;
import Middle.type.Jump;
import Middle.type.Operand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.StringJoiner;

public class MiddleCode implements Output {
    private final HashMap<String, Integer> nameToAddr = new HashMap<>();
    private final HashMap<String, Integer> nameToVal = new HashMap<>();
    private final HashMap<String, String> nameToAsciiz = new HashMap<>();
    private final HashMap<String, String> AsciizToName = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> nameToArray = new HashMap<>();
    private final HashMap<String, FuncBlock> nameToFunc = new HashMap<>();
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

    public HashMap<String, Integer> getNameToAddr() {
        return nameToAddr;
    }

    public HashMap<String, Integer> getNameToVal() {
        return nameToVal;
    }

    public HashMap<String, String> getNameToAsciiz() {
        return nameToAsciiz;
    }

    public HashMap<String, String> getAsciizToName() {
        return AsciizToName;
    }

    public HashMap<String, ArrayList<Integer>> getNameToArray() {
        return nameToArray;
    }

    public HashMap<String, FuncBlock> getNameToFunc() {
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


    private HashSet<BasicBlock> visited = new HashSet<>();
    private LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = new LinkedHashMap<>();

    // 通过dfs得到每个函数包含的基本块并排序
    public void getBlocks() {
        for (FuncBlock funcBlock : nameToFunc.values()) {
            BasicBlock body = funcBlock.getBody();
            ArrayList<BasicBlock> sortedBlock = new ArrayList<>();
            dfsBlock(body, sortedBlock);
            sortedBlock.sort(BasicBlock::compareTo);
            funcToSortedBlock.put(funcBlock, sortedBlock);
        }
    }

    private void dfsBlock(BasicBlock block, ArrayList<BasicBlock> sortedBlock) {
        if (visited.contains(block)) {
            return;
        }
        visited.add(block);
        sortedBlock.add(block);
        for (Operand operand : block.getOperandUsage()) {
            if (operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.TEMP && !((Symbol) operand).hasAddress()) {
                Symbol symbol = (Symbol) operand;
                if (symbolUsageMap.containsKey(symbol)) {
                    symbolUsageMap.put(symbol, symbolUsageMap.get(symbol) + 1);
                } else {
                    symbolUsageMap.put(symbol, 1);
                }
            }
        }
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

    private final HashMap<Symbol, Integer> symbolUsageMap = new HashMap<>();

    public LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> getFuncToSortedBlock() {
        if (visited.size() == 0) {
            getBlocks();
        }
        return this.funcToSortedBlock;
    }

    public HashMap<Symbol, Integer> getSymbolUsageMap() {
        if (visited.size() == 0) {
            getBlocks();
        }
        return this.symbolUsageMap;
    }
}

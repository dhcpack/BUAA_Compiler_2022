package Middle;

import Middle.type.FuncBlock;

import java.util.ArrayList;
import java.util.HashMap;

public class MiddleCode {
    private final HashMap<String, Integer> nameToAddr = new HashMap<>();
    private final HashMap<String, Integer> nameToVal = new HashMap<>();
    private final HashMap<String, String> nameToAsciiz = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> nameToArray = new HashMap<>();
    private final HashMap<String, FuncBlock> nameToFunc = new HashMap<>();
    private int AsciizNum = 1;

    public void addInt(String name, int addr, int initVal) {
        this.nameToAddr.put(name, addr);
        this.nameToVal.put(name, initVal);
    }

    public void addAsciiz(String content) {
        this.nameToAsciiz.put("STR_" + AsciizNum++, content);
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
}

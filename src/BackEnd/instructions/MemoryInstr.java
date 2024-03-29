package BackEnd.instructions;

public class MemoryInstr implements Instruction{
    public enum MemoryType {
        lw,
        sw,
    }

    private final InstructionType type = InstructionType.MEM;

    private final MemoryType memoryType;
    private final int rBase;
    private final int offset;
    private final int reg;

    public MemoryInstr(MemoryType memoryType, int rBase, int offset, int reg) {
        this.memoryType = memoryType;
        this.rBase = rBase;
        this.offset = offset;
        this.reg = reg;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        if(offset>=0){
            return String.format("%s $%d, 0x%x($%d)\n", memoryType.name(), reg, offset, rBase);
        } else {
            return String.format("%s $%d, -0x%x($%d)\n", memoryType.name(), reg, -offset, rBase);
        }
    }
}

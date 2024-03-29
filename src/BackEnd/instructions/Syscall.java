package BackEnd.instructions;

public class Syscall implements Instruction {
    public final InstructionType type = InstructionType.SYSCALL;

    public static final int print_int = 1;
    public static final int print_str = 4;
    public static final int get_int = 5;
    public static final int exit = 10;

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return "syscall\n";
    }
}

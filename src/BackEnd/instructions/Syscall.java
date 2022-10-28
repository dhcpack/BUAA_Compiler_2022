package BackEnd.instructions;

public class Syscall implements Instruction {
    public static final Integer print_int = 1;
    public static final Integer print_str = 4;
    public static final Integer get_int = 5;
    public static final Integer exit = 10;

    @Override
    public String toString() {
        return "syscall\n";
    }
}

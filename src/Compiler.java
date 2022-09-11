import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IO.output(Parser.parse(IO.input()).toString());
    }
}

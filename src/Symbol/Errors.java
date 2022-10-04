package Symbol;

import java.util.ArrayList;

import Config.IO;
import Exceptions.MyException;


public class Errors {
    private final ArrayList<MyException> exceptions = new ArrayList<MyException>();

    public void add(MyException exception) {
        this.exceptions.add(exception);
    }

    public void output() {
        for (MyException exception : exceptions) {
            IO.print(exception.getLine() + " " + exception.getType().getCode());
        }
    }
}

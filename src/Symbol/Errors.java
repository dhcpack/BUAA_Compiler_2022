package Symbol;

import java.util.ArrayList;
import java.util.Comparator;

import Config.IO;
import Exceptions.MyException;


public class Errors {
    private final ArrayList<MyException> exceptions = new ArrayList<>();

    public void add(MyException exception) {
        this.exceptions.add(exception);
    }

    public void output() {
        exceptions.sort(new Comparator<MyException>() {
            @Override
            public int compare(MyException o1, MyException o2) {
                if (o1.getLine() != o2.getLine()) {
                    return o1.getLine() - o2.getLine();
                }
                return o1.getType().getCode().compareTo(o2.getType().getCode());
            }
        });
        for (MyException exception : exceptions) {
            IO.print(exception.getLine() + " " + exception.getType().getCode());
        }
    }
}

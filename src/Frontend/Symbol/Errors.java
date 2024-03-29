package Frontend.Symbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import Config.ErrorWriter;
import Config.Reader;
import Exceptions.MyException;


public class Errors {
    private ArrayList<MyException> exceptions = new ArrayList<>();

    public boolean hasErrors() {
        return this.exceptions.size() != 0;
    }

    public int getErrorCount() {
        return this.exceptions.size();
    }

    public void add(MyException exception) {
        this.exceptions.add(exception);
    }

    public void output() {
        ArrayList<MyException> newExceptions = new ArrayList<>();
        for (MyException exception : exceptions) {
            boolean flag = true;
            for (MyException existException : newExceptions) {
                if (exception.getLine() == existException.getLine() && exception.getType() == existException.getType()) {
                    flag = false;
                }
            }
            if (flag) {
                newExceptions.add(exception);
            }
        }
        exceptions = newExceptions;
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
            ErrorWriter.print(exception.getLine() + " " + exception.getType().getCode());
            System.err.printf("%d %s\n", exception.getLine(), exception.getType().getCode());
        }
        try {
            ErrorWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

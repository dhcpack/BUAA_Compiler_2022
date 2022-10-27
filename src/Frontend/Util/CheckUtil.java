package Frontend.Util;

import Config.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CheckUtil {
    public static void check() throws IOException {
        System.out.println();
        System.out.println("-----Check Begin-----");
        File outputFile = new File(Config.mipsFile);
        File expectFile = new File(Config.expectedFile);
        BufferedReader out = new BufferedReader(new FileReader(outputFile));
        BufferedReader expect = new BufferedReader(new FileReader(expectFile));
        String o, e;
        int row = 1;
        boolean flag = true;
        while ((e = expect.readLine()) != null) {
            if ((o = out.readLine()) == null) {
                flag = false;
                System.out.printf("Your output is shorter at Line %d\n", row);
                break;
            }
            if (!o.equals(e)) {
                flag = false;
                System.out.printf("Line %d\n", row);
                System.out.printf("Expected %s\n", e);
                System.out.printf("Got %s\n", o);
                break;
            }
            row++;
        }
        if (flag && (o = out.readLine()) != null) {
            flag = false;
            System.out.printf("Your output is longer at Line %d\n", row);
        }
        if (flag) {
            System.out.println("Accepted");
        }
        System.out.println("-----Check End-----");
    }
}

package Test;

import Config.Config;
import Config.ErrorWriter;
import Config.MiddleWriter;
import Config.MipsWriter;
import Config.TestWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class MipsTest {
    private final String testPath;
    private final String testfile;
    private final String input;
    private final String expect;
    private final String marsPath = "./Mars2022.jar";

    public MipsTest(int year, String level, int point) {
        this.testPath = "./" + year + "/" + level + "/";
        this.testfile = testPath + "testfile" + point + ".txt";
        this.input = testPath + "input" + point + ".txt";
        this.expect = testPath + "output" + point + ".txt";
    }

    public MipsTest(String path, int point) {
        this.testPath = "./" + path + "/";
        this.testfile = testPath + "testfile" + point + ".txt";
        this.input = testPath + "input" + point + ".txt";
        this.expect = testPath + "output" + point + ".txt";
    }

    public boolean run() throws IOException {
        ErrorWriter.string = "";
        MiddleWriter.string = "";
        MipsWriter.string = "";

        Config.mipsRun(this.testfile);

        Process mars = Runtime.getRuntime().exec("java -jar " + marsPath + " nc " + "mips.txt");
        System.out.println("java -jar " + marsPath + " nc " + "mips.txt");
        OutputStream stdin = mars.getOutputStream();
        InputStream inputFile = Files.newInputStream(Paths.get(this.input));
        byte[] buffer = new byte[2048];
        int size;
        while ((size = inputFile.read(buffer)) != -1) {
            for (int i = 0; i < size; i++)
                if (buffer[i] == ' ') {
                    buffer[i] = '\n';
                }

            stdin.write(buffer, 0, size);
        }
        stdin.close();
        try {
            mars.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        InputStream stdout = mars.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
        ArrayList<String> output = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                output.add(line);
            }
        }
        stdout.close();

        InputStream expectedFile = Files.newInputStream(Paths.get(this.expect));
        reader = new BufferedReader(new InputStreamReader(expectedFile));
        ArrayList<String> expected = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                expected.add(line);
            }
        }
        // stdin.close();
        // stdout.close();
        inputFile.close();
        expectedFile.close();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // check
        if (output.size() < expected.size()) {
            TestWriter.print("Your output is shorter than we expected");
            TestWriter.print("EXPECT: " + expected.size() + "line(s)");
            TestWriter.print("OUTPUT: " + output.size() + "line(s)");
            TestWriter.print("\n");
            TestWriter.print("EXPECT: ");
            for (String s : expected) {
                TestWriter.print("\t" + s);
            }
            TestWriter.print("\n");
            TestWriter.print("OUTPUT: ");
            for (String s : output) {
                TestWriter.print("\t" + s);
            }
            TestWriter.print("\n");
            TestWriter.print("TEST END AT " + simpleDateFormat.format(new Date()));
            return false;
        } else if (output.size() > expected.size()) {
            TestWriter.print("Your output is longer than we expected");
            TestWriter.print("EXPECT: " + expected.size() + "line(s)");
            TestWriter.print("OUTPUT: " + output.size() + "line(s)");
            TestWriter.print("\n");
            TestWriter.print("EXPECT: ");
            for (String s : expected) {
                TestWriter.print("\t" + s);
            }
            TestWriter.print("\n");
            TestWriter.print("OUTPUT: ");
            for (String s : output) {
                TestWriter.print("\t" + s);
            }
            TestWriter.print("\n");
            TestWriter.print("TEST END AT " + simpleDateFormat.format(new Date()));
            return false;
        }
        for (int i = 0; i < expected.size(); i++) {
            System.out.println("EXPECT: " + expected.get(i));
            System.out.println("OUTPUT: " + output.get(i));
            if (!expected.get(i).equals(output.get(i))) {
                TestWriter.print("Line " + (i + 1) + ": ");
                TestWriter.print("We get: " + output.get(i));
                TestWriter.print("But we expected: " + expected.get(i));
                TestWriter.print("TEST END AT " + simpleDateFormat.format(new Date()));
                return false;
            }
        }
        TestWriter.print("ACCEPTED: " + simpleDateFormat.format(new Date()));
        return true;
    }


}

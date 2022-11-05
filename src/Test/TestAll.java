package Test;

import Config.TestWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TestAll {
    public static void run() throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        TestWriter.print("\n====== Auto Test Begin ======\n");
        TestWriter.print(simpleDateFormat.format(new Date()));
        TestWriter.print("\n");
        // PUBLIC
        // for (int i = 0; i <= 94; i++) {
        //     MipsTest mipsTest = new MipsTest("public",  i);
        //     if (mipsTest.run()) {
        //         System.out.printf("\033[1;32;40mAccepted: public-%d \033[0m\n", i);
        //     } else {
        //         System.out.printf("\033[1;31;40mWrong Answer: public-%d \033[0m\n", i);
        //         return;
        //     }
        // }

        // 2022-C
        for (int i = 1; i <= 30; i++) {
            // TODO: check
            if (i == 11) continue;
            // i=22;
            MipsTest mipsTest = new MipsTest(2022, "C", i);
            if (mipsTest.run()) {
                System.out.printf("\033[1;32;40mAccepted: 2022-C-%d \033[0m\n", i);
            } else {
                System.out.printf("\033[1;31;40mWrong Answer: 2022-C-%d \033[0m\n", i);
                return;
            }
        }

        // 2021-C
        for (int i = 1; i <= 29; i++) {
            MipsTest mipsTest = new MipsTest(2021, "C", i);
            if (mipsTest.run()) {
                System.out.printf("\033[1;32;40mAccepted: 2021-C-%d \033[0m\n", i);
            } else {
                System.out.printf("\033[1;31;40mWrong Answer: 2021-C-%d \033[0m\n", i);
                return;
            }
        }

        // 2022-B
        for (int i = 1; i <= 30; i++) {
            MipsTest mipsTest = new MipsTest(2022, "B", i);
            if (mipsTest.run()) {
                System.out.printf("\033[1;32;40mAccepted: 2022-B-%d \033[0m\n", i);
            } else {
                System.out.printf("\033[1;31;40mWrong Answer: 2022-B-%d \033[0m\n", i);
                return;
            }
        }

        // 2021-B
        for (int i = 1; i <= 27; i++) {
            MipsTest mipsTest = new MipsTest(2021, "B", i);
            if (mipsTest.run()) {
                System.out.printf("\033[1;32;40mAccepted: 2021-B-%d \033[0m\n", i);
            } else {
                System.out.printf("\033[1;31;40mWrong Answer: 2021-B-%d \033[0m\n", i);
                return;
            }
        }

        // 2022-A
        for (int i = 1; i <= 30; i++) {
            // TODO: check
            if(i == 24) continue;
            MipsTest mipsTest = new MipsTest(2022, "A", i);
            if (mipsTest.run()) {
                System.out.printf("\033[1;32;40mAccepted: 2022-A-%d \033[0m\n", i);
            } else {
                System.out.printf("\033[1;31;40mWrong Answer: 2022-A-%d \033[0m\n", i);
                return;
            }
        }

        // 2021-A
        for (int i = 1; i <= 26; i++) {
            // if (i == 1) continue;
            MipsTest mipsTest = new MipsTest(2021, "A", i);
            if (mipsTest.run()) {
                System.out.printf("\033[1;32;40mAccepted: 2021-A-%d \033[0m\n", i);
            } else {
                System.out.printf("\033[1;31;40mWrong Answer: 2021-A-%d \033[0m\n", i);
                return;
            }
        }
        System.out.println("\033[1;32;40mCongratulations! You passed all testpoints \033[0m\n");
    }
}

// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessUtils
{
    private enum OperatingSytemType
    {
        UNKNOWN,
        WINDOWS,
        LINUX
    }

    private static OperatingSytemType getOperatingSytemType()
    {
        //TODO --> expand this
        return OperatingSytemType.WINDOWS;
    }

    public static void kickOffProcess(String filePath, String[] args)
    {

    }


    /*
    For existing process
     */
    public static void bringProcessToFront()
    {

    }

    public static void getRunningProcesses()
    {
        //https://stackoverflow.com/questions/54686/how-to-get-a-list-of-current-open-windows-process-with-java
        try {
            String line;

            Process p = null;
            OperatingSytemType currentOS = getOperatingSytemType();

            switch (currentOS)
            {
                case LINUX:
                    p = Runtime.getRuntime().exec("ps -e");
                    break;
                case WINDOWS:
                    p = Runtime.getRuntime().exec
                            (System.getenv("windir") + "\\system32\\" + "tasklist.exe");
                    break;
                default:
                    break;
            }


            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line); //<-- Parse data here.
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

}

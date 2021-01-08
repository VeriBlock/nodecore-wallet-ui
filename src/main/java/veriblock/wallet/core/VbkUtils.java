// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import org.veriblock.core.utilities.Utility;

import java.math.BigDecimal;

public class VbkUtils {

    //Store/Calculate --> LONG
    //Display to User, Get from user --> String

    public static String getEmptyVbkString()
    {
        return "0.00000000";
    }

    public static String convertAtomicToVbkString(Long atomicUnits) {
        if (atomicUnits == null)
        {
            return "";
        }
        return Utility.formatAtomicLongWithDecimal(atomicUnits);
    }

    //From NC_CLI (could move into VBK core)
    public static long convertDecimalCoinToAtomicLong(String toConvert) {


        if (!toConvert.contains(".")) {
            toConvert = toConvert + ".";
        }

        if (toConvert.charAt(0) == '.') {
            toConvert = "0" + toConvert;
        }

        int numCharactersAfterDecimal = toConvert.length() - toConvert.indexOf(".") - 1;

        if (numCharactersAfterDecimal > 8) {
            throw new IllegalArgumentException("convertDecimalCoinToAtomicLong cannot be called with a String with more than 8 numbers after the decimal point!");
        }

        toConvert = toConvert.replace(".", "");

        for (int i = 8; i > numCharactersAfterDecimal; i--) {
            toConvert += "0";
        }

        return Long.parseLong(toConvert);
    }

    /*
    won't handle fee (small) / sentAmount (big) = very small number
    public static double getPercentLong(Long num1, Long num2)
    {
        if (num1 == 0)
            return 0;
        if (num2 == 0) {
            //really a match error, but don't crash
            return 0;
        }

        BigDecimal d1 = new BigDecimal(num1);
        BigDecimal d2 = new BigDecimal(num2);

        BigDecimal result = d1.divide(d2);
        return (double)result.floatValue();
    }
    */
}

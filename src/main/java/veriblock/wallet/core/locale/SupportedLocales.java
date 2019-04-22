// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.locale;

import java.util.Locale;

public class SupportedLocales {

    //https://www.oracle.com/technetwork/java/javase/java8locales-2095355.html

    public enum SupportedLocale {
        UNKNOWN,
        TEST,  //TEST
        en_US,
        ro_RO,
        it_IT,
        zh_CN,
        hi_IN
    }

    public static SupportedLocale convertStringLocal(String localeString)
    {
        switch (localeString.toLowerCase()) {
            case "test":
                return SupportedLocale.TEST;
            case "en":
                return SupportedLocale.en_US;
            case "ro":
                return SupportedLocale.ro_RO;
            case "it":
                return SupportedLocale.it_IT;
            case "zh":
                return SupportedLocale.zh_CN;
            case "hi":
                return SupportedLocale.hi_IN;
            default:
                return SupportedLocale.UNKNOWN;
        }
    }

    public static java.util.Locale convertToJavaLocale(SupportedLocale supportedLocale) {
        switch (supportedLocale) {
            case it_IT:
                return Locale.ITALIAN;

            case zh_CN:
                return Locale.CHINESE;

            default:
                //hi_IN
                //ro_RO
                return Locale.ENGLISH;
        }
    }
}

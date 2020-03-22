// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.locale;

public class LocaleItem
{
    public LocaleItem()
    {

    }

    public LocaleItem(String displayText, SupportedLocales.SupportedLocale locale)
    {
        this.DisplayText= displayText;
        this.Locale = locale;
    }

    public String DisplayText;
    public SupportedLocales.SupportedLocale Locale;
}

// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

public interface Exportable
{
    /*
    Pass in isHeader to keep the row creation and header all in one place, for easier maintenance
     */
    String createCsvRow(boolean isHeader);
}

// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

public class PendingTransactionEntity {
    public PendingTransactionEntity()
    {

    }

    public String txId;
    public TransactionType txType;
    public int sizeBytes;

    //615670
    public long totalFeeAtomic;

    //4376133270
    public long sourceAmountAtomic;

    //4375517600
    public long getOutputAtomic()
    {
        return sourceAmountAtomic - totalFeeAtomic;
    }

    public void setTransactionType(String input)
    {
        if (input == null || input.length() == 0)
        {
            this.txType = TransactionType.UNKNOWN;
            return;
        }
        input = input.toUpperCase();
        if (input.equals("PROOF_OF_PROOF"))
        {
            this.txType = TransactionType.PROOF_OF_PROOF;
        }
        else if (input.equals("STANDARD"))
        {
            this.txType = TransactionType.STANDARD;
        }
        else
        {
            this.txType = TransactionType.UNKNOWN;
        }
    }
}

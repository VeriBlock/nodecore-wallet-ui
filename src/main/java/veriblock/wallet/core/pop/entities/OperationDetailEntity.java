// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop.entities;

import veriblock.wallet.core.pop.PopAction;
import veriblock.wallet.core.pop.PopStatus;

public class OperationDetailEntity {

    public String operationId;
    public PopStatus status;
    public PopAction currentAction;

    public String btcTransactionId;
    public String merklePath;
    public String detail;
    public String vbkPopTransactionId;

    public String opReturnHex;
    public String vbkMinerAddress;

    public boolean isSuccess()
    {
        if (this.status != PopStatus.FAILED && this.status != PopStatus.UNKNOWN)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public int getWorkFlowNumber() {
        switch (this.currentAction) {
            case READY:
                return 1;
            case PUBLICATION_DATA:
                return 2;
            case TRANSACTION:
                return 3;
            case WAIT:
                return 4;
            case PROOF:
                return 5;
            case CONTEXT:
                return 6;
            case SUBMIT:
                return 7;
            case CONFIRM:
                return 8;
            case DONE:
                return 9;
            default:
                return 0;
        }
    }

    public boolean isErrorMustUnlockWallet()
    {
        //detail = "PoP Failed\n\tWallet must be unlocked to submit a PoP transaction\n"
        if (this.detail != null && this.detail.toLowerCase().contains("wallet must be unlocked"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /*
{
    "operationId": "ce3c3e8b",
    "status": "RUNNING",
    "currentAction": "WAIT",
    "miningInstruction": {
        "publicationData": [
            ...
        ],
        "endorsedBlockHeader": [
            ...
        ],
        "lastBitcoinBlock": [
            ...
        ],
        "minerAddress": [
            ...
        ],
        "endorsedBlockContextHeaders": [
            [
                ...
            ]
        ]
    },
    "transaction": [
        ...
    ],
    "submittedTransactionId": "2897ba47dfed819879ebe3c5cdbb17e56fcc15d49e3cb42331d2c683b949aeb3",
    "merklePath": "",
    "detail": "",
    "popTransactionId": ""
}
     */
}

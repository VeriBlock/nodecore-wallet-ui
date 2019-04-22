// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop.entities;

import veriblock.wallet.core.pop.PopStatus;

public class OperationEntity
{
    public String operationId;

    //listoperations shows as "state", but keep consistent
    public PopStatus status;

    public String actionMessage;
    public int endorsedBlockNumber;
    public String message;
}



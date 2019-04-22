// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.syncEstimator;

import veriblock.wallet.core.Utils;

import javax.rmi.CORBA.Util;

public class SyncResult {
    public SyncResult()
    {}

    public SyncHeight SyncFinalHeight;
    public SyncStatusEnum Status;
    public int LocalSyncRateBlocksPerSecond;
    public int MinutesToSync;

    public SyncHeight OldestHeight;
    public SyncHeight MostCurrentHeight;

    public int getSecondDuration()
    {
        if (this.OldestHeight == null || this.MostCurrentHeight == null)
        {
            return -1;
        }
        else
        {
            return Utils.getSecondsDiff(this.OldestHeight.Timestamp, this.MostCurrentHeight.Timestamp);
        }
    }

}

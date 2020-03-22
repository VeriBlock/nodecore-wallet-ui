// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.syncEstimator;

import org.joda.time.DateTime;

public class SyncHeight
{
    public SyncHeight()
    {

    }

    public SyncHeight(int localHeight, int networkHeight)
    {
        this.LocalHeight = localHeight;
        this.NetworkHeight = networkHeight;
        this.Timestamp = DateTime.now();
    }

    public SyncHeight(int localHeight, int networkHeight, DateTime timestamp)
    {
        this.LocalHeight = localHeight;
        this.NetworkHeight = networkHeight;
        this.Timestamp = timestamp;
    }

    public int LocalHeight;
    public int NetworkHeight;
    public DateTime Timestamp;

    @Override
    public String toString() {
        return String.format("LocalHeight=%1$s, NetworkHeight=%2$s, Timestamp=%3$s",
                this.LocalHeight, this.NetworkHeight, this.Timestamp);
    }
}


// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

public enum ConnectionState {

    NotSpecified,
    ErrorCouldNotConnect,
    Connected_Syncing,

    //All caught up
    Connected_Synced

}

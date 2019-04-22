// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

public enum WalletLockState {

    //
    UNKNOWN,

    /*
    Wallet is encrypted and locked
     */
    LOCKED,

    /*
    Wallet is encrypted and unlocked
     */
    UNLOCKED,

    /*
    Wallet is not encrypted
     */
    DEFAULT
}

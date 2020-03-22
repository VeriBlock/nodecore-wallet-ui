// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop;

public class ConfigConstants {

    public enum Key
    {
        BITCOIN_FEE_MAX,
        BITCOIN_FEE_PERKB,

        AUTO_MINE_ROUND1,
        AUTO_MINE_ROUND2,
        AUTO_MINE_ROUND3,
        AUTO_MINE_ROUND4,
    }

    public static String getKeyString(Key configKey)
    {
        switch (configKey)
        {
            case BITCOIN_FEE_MAX:
                return "bitcoin.fee.max";
            case BITCOIN_FEE_PERKB:
                return "bitcoin.fee.perkb";

            case AUTO_MINE_ROUND1:
                return "auto.mine.round1";
            case AUTO_MINE_ROUND2:
                return "auto.mine.round2";
            case AUTO_MINE_ROUND3:
                return "auto.mine.round3";
            case AUTO_MINE_ROUND4:
                return "auto.mine.round4";

            default:
                return null;
        }
    }

}

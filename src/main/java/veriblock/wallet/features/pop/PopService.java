// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.pop;

import veriblock.wallet.core.pop.ApiProxy;

public class PopService {

    public static String getPopHelpLink()
    {
        String helpLink = "https://wiki.veriblock.org/index.php?title=HowTo_run_PoP_Miner";
        return  helpLink;
    }

    public static ApiProxy getApiProxy()
    {
        return new ApiProxy("http://localhost:8080");
    }

    public static long getMinimumBtcBalance()
    {
        return 8000;
    }

    public static int rewardPaidInXBlocks()
    {
        return 500;
    }

    public static int getRoundNumber(int blockHeight)
    {
        //return 1 - 4
        //mod to 20
        int remainder = blockHeight % 20;

        if (remainder == 1 || remainder ==  4 || remainder ==  7 || remainder == 10 || remainder ==  13
                || remainder == 16 || remainder == 19)
        {
            return 1;
        }

        if (remainder == 2 || remainder ==  5 || remainder ==  8 || remainder == 11 || remainder ==  14
                || remainder == 17)
        {
            return 2;
        }

        if (remainder == 3 || remainder ==  6 || remainder ==  9 || remainder == 12 || remainder ==  15
                || remainder == 18)
        {
            return 3;
        }

        return 4;
    }

    public static int getRecentRewardsSearchLength()
    {
        return 750;
    }

    public static String getMinimumPopVersion()
    {
        return "0.3.12";
    }
}

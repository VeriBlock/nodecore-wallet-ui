// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.pop;

public class AutoMineModel {

    public AutoMineModel()
    {
        shouldMineRound1 = false;
        shouldMineRound2 = false;
        shouldMineRound3 = false;
        shouldMineRound4 = false;
    }

    public boolean shouldMineRound1;
    public boolean shouldMineRound2;
    public boolean shouldMineRound3;
    public boolean shouldMineRound4;

    public boolean areNoRoundsSelected() {
        if (getMinedBlockCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean areAllRoundsSelected() {
        if (shouldMineRound1 && shouldMineRound2 && shouldMineRound3 && shouldMineRound4)
            return true;
        else
            return false;

    }

    public int getMinedBlockCount()
    {
        int i = 0;
        if (shouldMineRound1)
        {
            i = i + 7;
        }

        if (shouldMineRound2)
        {
            i = i + 6;
        }


        if (shouldMineRound3)
        {
            i = i + 6;
        }

        if (shouldMineRound4)
        {
            i = i + 1;
        }
        return i;
    }

}

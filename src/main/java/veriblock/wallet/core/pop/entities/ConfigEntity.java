// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop.entities;

import java.util.ArrayList;
import java.util.List;

public class ConfigEntity {

    public long bitcoinFeeMax;
    public long bitcoinFeePerKb;

    public boolean autoMineRound1;
    public boolean autoMineRound2;
    public boolean autoMineRound3;
    public boolean autoMineRound4;

    public List<Integer> getAutoMineRounds()
    {
        List<Integer> aint = new ArrayList<>();

        if (this.autoMineRound1)
        {
            aint.add(1);
        }

        if (this.autoMineRound2)
        {
            aint.add(2);
        }

        if (this.autoMineRound3)
        {
            aint.add(3);
        }

        if (this.autoMineRound4)
        {
            aint.add(4);
        }

        return aint;
    }

}



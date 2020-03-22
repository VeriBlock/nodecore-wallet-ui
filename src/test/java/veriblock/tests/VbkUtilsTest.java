// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.tests;

import org.junit.Assert;
import org.junit.Test;
import veriblock.wallet.core.VbkUtils;

public class VbkUtilsTest {

    @Test
    public void convertDecimalCoinToAtomicLong_1() {
        long atomic= VbkUtils.convertDecimalCoinToAtomicLong("12.34567890");
        Assert.assertEquals(1234567890, atomic);
    }

    /*
    @Test
    public void getPercentLong_1() {
        //easy
        Assert.assertEquals(0.05,  VbkUtils.getPercentLong(5l, 100l), 0.0001);

        //edge cases
        Assert.assertEquals(0.0,  VbkUtils.getPercentLong(0l, 100l), 0.0001);

        //Big
        Assert.assertEquals(0.000140688,  VbkUtils.getPercentLong(615670l, 4376133270l), 0.0000001);

        //error case, but don't crash:
        Assert.assertEquals(0.0,  VbkUtils.getPercentLong(5l, 0l), 0.0001);
    }
    */
}
// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.tests;

import org.junit.Assert;
import org.junit.Test;
import veriblock.wallet.features.pop.PopService;


public class PopServiceTest {

    @Test
    public void getRoundNumber_1() {

        //obvious
        Assert.assertEquals(1, PopService.getRoundNumber(1));
        Assert.assertEquals(2, PopService.getRoundNumber(8));
        Assert.assertEquals(3, PopService.getRoundNumber(12));
        Assert.assertEquals(4, PopService.getRoundNumber(20));

        //further out
        Assert.assertEquals(1, PopService.getRoundNumber(80001));
        Assert.assertEquals(2, PopService.getRoundNumber(70008));
        Assert.assertEquals(3, PopService.getRoundNumber(600012));
        Assert.assertEquals(4, PopService.getRoundNumber(500020));

        Assert.assertEquals(1, PopService.getRoundNumber(80001 + 40));
        Assert.assertEquals(2, PopService.getRoundNumber(70008 + 40));
        Assert.assertEquals(3, PopService.getRoundNumber(600012 + 40));
        Assert.assertEquals(4, PopService.getRoundNumber(500020 + 40));
    }


}

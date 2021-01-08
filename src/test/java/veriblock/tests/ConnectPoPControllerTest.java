// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.
package veriblock.tests;

import org.junit.Assert;
import org.junit.Test;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.pop.ConnectPoPController;

import java.util.List;


public class ConnectPoPControllerTest {

    @Test
    public void createSeedMessage_1() {
        List<String> seeds = List.of("1549402633", "aaaa", "bbbb");
        LocaleModule lm = new LocaleModule();
        String sResult = ConnectPoPController.createSeedMessage(seeds, lm);
        Assert.assertTrue(sResult.length() > 0);
    }

    @Test
    public void createSeedMessage_2() {
        List<String> seeds = List.of("not_an_epoch", "aaaa", "bbbb");
        LocaleModule lm = new LocaleModule();
        String sResult = ConnectPoPController.createSeedMessage(seeds, lm);
        Assert.assertTrue(sResult.length() > 0);
    }

}

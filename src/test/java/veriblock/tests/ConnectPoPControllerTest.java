// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.tests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.pop.PopEntityParser;
import veriblock.wallet.core.pop.entities.MineResultEntity;
import veriblock.wallet.core.pop.entities.MinerPropertiesEntity;
import veriblock.wallet.features.pop.ConnectPoPController;


public class ConnectPoPControllerTest {

    @Test
    public void createSeedMessage_1() {

        String[] seeds = new String[3];
        seeds[0] = "1549402633";
        seeds[1] = "aaaa";
        seeds[2] = "bbbb";

        LocaleModule lm = new LocaleModule();
        String sResult = ConnectPoPController.createSeedMessage(seeds, lm);
        Assert.assertTrue(sResult.length() > 0);
    }

    @Test
    public void createSeedMessage_2() {

        String[] seeds = new String[3];
        seeds[0] = "not_an_epoch";
        seeds[1] = "aaaa";
        seeds[2] = "bbbb";

        LocaleModule lm = new LocaleModule();
        String sResult = ConnectPoPController.createSeedMessage(seeds, lm);
        Assert.assertTrue(sResult.length() > 0);
    }

}

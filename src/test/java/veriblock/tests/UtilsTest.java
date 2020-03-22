// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.tests;

import org.junit.Assert;
import org.junit.Test;
import veriblock.wallet.core.Utils;

public class UtilsTest {

    @Test
    public void getExcetionToString_1() {

        try {
           //Force an errow
            throw new Exception("Some test");
        }
        catch (Exception ex)
        {
            String s = Utils.getExcetionToString(ex);
            Assert.assertTrue(s.length() > 20);
        }
    }

    @Test
    public void getTimeOfDayNow_1() {

        String s = Utils.getTimeOfDayNow();
        Assert.assertTrue(s.length() > 5);
    }

    //Region isHigherNodeCoreVersion

    @Test
    public void isHigherNodeCoreVersion_1() {

        String actualVersion = "";
        String minVersion = "";

        actualVersion = "0.2.11";
        minVersion = "0.2.11";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "0.3.11";
        minVersion = "0.3.9";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "0.4.11";
        minVersion = "0.3.11-rc.2.dev.35.uncommitted+popminer.16672a4";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "0.4.1";
        minVersion = "0.3.12";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));
    }

    @Test
    public void isHigherNodeCoreVersion_malformed() {

        String actualVersion = "";
        String minVersion = "";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = null;
        minVersion = null;
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = null;
        minVersion = "0.3.11";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "0.3.11";
        minVersion = null;
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        //Maformed
        actualVersion = null;
        minVersion = "zzzzzzzzzzzzzzzzzz";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "zzzzzzzzzz";
        minVersion = null;
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "zzzzzzzzzz";
        minVersion = "aaaaaaaaaaaaa";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));
    }

    @Test
    public void isHigherNodeCoreVersion_2_true() {

        //Truncate off anything past 3 dots
        String actualVersion = "0.3.11";
        String minVersion = "0.3.11-rc1";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "0.2.11";
        minVersion = "0.2.1";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "0.22.1";
        minVersion = "0.9.1";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "1.0.0";
        minVersion = "0.99.99";
        Assert.assertTrue(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));
    }

    @Test
    public void isHigherNodeCoreVersion_false() {

        //Truncate off anything past 3 dots
        String actualVersion = "0.2.11-ppppppp";
        String minVersion = "0.3.11-xyz";
        Assert.assertFalse(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));

        actualVersion = "1.2.1";
        minVersion = "2.3.11-xyz";
        Assert.assertFalse(Utils.isHigherNodeCoreVersion(actualVersion, minVersion));
    }

        //endregion

}
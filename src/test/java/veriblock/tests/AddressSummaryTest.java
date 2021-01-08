// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.tests;

import org.junit.Assert;
import org.junit.Test;

import veriblock.wallet.entities.AddressBalanceEntity;
import veriblock.wallet.features.wallet.AddressSummary;
import java.util.ArrayList;
import java.util.List;

public class AddressSummaryTest {

    @Test
    public void parseMineResultEntity_1() {

        AddressSummary asummary = new AddressSummary();

        List<AddressBalanceEntity> addresses = new ArrayList<>();
        addresses.add(new AddressBalanceEntity("AAA", 4000));
        addresses.add(new AddressBalanceEntity("BBB", 1000));
        addresses.add(new AddressBalanceEntity("CCC", 3000));

        asummary.setAddresses(addresses);

        //Do sort
        asummary.sortByHighestConfirmed();

        Assert.assertEquals(4000, asummary.getAddresses().get(0).getAmountConfirmedAtomic());
        Assert.assertEquals(3000, asummary.getAddresses().get(1).getAmountConfirmedAtomic());
        Assert.assertEquals(1000, asummary.getAddresses().get(2).getAmountConfirmedAtomic());

    }

    @Test
    public void parseMineResultEntity_2() {

        AddressSummary asummary = new AddressSummary();

        List<AddressBalanceEntity> addresses = new ArrayList<>();
        addresses.add(new AddressBalanceEntity("AAA", 4000));
        addresses.add(new AddressBalanceEntity("BBB1", 1000));
        addresses.add(new AddressBalanceEntity("BBB2", 1000));
        addresses.add(new AddressBalanceEntity("CCC", 3000));

        asummary.setAddresses(addresses);

        //Do sort
        asummary.sortByHighestConfirmed();

        Assert.assertEquals(4000, asummary.getAddresses().get(0).getAmountConfirmedAtomic());
        Assert.assertEquals(3000, asummary.getAddresses().get(1).getAmountConfirmedAtomic());
        Assert.assertEquals(1000, asummary.getAddresses().get(2).getAmountConfirmedAtomic());
        Assert.assertEquals(1000, asummary.getAddresses().get(3).getAmountConfirmedAtomic());
    }
}

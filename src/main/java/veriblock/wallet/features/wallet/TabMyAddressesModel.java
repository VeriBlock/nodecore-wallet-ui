// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import java.util.*;

public class TabMyAddressesModel {

    public TabMyAddressesModel() {
        //_addresses = new ArrayList<>();

        //Default
        //TotalBalance = new SimpleStringProperty("zzzzz");

        _isCached = false;
        hideZeroBalanceAddresses = false;
    }

    public AddressSummary getAddressSummary() {
        if (_addressSummary == null)
        {
            _addressSummary = new AddressSummary();
        }
        return _addressSummary;
    }
    public void setAddressSummary(AddressSummary addressSummary) {
        this._addressSummary = addressSummary;
    }
    private AddressSummary _addressSummary;

    //public StringProperty TotalBalance;

    public boolean getIsCached() {
        return _isCached;
    }

    public void setIsCached(boolean isCached) {
        this._isCached = isCached;
    }

    private boolean _isCached;

    private HashMap<String, String> _nicknames;
    public  HashMap<String, String> getNicknamesOriginal() {
        return _nicknames;
    }
    public void setNicknamesOriginal( HashMap<String, String> value)
    {
        _nicknames = value;
    }

    public boolean hideZeroBalanceAddresses;

}

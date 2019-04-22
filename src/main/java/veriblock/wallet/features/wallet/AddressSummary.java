// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import veriblock.wallet.entities.AddressBalanceEntity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AddressSummary {

    public String toString()
    {
        int iCount = 0;
        if (_addresses != null)
        {
            iCount = _addresses.size();
        }
        return String.format("AddressCount: %1$s", iCount);
    }

    private List<AddressBalanceEntity> _addresses;

    public void setAddresses(List<AddressBalanceEntity> addresses)
    {
        _addresses = addresses;
    }
    public List<AddressBalanceEntity> getAddresses()
    {
        return _addresses;
    }

    public AddressBalanceEntity getAddress(String address)
    {
        if (_addresses == null || address == null)
        {
            return null;
        }

        for (AddressBalanceEntity entity : _addresses)
        {
            if (entity.getAddress() != null && entity.getAddress().equals(address))
            {
                //found it!
                return entity;
            }
        }
        return null;
    }


    public void sortByHighestConfirmed()
    {
        Collections.sort(_addresses, new CompareSortHighestConfirmed());
    }

    private class CompareSortHighestConfirmed implements Comparator<AddressBalanceEntity> {
        @Override
        public int compare(AddressBalanceEntity arg0, AddressBalanceEntity arg1) {
            //Desc
            if (arg1.getAmountConfirmedAtomic() > arg0.getAmountConfirmedAtomic())
                return 1;
            else if (arg1.getAmountConfirmedAtomic() < arg0.getAmountConfirmedAtomic())
                return -1;
            else
                return 0;
        }
    }

    public long getTotalSumAtomic()
    {
        if (_addresses == null)
        {
            return 0;
        }

        long sum = 0;
        for (AddressBalanceEntity address: _addresses)
        {
            sum = sum + address.getAmountConfirmedAtomic();
        }
        return sum;
    }

    public AddressBalanceEntity getDefaultAddress()
    {
        if (_addresses == null)
        {
            //Should not happen!
            return null;
        }

        for (AddressBalanceEntity row : _addresses)
        {
            if (row.getIsDefault())
            {
                return row;
            }
        }

        //Should not happen!
        return null;
    }
}

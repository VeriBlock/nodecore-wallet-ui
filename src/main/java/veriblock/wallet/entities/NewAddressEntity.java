// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

//GRPC: getNewAddress
public class NewAddressEntity {

    private String _newAddress;

    public String getNewAddress()
    {
        return _newAddress;
    }
    public  void setNewAddress(String value)
    {
        _newAddress = value;
    }
}

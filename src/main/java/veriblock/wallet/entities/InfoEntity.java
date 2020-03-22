// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.
// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

//GRPC getInfo
public class InfoEntity {

    public InfoEntity()
    {

    }

    private String _defaultAddress;
    private double _defaultAmount;
    private String _lastBlockHash;
    private int _lastBlockHeight;

    public long transactionFeePerByte;

    public String getDefaultAddress() {
        return _defaultAddress;
    }
    public void setDefaultAddress(String value)
    {
        _defaultAddress = value;
    }

    public String getLastBlockHash() {
        return _lastBlockHash;
    }
    public void setLastBlockHash(String value)
    {
        _lastBlockHash = value;
    }

    public int getLastBlockHeight() {
        return _lastBlockHeight;
    }
    public void setLastBlockHeight(int value)
    {
        _lastBlockHeight = value;
    }

    /*
    public double getTransactionFeePerByte() {
        return _transactionFeePerByte;
    }
    public void setTransactionFeePerByte(double value)
    {
        _transactionFeePerByte = value;
    }
*/

    //TODO, other fields...

    /*
    public double getDefaultAmount() {
        return _defaultAmount;
    }
    public void setDefaultAmount(double value)
    {
        _defaultAmount = value;
    }
    */
}

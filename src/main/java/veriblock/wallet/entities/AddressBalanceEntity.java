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

import veriblock.wallet.core.VbkUtils;

//GRPC: getBalance
public class AddressBalanceEntity
{
    public AddressBalanceEntity()
    {

    }

    public AddressBalanceEntity(String address, long amountConfirmed)
    {
        this._address = address;
        this._amountConfirmed = amountConfirmed;
    }

    public AddressBalanceEntity(String address) {
        this._address = address;
        this._isDefault = false;
        this._amountConfirmed = 0;
        this._amountPending = 0;
    }

    @Override
    public String toString()
    {
        String nickNamePrefix = "";
        if (_nickName != null && _nickName.length() > 0)
        {
            nickNamePrefix = String.format("[%1$s] ", _nickName);
        }
        return String.format("%4$s%1$s, Confirmed=%2$s, Default=%3$s", _address, _amountConfirmed, _isDefault,
                nickNamePrefix);
    }

    private String _nickName;
    private String _address;
    private boolean _isDefault;
    private long _amountConfirmed;
    private long _amountPending;

    public void setNickName(String value)
    {
        _nickName = value;
    }
    public String getNickName()
    {
        return _nickName;
    }
    
    public void setAddress(String value)
    {
        _address = value;
    }
    public String getAddress()
    {
        return _address;
    }

    public void setIsDefault(boolean value)
    {
        _isDefault = value;
    }
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public boolean getIsDefault()
    {
        return _isDefault;
    }

    public void setAmountConfirmedAtomic(long value)
    {
        _amountConfirmed = value;
    }
    public long getAmountConfirmedAtomic()
    {
        return _amountConfirmed;
    }

    public void setAmountPendingAtomic(long value)
    {
        _amountPending = value;
    }
    public long getAmountPendingAtomic()
    {
        return _amountPending;
    }

    private boolean _default;

    private boolean send;


}

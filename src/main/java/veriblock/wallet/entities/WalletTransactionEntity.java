// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

import org.joda.time.DateTime;
import veriblock.wallet.core.VbkUtils;

//GRPC: getWalletTransactions
public class WalletTransactionEntity
{
    private String _txId;
    private DateTime _timestamp;

    private int _confirmations;
    private int _blockHeight;
    private String _addressMine;
    private String _addressTo;
    private String _addressFrom;

    public long amount;
    private String _txType; //CB|TX sent | TX Received
    private String _status; //UNKNOWN|PENDING|CONFIRMED|DEAD

    public void setTxId(String value)
    {
        this._txId = value;
    }
    public String getTxId()
    {
        return this._txId;
    }

    public void setConfirmations(int value)
    {
        this._confirmations = value;
    }
    public int getConfirmations()
    {
        return this._confirmations;
    }

    public void setBlockHeight(int value)
    {
        this._blockHeight = value;
    }
    public int getBlockHeight()
    {
        return this._blockHeight;
    }

    public void setTimestamp(DateTime value)
    {
        this._timestamp = value;
    }
    public DateTime getTimestamp()
    {
        return this._timestamp;
    }

    public void setAddressMine(String value)
    {
        this._addressMine = value;
    }
    public String getAddressMine()
    {
        return this._addressMine;
    }

    public void setAddressFrom(String value)
    {
        this._addressFrom = value;
    }
    public String getAddressFrom()
    {
        return this._addressFrom;
    }

    public void setAddressTo(String value)
    {
        this._addressTo = value;
    }
    public String getAddressTo()
    {
        return this._addressTo;
    }

    /*
    public void setAmount(double value)
    {
        this._amount = value;
    }
    public double getAmount()
    {
        return this._amount;
    }
*/

    public String getAmountAsString()
    {
        return VbkUtils.convertAtomicToVbkString(this.amount);
    }

    public void setTxType(String value)
    {
        this._txType = value;
    }
    public String getTxType()
    {
        return this._txType;
    }

    public void setStatus(String value)
    {
        this._status = value;
    }
    public String getStatus()
    {
        return this._status;
    }

}

// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

import java.util.ArrayList;
import java.util.List;

public class WalletTransactionsEntity
{
    public WalletTransactionsEntity()
    {
        _walletTransactions = new ArrayList<>();
    }

    private List<WalletTransactionEntity> _walletTransactions;
    public void setWalletTransactions(List<WalletTransactionEntity> value)
    {
        _walletTransactions = value;
    }
    public  List<WalletTransactionEntity> getWalletTransactions()
    {
        return _walletTransactions;
    }

    private CacheState _cacheState;
    public void setCacheState(CacheState value)
    {
        _cacheState = value;
    }
    public void setCacheState(String value)
    {
        switch (value)
        {
            case "BUILDING":
                _cacheState = CacheState.BUILDING;
                break;
            case "CURRENT":
                _cacheState = CacheState.CURRENT;
                break;
            case "ERRORED":
                _cacheState = CacheState.ERRORED;
                break;
            default:
                _cacheState = CacheState.UNKNOWN;
                break;
        }
    }
    public CacheState getCacheState()
    {
        return _cacheState;
    }

    public enum CacheState
    {
        UNKNOWN,
        BUILDING,
        CURRENT,
        ERRORED,
    }

    private String _replyMessage;
    public void setReplyMessage(String value)
    {
        _replyMessage = value;
    }
    public  String getReplyMessage()
    {
        return _replyMessage;
    }
}

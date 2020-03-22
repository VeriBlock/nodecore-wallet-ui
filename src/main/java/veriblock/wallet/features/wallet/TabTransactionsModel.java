// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import veriblock.wallet.core.ValidationInfo;
import veriblock.wallet.entities.WalletTransactionEntity;
import veriblock.wallet.entities.WalletTransactionsEntity;

import java.util.ArrayList;
import java.util.List;

public class TabTransactionsModel {
    public TabTransactionsModel()
    {

    }

    //public String testData;

    private String _defaultAddress;
    private int _pageNumber;
    private int _resultsPerPage;

    public int currentBlock;

    //private int _walletCacheSyncHeight;
    //private int _blockHeight;

    private ValidationInfo _vi;
    public void setValidationInfo(ValidationInfo value)
    {
        _vi = value;
    }
    public ValidationInfo getValidationInfo()
    {
        return _vi;
    }

    private WalletTransactionsEntity.CacheState _cacheState;
    public void setCacheState(WalletTransactionsEntity.CacheState value)
    {
        _cacheState = value;
    }
    public WalletTransactionsEntity.CacheState getCacheState()
    {
        return _cacheState;
    }
    public boolean isCacheStateRebuilding()
    {
        return (_cacheState != null && _cacheState == WalletTransactionsEntity.CacheState.BUILDING);
    }

    public void setSelectedAddress(String value)
    {
        _defaultAddress = value;
    }
    public String getSelectedAddress()
    {
        return _defaultAddress;
    }

    public void setPageNumber(int value)
    {
        _pageNumber = value;
    }
    public int getPageNumber()
    {
        return _pageNumber;
    }

    public void setResultsPerPage(int value)
    {
        _resultsPerPage = value;
    }
    public int getResultsPerPage()
    {
        return _resultsPerPage;
    }

    private List<WalletTransactionEntity> _walletTransactions;
    public void setWalletTransactions(List<WalletTransactionEntity> value)
    {
        _walletTransactions = value;
    }
    public  List<WalletTransactionEntity> getWalletTransactions()
    {
        //ensure non=null
        if (_walletTransactions == null)
        {
            _walletTransactions = new ArrayList<>();
        }
        return _walletTransactions;
    }

    private List<WalletTransactionEntity> _walletTransactionsPending;
    public void setWalletTransactionsPending(List<WalletTransactionEntity> value)
    {
        _walletTransactionsPending = value;
    }
    public  List<WalletTransactionEntity> getWalletTransactionsPending()
    {
        return _walletTransactionsPending;
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

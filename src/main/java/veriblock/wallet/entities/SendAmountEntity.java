// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

import java.util.ArrayList;
import java.util.List;

//GRPC send
public class SendAmountEntity {

    public SendAmountEntity()
    {
        _createdTxList = new ArrayList<>();
    }

    public SendInput input;
    private List<String> _createdTxList;

    public void setOutputTxIdList(List<String> value)
    {
        this._createdTxList = value;
    }
    public List<String> getOutputTxIdList()
    {
        return this._createdTxList;
    }

    //Returns 1 row per line
    public String getOutputTxList(String delimiter)
    {
        String[] astr = new String[this.getOutputTxIdList().size()];
        for (int i = 0; i < astr.length; i++)
        {
            astr[i] = getOutputTxIdList().get(i);
        }
        String csv = String.join(delimiter, astr);
        return csv;
    }
}

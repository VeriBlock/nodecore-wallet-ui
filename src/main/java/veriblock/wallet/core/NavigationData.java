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

package veriblock.wallet.core;

public class NavigationData {

    public NavigationData()
    {

    }

    @Override
    public String toString() {
        if (_data == null)
        {
            return null;
        }
        else
        {
            return _data.toString();
        }
    }

    public NavigationData(Object data) {
        _data = data;
    }

    private Object _data;

    public Object getData() {
        return _data;
    }
    public void setData(Object value)
    {
        _data = value;
    }

}

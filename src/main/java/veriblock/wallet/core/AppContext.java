// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

public class AppContext {

    public UIManager UIManager;

    public DefaultConfiguration Configuration;

    private int currentBlockHeight;
    public int getCurrentBlockHeight()
    {
        return currentBlockHeight;
    }

    //Called by main controller on block-update loop. Good if a page wants to get this on start
    //Call BaseController.onCurrentBlockChanged to get when the height changes
    public void setCurrentBlockHeight(int height)
    {
        currentBlockHeight = height;
    }
}

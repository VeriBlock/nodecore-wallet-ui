// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseController
{
    private static final Logger _logger = LoggerFactory.getLogger(BaseController.class);

    public void initData(AppContext appContext, NavigationData navigationData)
    {
        _appContext = appContext;
        _navigationData = navigationData;

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> globalPageErrorHandler(t, e)));
        Thread.currentThread().setUncaughtExceptionHandler(this::globalPageErrorHandler);

        init();
    }

    protected void globalPageErrorHandler(Thread t, Throwable e) //(Exception ex)
    {
        //System.out.println("Run global handler");
        _appContext.UIManager.setStatusMessageError("An unknown error occured");

        _logger.error(getClass().getName() + ": " + Utils.getExcetionToString((Exception) e));

    }

    protected AppContext _appContext;
    protected NavigationData _navigationData;

    //region Abstract and Virtual Methods

    public abstract void init();
    public abstract void setLocale();

    //optional cleanup methods
    public void dispose()
    {

    }

    //Called only when current block updated
    //Expect to catch each new block increase
    public void onCurrentBlockChanged(int oldBlockHeight, int newBlockHeight)
    {

    }

    //Called only when balance updated
    public void onBalanceChanged(boolean didChange, long oldAmount, long newAmount)
    {

    }

    //endregion

}

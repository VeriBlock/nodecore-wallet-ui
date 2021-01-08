// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.tools;

import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import javafx.scene.control.*;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;

public class TabConsoleController extends BaseController  {

    public void initialize() {

    }

    public void init() {
        _appContext.UIManager.setTitle("Diagnostics");

        setLocale();
        load();

    }

    private LocaleModule _localeModule;
    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabSettings);

    }

    public Label lblMessage;

    private void load() {

        //Can we find any running instance of the app?

        //If yes, can we bring that to the front? --> already running

        //If no, search directory

    }

    public void clickStartConsole()
    {

    }
}

// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.shell;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.ImageView;
import veriblock.wallet.core.*;
import javafx.scene.control.Label;
import veriblock.wallet.core.locale.LocaleItem;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;

public class WelcomeController extends BaseController {

    public void initialize() {

    }

    public ImageView imgName;
    public void init()
    {
        loadData();
        ControlHelper.setImage(imgName, "VeriBlock_Logo_Rev.png");
    }

    private LocaleModule _localeModule;

    private void loadData()
    {
        setLocale();

        ControlBuilder.setupLanguageDropdown(this.ddLanguageList,
                (newItem) -> {
                    updateLanguageDropdown(newItem);
                }
        );
    }

    private void updateLanguageDropdown(LocaleItem newItem) {
        //reget local
        setLocale();
        _appContext.UIManager.updateLocaleMain();
    }

    public void setLocale() {

        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.Welcome);

        lblHeader.setText(_localeModule.getString("main.title"));
        String mainText = _localeModule.getString("text.main");
        btnGetStarted.setText(_localeModule.getString("button.getstarted"));
        lblSelectLanguage.setText(_localeModule.getString("label.selectLanguage"));

        String s = String.format(mainText,
                _appContext.Configuration.getNodecoreAddress(),
                _appContext.Configuration.getNodecorePort(),
                _appContext.Configuration.getNetworkName());

        this.lblCustomMessage.setText(s);
    }

    public ChoiceBox<LocaleItem> ddLanguageList;
    public Label lblSelectLanguage;
    public Label lblHeader;
    public Label lblCustomMessage;
    public Button btnGetStarted;

    public void clickGetStarted()
    {
        SoundItem.playButtonClick();

        //close window
        _appContext.UIManager.closeModalPoP();

    }

}

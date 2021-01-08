// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.shell;

import javafx.scene.control.Label;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.uicommon.ControlHelper;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;

public class TabHelpController extends BaseController  {

    public void initialize() {

    }

    public Hyperlink lnkWebOrg;
    public Hyperlink lnkExplorer;
    public Hyperlink lnkWiki;
    public Hyperlink lnkTelegram;

    public Label lblWebOrg;
    public Label lblExplorer;
    public Label lblWiki;
    public Label lblTelegram;

    private LocaleModule _localeModule;

    public void init()
    {
        //--
        Tooltip.install(this.lnkWebOrg,  new Tooltip(_urlWebOrg));
        Tooltip.install(this.lnkExplorer,  new Tooltip(_urlExplorer));
        Tooltip.install(this.lnkWiki,  new Tooltip(_urlWiki));
        Tooltip.install(this.lnkTelegram,  new Tooltip(_urlDiscord));

        ControlHelper.setImage(imgHome, "help/home.png");
        ControlHelper.setImage(imgDashboard, "help/dashboard.png");
        ControlHelper.setImage(imgWiki, "help/wiki.png");
        ControlHelper.setImage(imgTelegram, "help/telegram.png");

        setLocale();

    }

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabHelp);

        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));

        lnkWebOrg.setText(_localeModule.getString("lnkWebOrg.displayText"));
        lnkExplorer.setText(_localeModule.getString("lnkExplorer.displayText"));
        lnkWiki.setText(_localeModule.getString("lnkWiki.displayText"));
        lnkTelegram.setText(_localeModule.getString("lnkDiscord.displayText"));

        lblWebOrg.setText(_localeModule.getString("label.WebOrg"));
        lblExplorer.setText(_localeModule.getString("label.Explorer"));
        lblWiki.setText(_localeModule.getString("label.Wiki"));
        lblTelegram.setText(_localeModule.getString("label.Discord"));

    }

    public javafx.scene.image.ImageView imgHome;
    public javafx.scene.image.ImageView imgDashboard;
    public javafx.scene.image.ImageView imgWiki;
    public javafx.scene.image.ImageView imgTelegram;

    private String _urlWebOrg = "https://veriblock.org";
    private String _urlExplorer = "http://testnet.explore.veriblock.org";
    private String _urlWiki = "http://wiki.veriblock.org";
    //private String _urlTelegram = "https://web.telegram.org/#/im?p=@veriblock";
    private String _urlDiscord = "https://discord.gg/wJZEjry";

    public void clickOrg()
    {
        Utils.openLink(_urlWebOrg);
    }

    public void clickExplorer()
    {
        //TODO --> choose mainnet vs. testnet
        Utils.openLink(_urlExplorer);
    }

    public void clickWiki()
    {
        Utils.openLink(_urlWiki);
    }

    public void clickTelegram()
    {
        Utils.openLink(_urlDiscord);
    }
}

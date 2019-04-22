// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.pop;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.pop.*;
import veriblock.wallet.core.pop.entities.MinerPropertiesEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.FormatHelper;
import veriblock.wallet.uicommon.IntegrationLinks;


public class AddBtcController extends DialogController {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectPoPController.class);
    public void initialize() {
    }

    private LocaleModule _localeModule;

    public void init() {

        //set current props
        _clientProxy = PopService.getApiProxy();

        setLocale();

        setupUI();
        applyModel();

    }

    private void setupUI()
    {
        ControlHelper.setLabelCopyProperties(lblBtcAddress, true);
        ControlHelper.setLabelCopyProperties(lblBtcBalance, true);

    }

    private void applyModel()
    {
        //set minimum
        this.lblMinBtc.setText(VbkUtils.convertAtomicToVbkString( PopService.getMinimumBtcBalance()));

        //Set fields
        MinerPropertiesEntity props =  _clientProxy.getMinerPropertiesEntity();
        _btcAddress = props.bitcoinAddress;
        this.lblBtcAddress.setText(_btcAddress);
        this.lblBtcBalance.setText(VbkUtils.convertAtomicToVbkString( props.bitcoinBalance));
    }

    private ApiProxy _clientProxy;

    private String _btcAddress;
    public Label lblMinBtc;
    public TextField lblBtcAddress;
    public TextField lblBtcBalance;

    public Label lblMsg1;
    public Label lblMsg2;
    public Label lblMsg3;
    public Label lblMinRequiredLabel;
    public Label lblSendToAddress;
    public Label lblBtcBalanceLabel;
    public Hyperlink hlnkViewAddress;
    public Button btnClose;

    public Button btnCopyBtcAddress;

    public void setLocale() {

        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabPoP);

        lblMsg1.setText(_localeModule.getString("AddBtc_label_message1"));
        lblMsg2.setText(_localeModule.getString("AddBtc_label_message2"));
        lblMsg3.setText(_localeModule.getString("AddBtc_label_message3"));

        lblMinRequiredLabel.setText(_localeModule.getString("AddBtc_label_minrequired"));
        lblSendToAddress.setText(_localeModule.getString("AddBtc_label_sendToAddress"));
        lblBtcBalanceLabel.setText(_localeModule.getString("AddBtc_label_myPopBalance"));
        hlnkViewAddress.setText(_localeModule.getString("AddBtc_link_viewBtcAddress"));
        btnClose.setText(_localeModule.getString("AddBtc_button_close"));
        btnCopyBtcAddress.setText(_localeModule.getString("AddBtc_button_copyBtc"));
    }

    public void clickViewBtcInExplorer() {
        String strAddress = _btcAddress;

        IntegrationLinks links = IntegrationLinks.getInstance();
        String url = links.getBtcAddressUrl(strAddress);
        Utils.openLink(url);

        _appContext.UIManager.setStatusMessage(
                String.format("Opened BTC block explorer for your PoP BTC wallet address: %1$s", strAddress));
    }

    public void clickClose()
    {
        SoundItem.playButtonClick();
        this.closeDialog(null);
    }

    public Label lblStatus;

    public void clickCopy()
    {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(_btcAddress);
        clipboard.setContent(content);
        _logger.info("Btc Address copied to clipboard: {}", _btcAddress);

        this.lblStatus.setText(String.format(_localeModule.getString("AddBtc_button_copyBtc_Success"),
                _btcAddress, Utils.getTimeOfDayNow()));
    }
}

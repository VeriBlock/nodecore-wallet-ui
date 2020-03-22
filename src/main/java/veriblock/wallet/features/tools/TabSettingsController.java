// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.tools;

import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleItem;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.entities.StateInfoEntity;
import veriblock.wallet.entities.WalletLockState;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.features.shell.FooterController;
import veriblock.wallet.features.wallet.WalletService;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;

public class TabSettingsController extends BaseController  {

    private static final Logger _logger = LoggerFactory.getLogger(TabSettingsController.class);

    public void initialize() {

    }

    private LocaleModule _localeModule;

    public void init()
    {
        setupModel();
        setLocale();
        bindModelToView();

        setupControls();

        updateUI_Encryption();
    }

    private TabSettingsModel _tabSettingsModel;

    public Label lblChangeLanguage;
    public Label lblUserDirectory;
    public ChoiceBox<LocaleItem> ddLanguageList;
    public TitledPane paneLang;
    public Accordion accSettings;
    public Hyperlink hlnkUserDirectory;
    public Label lblEnableSound;
    public CheckBox chkEnableSound;
    public Button btnTestSound;

    private void setupModel()
    {
        _tabSettingsModel = new TabSettingsModel();

        String userDir = (new FileManager()).getRootDirectory();
        this.hlnkUserDirectory.setText(userDir);
    }

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabSettings);

        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));

        _tabSettingsModel.ChangeLanguageLabel.set(_localeModule.getString("label.changeLanguage"));
        _tabSettingsModel.LanguagePaneTitle.set(_localeModule.getString("panel.general"));

        lblUserDirectory.setText(_localeModule.getString("label.UserDirectory"));

        //Sound
        lblEnableSound.setText(_localeModule.getString("sound_label"));
        btnTestSound.setText(_localeModule.getString("sound_button_text"));

        //Encryption
        lblWalletEncryptionHeader.setText(_localeModule.getString("encrypt_label"));
    }

    private void bindModelToView()
    {
        lblChangeLanguage.textProperty().bindBidirectional(_tabSettingsModel.ChangeLanguageLabel);
        paneLang.textProperty().bindBidirectional(_tabSettingsModel.LanguagePaneTitle);
    }

    private void setupControls() {

        ControlBuilder.setupLanguageDropdown(this.ddLanguageList,
                (newItem) -> {
                    updateLanguageDropdown(newItem);
                }
        );

        this.accSettings.setExpandedPane(paneLang);

        SoundManager sm = SoundManager.getInstance();
        boolean soundEnabled = sm.isSoundEnabled();
        this.chkEnableSound.setSelected(soundEnabled);
    }


    private void updateLanguageDropdown(LocaleItem newItem)
    {
        //reget local
        setLocale();

        _appContext.UIManager.updateLocaleMain();

        //TODO --> convert binding to model:
        _appContext.UIManager.setStatusMessageSucess(
                String.format(_localeModule.getString("changeLanguage.success"), newItem.DisplayText));
    }


    private String getApplicationDirectory()
    {
        return hlnkUserDirectory.getText();
    }

    public void clickUserDirectory()
    {
        String s = getApplicationDirectory();
        if (s.length() > 0) {
            Utils.openFolder(s);
        }
    }

    //region Sound Effects

    public void clickEnableSound()
    {
        //check state of textbox

        //update settings
        //display prompt
        boolean didEnabledSound = this.chkEnableSound.isSelected();
        SoundManager sm = SoundManager.getInstance();
        sm.enableSound(didEnabledSound);

        if (didEnabledSound)
        {
            _appContext.UIManager.setStatusMessageSucess(
                    _localeModule.getString("sound_enabled_true")
            );
        }
        else
        {
            _appContext.UIManager.setStatusMessageSucess(
                    _localeModule.getString("sound_enabled_false")
            );
        }
    }

    public void clickTestSound()
    {
        SoundManager sm = SoundManager.getInstance();

        sm.play(SoundItem.get(SoundItem.SoundEnum.ButtonClick));
        if (sm.isSoundEnabled())
        {
            _appContext.UIManager.setStatusMessageSucess(
                    _localeModule.getString("sound_test_on")
            );
        }
        else
        {
            _appContext.UIManager.setStatusMessageSucess(
                    _localeModule.getString("sound_test_off")
            );
        }

    }

    //endregion

    //region Wallet Encryption

    public Label lblWalletEncryptionHeader;
    public Button btnEncryptWallet;
    public Button btnEncryptClearPassword;
    public Button btnEncryptRefresh;

    public void clickEncryptRefresh()
    {
        updateUI_Encryption();
        _appContext.UIManager.setStatusMessageSucess(
                String.format(_localeModule.getString("encrypt_refreshTime"), Utils.getTimeOfDayNow()));
    }

    private WalletLockState _lockState;

    private void updateUI_Encryption() {
        CommandResult<StateInfoEntity> commandResult = NodeCoreGateway.getInstance().getGetStateInfo();
        WalletLockState lockState = WalletLockState.UNKNOWN;
        if (commandResult.isSuccess()) {
            //yay!
            lockState = commandResult.getPayload().walletLockState;
        }

        updateUI_Encryption(lockState);
    }

    private void updateUI_Encryption(WalletLockState lockState) {
        _lockState = lockState;
        boolean isEncrypted = WalletService.isWalletEncrypted(lockState);
        boolean isWalletPasswordStored = NodeCoreGateway.getInstance().getIsWalletPasswordStored();

        btnEncryptRefresh.setText(_localeModule.getString("encrypt_button_refresh"));
        btnEncryptClearPassword.setText(_localeModule.getString("encrypt_button_clearPassword"));

        if (isEncrypted) {
            //Current state = Encrypted
            //swap state -> decrypt
            btnEncryptWallet.setText(_localeModule.getString("encrypt_button_currentEncrypt"));

            //Check ClearPassword button
            if (isWalletPasswordStored)
            {
                //have a password, could then clear it
                btnEncryptClearPassword.setDisable(false);
            }
            else
            {
                //no password, nothing to clear
                btnEncryptClearPassword.setDisable(true);
            }

        } else {
            //swap state -> encrypt
            btnEncryptWallet.setText(_localeModule.getString("encrypt_button_currentDecrypt"));

            //Current state = Decrypted, so easy case
            btnEncryptClearPassword.setDisable(true);    //password already cleared if decrypted
        }
    }

    public void clickEncryptWallet()
    {
        _logger.info("clickEncryptWallet: {}", _lockState);

        boolean isEncrypted = WalletService.isWalletEncrypted(_lockState);
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();


        ValidationInfo vi = new ValidationInfo();
        CommandResult<Void> result = null;
        if (isEncrypted)
        {
            //Is encrypted --> should decrypt
            //Easy, simply request password to unlock
            String password = WalletService.promptEncryptWallet(_appContext, false);
            if (password == null || password.length() == 0)
            {
                vi.setMessageWarning(_localeModule.getString("encrypt_validation_noPasswordDecrypt"));
            }
            else {
                result = ngw.decryptWallet(password);
                vi.setMessageSuccess(_localeModule.getString("encrypt_validation_decryptSuccess"));
            }
        }
        else {
            //Is decrypted --> should encrypt

            //Need custom form
            String password = WalletService.promptEncryptWallet(_appContext, true);
            if (password == null || password.length() == 0) {
                vi.setMessageWarning(_localeModule.getString("encrypt_validation_noPasswordEncrypt"));
            } else {
                result = ngw.encryptWallet(password);
                vi.setMessageSuccess(_localeModule.getString("encrypt_validation_encryptSuccess"));
            }
        }

        if (result == null)
        {
            _appContext.UIManager.setStatusMessage(vi);
            return;
        }

        if (result.isSuccess())
        {
            //yes!
            _appContext.UIManager.setStatusMessage(vi);
        }
        else
        {
            //bad
            _appContext.UIManager.setStatusMessageCommandError(result);
        }

        updateUI_Encryption();

    }

    public void clickEncryptClearPassword()
    {
        _logger.info("clickEncryptClearPassword: {}", _lockState);

        String message = _localeModule.getString("encrypt_clearPasswordConfirm");
        boolean shouldContinue = ControlHelper.showAlertYesNoDialog(message);
        if (shouldContinue)
        {
            NodeCoreGateway ngw = NodeCoreGateway.getInstance();
            ngw.clearWalletPassword();
            ngw.lockwallet();

            _appContext.UIManager.setStatusMessageSucess(_localeModule.getString("encrypt_clear_success"));
        }
        else
        {
            _appContext.UIManager.setStatusMessage(_localeModule.getString("encrypt_clear_cancelled"));
        }

        updateUI_Encryption();
    }

    //endregion

}

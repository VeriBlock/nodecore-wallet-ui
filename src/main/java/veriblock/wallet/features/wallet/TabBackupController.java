// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import javafx.scene.control.*;
import org.veriblock.core.utilities.Utility;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.entities.AddressBalanceEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.uicommon.ControlHelper;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import veriblock.wallet.uicommon.FormatHelper;

import java.io.File;

public class TabBackupController extends BaseController {
    public void initialize() {
    }

    public void init() {
        setLocale();
        setupUI();
    }

    private LocaleModule _localeModule;
    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabBackup);

        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));

        //Backup Wallet
        paneBackupWallet.setText(_localeModule.getString("backup.title"));
        backupText1.setText(_localeModule.getString("backup.explainText1"));
        backupText2.setText(_localeModule.getString("backup.explainText2"));
        backupBtnChooseFolder.setText(_localeModule.getString("backup.button.chooseFolder"));
        txtWalletBackupFolder.setPromptText(_localeModule.getString("backup.text.folder.nothingSelected"));
        backupBtnDoBackup.setText(_localeModule.getString("backup.button.doBackup2"));

        //Backup Single
        labelOR.setText(_localeModule.getString("labelOR"));
        backupBtnSingleAddress.setText(_localeModule.getString("backup_single_button"));
        backupSingleLabel.setText(_localeModule.getString("backup_single_label"));

        //Import Wallet
        paneImportWallet.setText(_localeModule.getString("import.title"));
        importText1.setText(_localeModule.getString("import.importText1"));
        importText2.setText(_localeModule.getString("import.importText2"));
        importBtnChooseFile.setText(_localeModule.getString("import.button.chooseFile"));
        txtWalletImportFile.setPromptText(_localeModule.getString("import.text.file.nothingSelected"));
        importBtnDoImport.setText(_localeModule.getString("import.button.doImport"));

        //Import Single
        labelOR2.setText(_localeModule.getString("labelOR"));
        ControlHelper.setPromptText(importTxtKey,_localeModule.getString("import_single_keyprompt"));
        importBtnSingleAddress.setText(_localeModule.getString("import_single_button"));
        importSingleLabel.setText(_localeModule.getString("import_single_label"));
    }

    //region Model - setup and update

    //endregion

    //region UI - Setup
    public Accordion accordionMain;

    public TitledPane paneBackupWallet;
    public Label backupText1;
    public Label backupText2;
    public Button backupBtnChooseFolder;
    public TextField txtWalletBackupFolder;
    public Button backupBtnDoBackup;

    public Label labelOR;
    public Button backupBtnSingleAddress;
    public Label backupSingleLabel;

    public TitledPane paneImportWallet;
    public Label importText1;
    public Label importText2;
    public Button importBtnChooseFile;
    public TextField txtWalletImportFile;
    public Button importBtnDoImport;

    public Label labelOR2;
    public Label importSingleLabel;
    public Button importBtnSingleAddress;
    public TextArea importTxtKey;

    private void setupUI()
    {
        this.accordionMain.setExpandedPane(paneBackupWallet);

        setupBackupAddressDropdown();

        int intMaxLength = 4000;    //better not have a private key bigger
        ControlHelper.setTextFieldProperties(this.importTxtKey, ControlHelper.MaskType.Hex, intMaxLength);
    }

    //endregion

    //region UI - Update

    public void clickChooseDirectory()
    {
        SoundItem.playButtonClick();

        Stage primaryStage = _appContext.UIManager.getPrimaryStage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if(selectedDirectory == null){
            //No Directory selected
        }else{
            this.txtWalletBackupFolder.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private String getBackupFolder()
    {
        return  this.txtWalletBackupFolder.getText();
    }


    private String getImportFile()
    {
        return  this.txtWalletImportFile.getText();
    }

    public void clickDoBackup()
    {
        SoundItem.playButtonClick();

        //Ensure textfield exists
        String strBackupFolder = getBackupFolder();
        if (strBackupFolder.length() == 0)
        {
            _appContext.UIManager.setStatusMessageError(_localeModule.getString("backup.validation.folderRequired"));
            return;
        }

        if (!Utils.doesFolderExist(strBackupFolder))
        {
            boolean blnShouldCreate = ControlHelper.showAlertYesNoDialog(_localeModule.getString("backup.validation.folderNotExists"));
            if (blnShouldCreate)
            {
                //Create folder!
                Utils.createFolder(strBackupFolder);
            }
            else
            {
                _appContext.UIManager.setStatusMessage(_localeModule.getString("backup.validation.noAction"));
                return;
            }
        }

        //Call GRPC
        CommandResult<Void> commandResult = NodeCoreGateway.getInstance().backupWallet(strBackupFolder);

        if (commandResult.isSuccess()) {
            //greate
            //Update UI
            _appContext.UIManager.setStatusMessage(new ValidationInfo(ValidationInfo.Status.Success,
                    String.format(_localeModule.getString("backup.validation.success"),strBackupFolder)));
        }
        else {
            _appContext.UIManager.setStatusMessageCommandError(commandResult);
        }

    }

    public void clickChooseImportFile()
    {
        SoundItem.playButtonClick();

        Stage primaryStage = _appContext.UIManager.getPrimaryStage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if(selectedFile == null){
            //No Directory selected
        }else{
            this.txtWalletImportFile.setText(selectedFile.getAbsolutePath());
        }
    }

    public void clickDoImport() {
        SoundItem.playButtonClick();

        try {
            //Ensure textfield exists
            String importFile = getImportFile();
            if (importFile.length() == 0) {
                _appContext.UIManager.setStatusMessageError(_localeModule.getString("import.validation.fileRequired"));
                return;
            }

            if (!Utils.doesFileExist(importFile)) {
                _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("import.validation.fileNotFound"));
                return;
            }

            //validate the file looks correct
            //1 - not named "walletconfig_backup.dat", "default_address VE1Vy1y43MiqcXXEjAKc8wW1A98yvr"
            //2 - not plain text
            String[] astrLines = Utils.readLines(importFile);
            if (astrLines.length > 0 && astrLines[0].contains("default_address")) {
                _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("import.validation.fileWrongType"));
                return;
            }

            //---------
            //Call GRPC

            AddressSummary addressBefore = WalletService.getAddressSummary().getPayload();

            NodeCoreGateway ngw = NodeCoreGateway.getInstance();

            //prompt for unlock
            boolean successfullyUnlocked = WalletService.promptUnlockWallet(_appContext, ngw);
            if (!successfullyUnlocked)
            {
                LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
                _appContext.UIManager.setStatusMessageWarning(lm.getString("walletService_passwordCancelled"));
                return;
            }

            CommandResult<Void> commandResult = ngw.importwallet(importFile);

            AddressSummary addressAfter = WalletService.getAddressSummary().getPayload();

            long diffAmount = addressAfter.getTotalSumAtomic() - addressBefore.getTotalSumAtomic();
            int diffCount = addressAfter.getAddresses().size() - addressAfter.getAddresses().size();
            String strDiff = String.format(_localeModule.getString("import.validation.successNetChange"),
                    diffCount,
                    VbkUtils.convertAtomicToVbkString(diffAmount),
                    _appContext.Configuration.getVbkUnit()
            );

            if (commandResult.isSuccess()) {
                //great
                //Update UI
                _appContext.UIManager.setStatusMessage(new ValidationInfo(ValidationInfo.Status.Success,
                        String.format(_localeModule.getString("import.validation.successFinalMessage"),
                                VbkUtils.convertAtomicToVbkString(addressAfter.getTotalSumAtomic()),
                                _appContext.Configuration.getVbkUnit(), strDiff)
                ));

                //UPDATES:
                //Trigger footer to refresh without waiting ~30 seconds for next block
                _appContext.UIManager.updateFooterBalance(addressAfter.getTotalSumAtomic());

                //add new addresses to dropdown
                setupBackupAddressDropdown();

            } else {
                _appContext.UIManager.setStatusMessageCommandError(commandResult);
            }
        } catch (Exception ex) {
            _appContext.UIManager.setStatusMessageError(_localeModule.getString("error.generalMessage"));
        }

    }
    //endregion

    //region Backup single address

    public ChoiceBox<AddressBalanceEntity> ddAddressBackup;

    private void setupBackupAddressDropdown() {

        boolean setupSuccess = WalletService.setupAddressDropdown(ddAddressBackup, _appContext, true);
    }

    private String getBackupAddress()
    {
        AddressBalanceEntity row = this.ddAddressBackup.getValue();
        if (row == null)
        {
            return  null;
        }
        return this.ddAddressBackup.getValue().getAddress();
    }

    public void clickDoBackupSingle() {

        //get address
        String strAddress = getBackupAddress();
        if (strAddress == null || strAddress.length() == 0) {
            //should not be possible via UI
            _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("backup_single_selectAddress"));
            return;
        }


        //prompt for unlock
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();
        boolean successfullyUnlocked = WalletService.promptUnlockWallet(_appContext, ngw);
        if (!successfullyUnlocked)
        {
            LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
            _appContext.UIManager.setStatusMessageWarning(lm.getString("walletService_passwordCancelled"));
            return;
        }


        //call NCGW
        CommandResult<String> commandResult = ngw.dumpPrivateKey(strAddress);
        if (!commandResult.isSuccess())
        {
            //bad
            _appContext.UIManager.setStatusMessage(commandResult.getValidationInfo());
            return;
        }

        String privateKeyHex = commandResult.getPayload();

        _appContext.UIManager.setStatusMessageSucess(
                String.format(_localeModule.getString("backup_single_successPrivateKeyShown"), strAddress)
        );

        //SHOW prompt
        ValidationInfo vi = new ValidationInfo(ValidationInfo.Status.Success,
                String.format(_localeModule.getString("backup_single_success1")));

        String strSuccessSingleBackup = String.format(_localeModule.getString("backup_single_success2"),
                strAddress);
        String strCopyableText = String.format(strSuccessSingleBackup + "\n\n"
                + privateKeyHex);

        ControlHelper.showAlertYesNoDialog(vi, strCopyableText);

    }

    //endregion

    //region Import single address

    public ValidationInfo validatePrivateKey(String privateKey)
    {
        //NOTE: exact details in NodeCore.AdminServiceImple.importPrivateKey()
        //will punt details to NC, but at least check basic sanity in client so we can ensure a helpful message

        ValidationInfo vi = new ValidationInfo();

        String strErrorRoot = _localeModule.getString("import_single_validateKey0");

        if (privateKey == null)
            privateKey = "";

        //1 check for HEX
        if (!Utility.isHex(privateKey))
        {
            //bad
            vi.setMessageError(strErrorRoot + " " + _localeModule.getString("import_single_validateKey1"));
            return vi;
        }

        //1b check for even number of digits, as a hex string
        if (privateKey.length() %2 != 0)
        {
            //bad
            vi.setMessageError(strErrorRoot + " " + _localeModule.getString("import_single_validateKey2"));
            return vi;
        }

        //2 check length at least > 100, i.e. don't confuse with address
        int sanityLength = 40;
        if (privateKey.length() < sanityLength)
        {
            vi.setMessageError(strErrorRoot + " " + _localeModule.getString("import_single_validateKey3"));
            return vi;
        }

        return vi;
    }

    public void clickDoImportSingle()
    {
        //get key
        String strPrivateKey = this.importTxtKey.getText();
        if (strPrivateKey == null || strPrivateKey.length() == 0)
        {
            _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("import_single_keyRequired"));
            return;
        }

        //do basic sanity input. let NodeCore do the validation on the privatekey
        ValidationInfo viCheck = validatePrivateKey(strPrivateKey);
        if (viCheck.isWarningOrError())
        {
            _appContext.UIManager.setStatusMessage(viCheck);
            return;
        }

        //show prompt
        String messareAreYouSure = _localeModule.getString("import_single_areYouSure");
        boolean shouldContinue = ControlHelper.showAlertYesNoDialog(messareAreYouSure);
        if (!shouldContinue)
        {
            _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("import_single_cancelled"));
            return;
        }


        //prompt for unlock
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();
        boolean successfullyUnlocked = WalletService.promptUnlockWallet(_appContext, ngw);
        if (!successfullyUnlocked)
        {
            LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
            _appContext.UIManager.setStatusMessageWarning(lm.getString("walletService_passwordCancelled"));
            return;
        }

        //do import
        String strNewAddress = "";

        //get address snapshot before importing so we can see if this address already existed
        CommandResult<AddressSummary> addressSummaryOldResult = WalletService.getAddressSummary();
        AddressSummary addressSummaryOld = null;
        if (addressSummaryOldResult.isSuccess()) {
            addressSummaryOld = addressSummaryOldResult.getPayload();
        }

        //IMPORT HERE!
        CommandResult<String> commandResult = NodeCoreGateway.getInstance().importPrivateKey(strPrivateKey);

        if (commandResult.isSuccess()) {
            //great
            strNewAddress = commandResult.getPayload();

            CommandResult<AddressSummary> addressSummaryNewResult = WalletService.getAddressSummary();
            if (addressSummaryNewResult.isSuccess()) {
                AddressSummary addressSummaryNew = addressSummaryNewResult.getPayload();
                AddressBalanceEntity newAddressEntity = addressSummaryNew.getAddress(strNewAddress);

                //expect it to be, especially if previous commands worked
                boolean didAddressAlreadyExist = true;
                if (addressSummaryOld.getAddress(strNewAddress) == null && newAddressEntity != null) {
                    //did not exist in old, does exist now --> this is new!
                    didAddressAlreadyExist = false;
                }

                if (didAddressAlreadyExist) {
                    //Address already existed, this is not new. User re-imported their address
                    String resultMessage = String.format(_localeModule.getString("import_single_keyExists"), strNewAddress);
                    _appContext.UIManager.setStatusMessage(resultMessage);
                } else {
                    //NEW ADDRESS!!!, this changes stuff!
                    String newAddressBalance = "-";
                    if (newAddressEntity != null) {
                        newAddressBalance = VbkUtils.convertAtomicToVbkString(newAddressEntity.getAmountConfirmedAtomic());
                    }
                    String resultMessage = String.format(_localeModule.getString("import_single_successFull"),
                            strNewAddress,
                            newAddressBalance,
                            VbkUtils.convertAtomicToVbkString(addressSummaryNew.getTotalSumAtomic())
                    );

                    _appContext.UIManager.setStatusMessageSucess(resultMessage);

                    //UPDATE balance and address (do this before another dialog pops up...)
                    //Trigger footer to refresh without waiting ~30 seconds for next block
                    _appContext.UIManager.updateFooterBalance(addressSummaryNew.getTotalSumAtomic());

                    //add new addresses to dropdown
                    setupBackupAddressDropdown();

                    //PROMPT FOR BACKUP
                    //Do you want to re-backup your wallet with this new key?
                    String headerText = String.format(_localeModule.getString("backup_prompt1"), strNewAddress);
                    ValidationInfo viResult = WalletService.promptBackup(headerText);
                    _appContext.UIManager.setStatusMessage(viResult);


                }
            } else {
                //rare edge case. But if so, then only show the new address, no balance info
                String resultMessage = String.format(_localeModule.getString("import_single_successSmall"), strNewAddress);
                _appContext.UIManager.setStatusMessageSucess(resultMessage);
            }

        }
        else {
            _appContext.UIManager.setStatusMessageCommandError(commandResult,
                    _localeModule.getString("import_single_techError"));
            return;
        }

    }

    //endregion

}

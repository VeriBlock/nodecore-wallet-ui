// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.storage.OrmLiteAddressRepository;
import veriblock.wallet.entities.AddressBalanceEntity;
import veriblock.wallet.entities.InfoEntity;
import veriblock.wallet.entities.WalletLockState;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.FormatHelper;
import veriblock.wallet.uicommon.GetPasswordInput;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static veriblock.wallet.core.ValidationInfo.Status.Info;

public class WalletService {

    //Reusable methods could be called from many places

    //Returns positive value, else sentinal -1
    public static long getTotalBalance()
    {
        long amount = -1;   //default is not passing
        CommandResult<AddressSummary> finalResult  =  WalletService.getAddressSummary();
        if (finalResult.isSuccess())
        {
            amount = finalResult.getPayload().getTotalSumAtomic();
        }
        return amount;
    }

    public static CommandResult<AddressSummary> getAddressSummary()
    {
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();

        AddressSummary domainObject = new AddressSummary();
        CommandResult<AddressSummary> finalResult = new CommandResult<>(domainObject);

        //Part 1: Get AddressBalanceEntity
        CommandResult<List<AddressBalanceEntity>> addressResult = ngw.getAddressBalances();
        //TODO --> Copy commandResult: connection|resultInfo
        finalResult.setConnectionResult(addressResult.getConnectionResult());
        finalResult.setValidationInfo(addressResult.getValidationInfo());

        if (!addressResult.isSuccess()) {
            //Bad, no point to continuing
            return finalResult;
        }

        //Good, continue assembling...
        domainObject.setAddresses(addressResult.getPayload());

        //Part 2: Get Info (default address)
        CommandResult<InfoEntity> infoResult = ngw.getInfo();
        String defaultAddress = infoResult.getPayload().getDefaultAddress();

        List<AddressBalanceEntity> addressList = addressResult.getPayload();

        for (AddressBalanceEntity o : addressList) {
            if (o.getAddress().equals(defaultAddress)) {
                o.setIsDefault(true);
            } else {
                o.setIsDefault(false);
            }
        }

        return finalResult;

    }

    public static HashMap<String, String> setupModelMergeNickNames(List<AddressBalanceEntity> addresses)
    {
        //HashMap<String, String> nicknames = UserSettingsManager.getInstance().getSettings().MyAddress.AddressNickNames;
        OrmLiteAddressRepository db = new OrmLiteAddressRepository();
        HashMap<String, String> nicknames = db.getAllAsHashMap();

        if (nicknames == null || nicknames.size() == 0)
        {
            //nothing to do
            return nicknames;
        }
        else
        {
            //have some values merge them in!
            for (AddressBalanceEntity row : addresses)
            {
                if (nicknames.containsKey(row.getAddress()))
                {
                    String nickName = nicknames.get(row.getAddress());
                    row.setNickName((nickName));
                }
            }
        }

        return nicknames;
    }

    //Returns true if successfully set up, else false
    public static boolean setupAddressDropdown(ChoiceBox<AddressBalanceEntity> ddAddress,
        AppContext appContext, boolean isAddressRequired) {
        CommandResult<AddressSummary> commandResult = WalletService.getAddressSummary();
        if (!commandResult.isSuccess()) {
            //bad, should not happen by the time this can be called
            return false;
        }

        AddressSummary domainModel = commandResult.getPayload();
        domainModel.sortByHighestConfirmed();   //default sort
        List<AddressBalanceEntity> addresses = domainModel.getAddresses();
        WalletService.setupModelMergeNickNames(addresses);

        ddAddress.getItems().clear();
        ddAddress.getItems().addAll(addresses);

        if (isAddressRequired) {
            //don't leave empty
        } else {
            //not required
            //Initial value is to skip any address
            ddAddress.getItems().add(0, null);
        }

        ddAddress.getSelectionModel().selectFirst();

        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.TabSend);

        //Set - Assume default address
        FormatHelper.formatAddressDropdown(ddAddress,
                appContext.Configuration.getVbkUnit(),
                lm.getString("dropdown.fromAddress.leySystemChoose"));

        return true;
    }

    //Returns validation info for success/fail
    //headerText = some explanation how the wallet has changed
    public static ValidationInfo promptBackup(String headerText)
    {
        //headerText = "You've changed the wallet by ___."

        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);

        String message = headerText + "\n" + lm.getString("walletService_backup_prompt");

        ValidationInfo viResult = new ValidationInfo();
        boolean blnShouldBackup = ControlHelper.showAlertYesNoDialog(message);
        if (!blnShouldBackup)
        {
            viResult = new ValidationInfo(Info, lm.getString("walletService_backupcancel"));
            return viResult;
        }

        //User wants to backup

        String strBackupFolder = (new FileManager()).getBackupDirectory();

        //append epoch
        String epoch = Long.toString(Utils.getEpochCurrent());
        strBackupFolder = Paths.get(strBackupFolder, epoch).toString();
        Utils.createFolder(strBackupFolder);

        CommandResult<Void> commandResult = NodeCoreGateway.getInstance().backupWallet(strBackupFolder);
        if (commandResult.isSuccess())
        {
            //YAY!
            viResult.setMessageSuccess(String.format(lm.getString("walletService_backup_success"), strBackupFolder));
        }
        else
        {
            viResult.setMessageError(lm.getString("walletService_backup_fail"));
        }
        return viResult;
    }

    //region Lock Wallet

    public static boolean isWalletEncrypted(WalletLockState lockedState)
    {

        if (lockedState == WalletLockState.UNLOCKED || lockedState == WalletLockState.LOCKED)
        {
            //Wallet either locked or unlocked --> implies it must have been encrypted
            return true;
        }
        else if (lockedState == WalletLockState.DEFAULT)
        {
            //originally unlocked
            return false;
        }
        else
        {
            //unknown
            return false;
        }
    }

    public static boolean promptUnlockWallet(AppContext _appContext,NodeCoreGateway ngw)
    {
        return promptUnlockWallet(_appContext, ngw, false);
    }

    //returns true = password collected, false = cancelled
    public static boolean promptUnlockWallet(AppContext _appContext, NodeCoreGateway ngw, boolean alreadyKnowWalletIsLocked)
    {
        //Check if we already have password long term - if so, then no reason to prompt
        if (ngw.getIsWalletPasswordLongTerm())
        {
            return true;
        }

        boolean isWalletLocked;
        if (alreadyKnowWalletIsLocked)
        {
            isWalletLocked = true;
        }
        else {
            isWalletLocked = ngw.isWalletLocked();
        }

        if (isWalletLocked)
        {
            //Show popup
            LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
            String promptMessage = lm.getString("walletService_enterPassword");
            GenericFunction<String, String> testFunc = (strPassword) -> {
                return testWalletPassword(strPassword);
            };

            GetPasswordInput input = new GetPasswordInput();
            input.showShouldRemember = true;
            input.defaultShouldRemember = true;
            input.createNewPassword = false; //unlock doesn't need double-confirm;
            input.testFunc = testFunc;
            Pair<String, Boolean> result = ControlBuilder.showPasswordDialog(_appContext, promptMessage, input);

            if (result != null)
            {
                String password = result.getKey();
                Boolean shouldRemember = result.getValue();
                if (shouldRemember) {
                    ngw.setWalletPasswordLongTerm(password);
                }
                else
                {
                    ngw.setWalletPasswordOnce(password);
                }

                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            //Wallet wasn't locked - nothing to do
            return true;
        }
    }

    /*
    For features that require wallet to remain unlocked long term
    do not should 'shouldRemeber', and default it to true
     */
    public static boolean promptUnlockWalletLongTerm(AppContext _appContext, String promptMessage) {
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();

        //Check if we already have password long term - if so, then no reason to prompt
        //Even if password is temporarily unlocked - we'll need password long term to keep it unlocked
        if (ngw.getIsWalletPasswordLongTerm()) {
            return true;
        }

        //Show popup
        GenericFunction<String, String> testFunc = (strPassword) -> {
            return testWalletPassword(strPassword);
        };

        GetPasswordInput input = new GetPasswordInput();
        input.showShouldRemember = false;   //force always remember
        input.defaultShouldRemember = true;
        input.createNewPassword = false; //unlock doesn't need double-confirm;
        input.testFunc = testFunc;
        Pair<String, Boolean> result = ControlBuilder.showPasswordDialog(_appContext, promptMessage, input);

        if (result == null) {
            return false;
        } else {
            String password = result.getKey();
            ngw.setWalletPasswordLongTerm(password);
            return true;
        }
    }

    /*
        Returns password to use for encrypt/decrypt.
        isEncrypt = true for encrypting, false for decrypting
     */
    public static String promptEncryptWallet(AppContext _appContext, boolean isEncrypt) {
        //Show popup
        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
        String promptMessage = null;

        //Getting just password once to decrypt it
        GetPasswordInput input = new GetPasswordInput();
        input.showShouldRemember = false;
        input.defaultShouldRemember = false;
        input.createNewPassword = true;

        if (isEncrypt)
        {
            //CASE: ENCRYPT
            promptMessage = lm.getString("walletService_enterPasswordEncrypt");
            input.createNewPassword = true;   //specifying a new password, want to confirm it
        }
        else
        {
            //CASE: DECRYPT
            promptMessage = lm.getString("walletService_enterPassword");
            input.createNewPassword = false;
        }

        input.testFunc = (strPassword) -> {
            return testWalletPassword(strPassword);
        };

        Pair<String, Boolean> result = ControlBuilder.showPasswordDialog(_appContext, promptMessage, input);

        if (result != null) {
            String password = result.getKey();
            return password;
        } else {
            return null;
        }
    }

    public static String testWalletPassword(String password)
    {
        //can lock?
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();

        //CommandResult
        CommandResult<Void> result = ngw.unlockwallet(password);

        //check for success
        if (result.isErrorWalletLocked())
        {
            //bad!
            if (result.getValidationInfo() == null)
            {
                return (LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService)).getString("walletService_couldNotUnlock");
            }
            return result.getValidationInfo().getMessage();
        }
        else
        {
            //success!
            ngw.lockwallet();
            return null;
        }
    }

    //endregion
}

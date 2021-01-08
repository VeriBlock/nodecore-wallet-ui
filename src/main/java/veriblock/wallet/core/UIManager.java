// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.shell.MainController;
import veriblock.wallet.features.TabEnum;
import javafx.stage.Stage;

//Wrapper for global UI calls
public class UIManager
{
    private MainController _mainController;
    private Stage _primaryStage;

    public UIManager(MainController mainController, Stage primaryStage)
    {
        _mainController = mainController;
        _primaryStage = primaryStage;
    }

    public void showPopup(String fxmlPath)
    {
        _mainController.showModal(fxmlPath);
    }

    //Hides the current modal
    public void closeModalPoP()
    {
        _mainController.closeModalPopup();
    }

    //Possibly needed if another page had a connection button, and we wanted to manually trigger this
    /*
    public void updateFooter(StateInfoEntity footer)
    {
        _mainController.updateFooter(null);
       // _mainController.updateFooter(footer);
    }
*/
    public Stage getPrimaryStage()
    {
        return _primaryStage;
    }

    public void setTitle(String sectionTitle) {
        _mainController.setTitle(sectionTitle);
    }

    public void setStatusMessageCommandError(CommandResult input)
    {
        setStatusMessageCommandError(input, null);
    }
    public void setStatusMessageCommandError(CommandResult input, String prefixMessage) {
        //TODO --> check between

        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        ConnectionResult connectionResult = input.getConnectionResult();
        ValidationInfo ri = input.getValidationInfo();

        ValidationInfo rNew = null;
        if (connectionResult.ConnectionState == ConnectionState.Connected_Syncing ||
                connectionResult.ConnectionState == ConnectionState.NotSpecified )
        {
            rNew = new ValidationInfo(ValidationInfo.Status.Error,
                    lm.getString("notConnected.mustSync"));
        }
        else if (connectionResult.ConnectionState == ConnectionState.ErrorCouldNotConnect)
        {
            String strEx = "";
            if (connectionResult.Exception != null)
            {
                strEx = connectionResult.Exception.getMessage();
            }
            rNew = new ValidationInfo(ValidationInfo.Status.Error,
                    String.format(lm.getString("notConnected.couldNotConnect"), strEx ));
        }
        else {

            //Connected --> great! but any other command-related errors?
            if (!input.isSuccess()) {

                rNew = ri;
                if (prefixMessage != null && prefixMessage.length() > 0)
                {
                    rNew.setMessageInfo(prefixMessage + " " + rNew.getMessage());
                }
            }
        }

        setStatusMessage(rNew);
    }

    public void setStatusMessage(String informationMessage) {
        ValidationInfo ri = new ValidationInfo(ValidationInfo.Status.Info, informationMessage);
        setStatusMessage(ri);
    }
    public void setStatusMessage(ValidationInfo result)
    {
        _mainController.setStatusMessage(result);
    }
    public void setStatusMessageWarning(String errorMessage)
    {
        ValidationInfo ri = new ValidationInfo(ValidationInfo.Status.Warning, errorMessage);
        _mainController.setStatusMessage(ri);
    }
    public void setStatusMessageError(String errorMessage)
    {
        ValidationInfo ri = new ValidationInfo(ValidationInfo.Status.Error, errorMessage);
        _mainController.setStatusMessage(ri);
    }
    public void setStatusMessageSucess(String successMessage)
    {
        ValidationInfo ri = new ValidationInfo(ValidationInfo.Status.Success, successMessage);
        _mainController.setStatusMessage(ri);
    }

    public void clearStatusMessage()
    {
        _mainController.clearStatusMessage();
    }

    //TODO - cannot call this within a tab... it won't close the first tab when calling the second.
    //Can call it from an external menu link
    public void navigateToTab(TabEnum tabName)
    {
        navigateToTab(tabName, null);
    }
    public void navigateToTab(TabEnum tabName, NavigationData navigationData)
    {
        _mainController.navigateToTab(tabName, navigationData);
    }

    //TODO --> replace with model binding
    public void updateLocaleMain()
    {
        _mainController.updateLocale();
    }

    public void updateFooterBalance(long amount)
    {
        _mainController.updateFooterBalance(amount);
    }

}

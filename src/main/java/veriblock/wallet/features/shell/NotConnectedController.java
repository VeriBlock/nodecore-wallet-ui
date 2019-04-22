// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.
// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.shell;

import veriblock.wallet.core.*;
import javafx.scene.control.*;
import veriblock.wallet.core.cli.DefaultResult;
import veriblock.wallet.core.cli.ExternalProgramUtilities;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.uicommon.ControlHelper;

public class NotConnectedController extends BaseController {
    public void initialize() {

    }

    public void init() {

        setLocale();

        if (_navigationData != null && _navigationData.getData() != null
                && _navigationData.getData().toString().length() > 0)
        {
            String strMessage = _navigationData.getData().toString();
            setIncomingMessage(strMessage);
        }

        String propFile = _appContext.Configuration.getConfigPath();
        hlinkPropFile.setText(propFile);

        ConnectionResult result = NodeCoreGateway.getInstance().getGetStateInfo().getConnectionResult();
        setConnectionError(null, result);
    }

    private void setConnectionError(String messagePrefix, ConnectionResult result)
    {
        //recheck connection
        if (messagePrefix == null)
        {
            messagePrefix = "";
        }
        if (messagePrefix.length() > 0)
        {
            messagePrefix = messagePrefix + ". ";
        }

        String timeStamp = " [" + Utils.getTimeOfDayNow() + "]";

        if (result.IsErrorBadGrpcPassword) {
            String strFormatString = messagePrefix + _localeModule.getString("onloadErrorPassword")
                    + timeStamp;
            String propFile = _appContext.Configuration.getConfigPath();
            this._appContext.UIManager.setStatusMessageError(
                    String.format(strFormatString, propFile, "rpc.security.password=<password>"));
        }
        else if (result.IsErrorUnavailable)
        {
            String strFormatString = messagePrefix +
                    _localeModule.getString("onloadErrorUnavailable")
                    + timeStamp;
            String propFile = _appContext.Configuration.getConfigPath();
            this._appContext.UIManager.setStatusMessageError(
                    String.format(strFormatString, result.ConnectionInput.getAddressPort(), propFile));
        }

        // --> other connection errors?
    }



    private LocaleModule _localeModule;
    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.NotConnected);

        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));

        lblConnectToNC.setText(_localeModule.getString("label.lblConnectToNC"));
        lblPropFile.setText(_localeModule.getString("label.lblPropFile"));
        lblMessageCommunity.setText(_localeModule.getString("label.lblMessageCommunity"));
        lnkForum.setText(_localeModule.getString("label.lnkForum"));

        lblMinVersion.setText(String.format(_localeModule.getString("label.minNCVersion"),
                Constants.MINIMUM_NODECORE_VERSION));

        lblbtnNodeCoreStart.setText(_localeModule.getString("label.lblbtnNodeCoreStart"));
        lblbtnNodeCoreInstall.setText(_localeModule.getString("label.lblbtnNodeCoreInstall"));
        lblbtnNodeCoreReconnect.setText(_localeModule.getString("label.lblbtnNodeCoreReconnect"));

        btnNodeCoreStart.setText(_localeModule.getString("btnNodeCoreStart.text"));
        btnNodeCoreInstall.setText(_localeModule.getString("btnNodeCoreInstall.text"));
        btnNodeCoreReconnect.setText(_localeModule.getString("btnNodeCoreReconnect.text"));

    }


    public Label lblConnectToNC;
    public Label lblPropFile;
    public Label lblMessageCommunity;
    public Hyperlink lnkForum;
    public Hyperlink hlinkPropFile;
    public Label lblIncomingMessage;

    public Label lblbtnNodeCoreStart;
    public Label lblbtnNodeCoreInstall;
    public Label lblbtnNodeCoreReconnect;
    public Label lblMinVersion;

    public Button btnNodeCoreStart;
    public Button btnNodeCoreInstall;
    public Button btnNodeCoreReconnect;

    private void setIncomingMessage(String message)
    {
        this.lblIncomingMessage.setVisible(true);
        this.lblIncomingMessage.setText(message + "\n\n");
    }


    /*
    public void clickLink_github()
    {
        Utils.openLink("https://github.com/veriblock/nodecore-releases");
    }
*/

    public void clickLink_propFile()
    {
        String filePath = hlinkPropFile.getText();
        Utils.openFolder(filePath);
    }

    public void clickLink_wiki()
    {
        Utils.openLink("http://wiki.veriblock.org");
    }

    public void clickLink_forum()
    {
        Utils.openLink("https://discord.gg/wJZEjry");
    }

    private void tryReconnectToNodeCore()
    {
        _appContext.Configuration.reload(); //assume password changed
        ShellService.setupNodeCoreConnection(_appContext);

        //Try reconnect
        ConnectionResult result = NodeCoreGateway.getInstance().getGetStateInfo().getConnectionResult();
        if (result.isConnectedAndSynced())
        {
            _appContext.UIManager.setStatusMessageSucess(_localeModule.getString("tryConnectSuccess"));
            //TODO --> bonus - navigate to previous page
        }
        else
        {
            //nuts, reinit
            setConnectionError(_localeModule.getString("tryConnectRetry"), result);
        }
    }

    private void tryStartNodeCore() {
        //look at current directory
        //recursively search if NodeCore within it

        //detect if a NodeCore process is already running?

        //TODO --> ideally scan local process. (But it could be running remotely, so best case that's just another prompt)
        boolean areYouSure = ControlHelper.showAlertYesNoDialog(_localeModule.getString("startAreYouSure"));
        if (areYouSure == false)
        {
            this._appContext.UIManager.setStatusMessageWarning(_localeModule.getString("startAreYouSure_cancel"));
            return;
        }


        DefaultResult result = new DefaultResult();

        //Get root folder with UI popup, save to settings
        String strTitle = _localeModule.getString("startPickFolder");
        String strDefaultDir = UserSettings.getValue(SettingsConstants.NODECORE_CONTAINING_FOLDER);
        if (strDefaultDir == null || strDefaultDir.length() == 0)
        {
            strDefaultDir = Utils.getCurrentDirectory();
        }
        String nodeCoreContainingFolder = ControlHelper.clickChooseNCDirectory(_appContext,strTitle, strDefaultDir);
        if (nodeCoreContainingFolder != null && nodeCoreContainingFolder.length() > 0)
        {
            //got something! Proceed!
            //Save what the user actually selected
            UserSettings.save(SettingsConstants.NODECORE_CONTAINING_FOLDER, nodeCoreContainingFolder);
        }
        else
        {
            String message = _localeModule.getString("startNoFolder");
            this._appContext.UIManager.setStatusMessageWarning(message);
            return;
        }

        //Have a NC folder - attempt to open it.
        String successFile = ExternalProgramUtilities.startupExternalProcess(result,
                nodeCoreContainingFolder,
                "nodecore-0", "nodecore", "NodeCore");

        if (successFile != null) {
            String message = String.format(_localeModule.getString("startSuccess"), successFile);
            this._appContext.UIManager.setStatusMessageSucess(message);
            return;
        }
        else if (successFile == null || result.didFail())
        {
            String message = "";
            if (result.getMessages().size() > 0)
            {
                message = result.getMessages().get(0).getDetails()
                        .replace("\n"," ")
                        .replace("\t"," "); //cleanse newline/tab formatting
                this._appContext.UIManager.setStatusMessageError(message);
                return;
            }
        }
    }


    //region NodeCore reconnect links

    public void clickStartNC()
    {
        tryStartNodeCore();
    }

    public void clickInstallNC()
    {
        //simply open up wiki link
        String installPage = "https://wiki.veriblock.org/index.php?title=NodeCore_QuickStart";
        Utils.openLink(installPage);
        this._appContext.UIManager.setStatusMessage(String.format(_localeModule.getString("installSuccess"), installPage));
    }

    public void clickReconnectNC()
    {
        tryReconnectToNodeCore();
    }

    //endregion
}

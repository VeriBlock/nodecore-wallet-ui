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

package veriblock.wallet.features.pop;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.cli.DefaultResult;
import veriblock.wallet.core.cli.ExternalProgramUtilities;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.pop.*;
import veriblock.wallet.core.pop.entities.MinerPropertiesEntity;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.shell.SettingsConstants;
import veriblock.wallet.uicommon.BackgroundTask;
import veriblock.wallet.uicommon.ControlHelper;


public class ConnectPoPController extends DialogController {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectPoPController.class);
    public void initialize() {
    }

    private LocaleModule _localeModule;

    public void init() {
        setLocale();

        _backgroundTask = new BackgroundTask();

        //Now wait for PoP to get ready
        _clientProxy = PopService.getApiProxy();

        //wait and confirm that it indeed loads!
        _backgroundTask.start(
                _waitInternalSeconds,
                () -> {
                    background_loop();
                    return null;
                },
                () -> {
                    background_done();
                    return null;
                }
        );
        //have background check close the dialog upon success
    }

    private ApiProxy _clientProxy;
    private BackgroundTask _backgroundTask;
    public Label lblResult;

    public Label lblMessage1;
    public Label lblMessage2;
    public Label lblMessage3;

    public Hyperlink hlnkHelp;
    public Button btnStartPoP;
    public Button btnCancel;

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabPoP);

        lblMessage1.setText(_localeModule.getString("ConnectPoP_label_message1"));
        lblMessage2.setText(_localeModule.getString("ConnectPoP_label_message2"));
        lblMessage3.setText(_localeModule.getString("TabPoP_connect_onlyOne"));
        hlnkHelp.setText(_localeModule.getString("ConnectPoP_hlnk_help"));

        btnStartPoP.setText(_localeModule.getString("ConnectPoP_button_startpop"));
        btnCancel.setText(_localeModule.getString("ConnectPoP_button_closeWindow"));

    }

    public void dispose()
    {
        _backgroundTask.dispose();
    }

    private void setStatus(String s) {
        this.lblResult.setText(s);
    }

    private int _waitInternalSeconds = 2;

    private boolean clickedStartButtonSuccess = false;
    public void clickStartPoP() {
        ValidationInfo vi = startPopService();

        //Display results
        if (vi.isWarningOrError())
        {
            //bad
            setStatus(vi.getMessage());
            return;
        }
        else
        {
            //good - tell the user that we're waiting
            setStatus(_localeModule.getString("ConnectPoP_status_started"));
        }

        clickedStartButtonSuccess = true;

    }

    private ValidationInfo startPopService() {
        ValidationInfo vi = new ValidationInfo();

        //Get root folder with UI popup, save to settings
        String strTitle = _localeModule.getString("ConnectPoP_pickfolder_1");
        String strDefaultDir = UserSettings.getValue(SettingsConstants.POP_CONTAINING_FOLDER);
        if (strDefaultDir == null || strDefaultDir.length() == 0) {
            strDefaultDir = Utils.getCurrentDirectory();
        }
        String popContainingFolder = ControlHelper.clickChooseNCDirectory(_appContext, strTitle, strDefaultDir);
        if (popContainingFolder != null && popContainingFolder.length() > 0) {
            //got something! Proceed!
            //Save what the user actually selected
            UserSettings.save(SettingsConstants.POP_CONTAINING_FOLDER, popContainingFolder);
        } else {
            String message = _localeModule.getString("ConnectPoP_pickfolder_none");
            vi.setMessageWarning(message);
            return vi;
        }

        DefaultResult result = new DefaultResult();

        String successFile = ExternalProgramUtilities.startupExternalProcess(
                result, popContainingFolder,
                "nodecore-pop-0",
                "nodecore-pop",
                "NodeCore PoP Miner",
                "-skipAck");

        if (successFile == null) {
            vi.setMessageWarning(_localeModule.getString("ConnectPoP_pickfolder_couldNotStart"));
        } else {
            vi.setMessageInfo(String.format(_localeModule.getString("ConnectPoP_pickfolder_successStart"), successFile));
            _logger.info("Started PoP CLI at: " + successFile);
        }
        return vi;
    }

    public void clickCancel() {
        ValidationInfo vi = new ValidationInfo();
        vi.setMessageWarning(_localeModule.getString("ConnectPoP_pickfolder_cancel"));
        this.closeDialog(vi);
    }

    //region Check PoP Service ready

    public void background_loop()
    {
        MinerPropertiesEntity minerProps = _clientProxy.getMinerPropertiesEntity();
        if (minerProps != null
                && (minerProps.minerAddress != null || minerProps.walletSeeds != null ))
        {
            //minerAddress should always be populated.
            //walletSeeds populated only on 1st run
            if (minerProps.walletSeeds != null)
            {
                //Capture seeds on 1st run
                _walletSeeds = minerProps.walletSeeds;
                _btcAddress = minerProps.bitcoinAddress;
            }

            _isPopReady = true;
        }
        _logger.info("PoP Connection dialog check. IsReady={}", _isPopReady);
    }

    private boolean _isPopReady = false;
    private int _waitAttempts = 0;

    private  String[] _walletSeeds;
    private String _btcAddress;

    public void background_done() {

        if (clickedStartButtonSuccess)
        {
            _waitAttempts++;

            //Check total time, if it is > X, then troubleshoot
            int maxWaitTime = 20;
            if (_waitInternalSeconds * _waitAttempts > maxWaitTime) {
                //Something is wrong
                this.setStatus(_localeModule.getString("ConnectPoP_warning_shouldBeDone"));
                return;
            }

            if (_isPopReady) {
                //Awesome!
                _logger.info("PoP Service was started, will auto-close window");
                ValidationInfo vi = new ValidationInfo();
                vi.setMessageInfo(_localeModule.getString("ConnectPoP_success"));

                //First time running this PoP Instance
                if (_walletSeeds != null)
                {
                    //STEP 1: Show Seeds
                    //String seedCsv = String.join(", ", _walletSeeds);
                    ValidationInfo vi2 = new ValidationInfo();
                    vi2.setMessageInfo(_localeModule.getString("ConnectPoP_firsttime_header"));
                    ControlHelper.showAlertYesNoDialog(vi2, createSeedMessage(_walletSeeds, _localeModule));

                    //STEP 2: Add to wallet
                    String promptMessage =_localeModule.getString("ConnectPoP_firsttime_prompt");
                    Object result = ControlHelper.showCustomDialog("pop/AddBtc", _appContext, promptMessage);
                }

                this.closeDialog(vi);
            } else {
                String s = String.format(_localeModule.getString("ConnectPoP_loading"),
                        Utils.getTimeOfDayNow());
                this.setStatus(s);
            }
        }
        else {
            //User hasn't clicked any button yet
            //They could open PoP from an external process, or have closed the previous window and re-opened before PoP fully started
            if (_isPopReady) {
                //PoP was already running
                _logger.info("PoP Service already running, will auto-close window");
                ValidationInfo vi = new ValidationInfo();
                vi.setStatus(ValidationInfo.Status.Success);
                this.closeDialog(vi);
                return;
            }
        }
    }

    public static String createSeedMessage(String[] seedCsv, LocaleModule localeModule )
    {
        //if first char is digits, then use at create stamp

        if (seedCsv == null || seedCsv.length == 0)
        {
            return null;
        }

        /*
            WALLET CREATION TIME:
                    1549402633
            SEED WORDS:
                    thunder
                    number
                    drill
        */

        StringBuilder sb = new StringBuilder();

        int iSeedStart = 0;

        String createTime = seedCsv[0];
        if (Utils.isLong(createTime)) {
            sb.append(localeModule.getString("ConnectPoP_firsttime_walletCreateTime") + ":\n");
            sb.append("\t" + createTime + "\n");

            iSeedStart = 1;
        }

        sb.append(localeModule.getString("ConnectPoP_firsttime_seedwords") + ":\n");
        for (int i = iSeedStart; i < seedCsv.length; i++)
        {
            sb.append("\t" + seedCsv[i] + "\n");
        }

        return sb.toString();

    }
    //endregion

    public void click_HelpLink()
    {
        String helpLink = PopService.getPopHelpLink();
        Utils.openLink(helpLink);
    }


    //Background loop continually checks, but allow anyway, say prior to clicking start button
    public void clickTest()
    {
        //TODO_TRANSLATE
        boolean isConnected = _clientProxy.isConnected();
        ValidationInfo vi = new ValidationInfo();
        if (isConnected)
        {
            vi.setMessageSuccess(String.format("PoP Service is connected and ready [%1$s]",
                    Utils.getTimeOfDayNow()));
        }
        else
        {
            vi.setMessageWarning(String.format("PoP Service is NOT ready [%1$s]",
                    Utils.getTimeOfDayNow()));
        }
        this.setStatus(vi.getMessage());
    }


}

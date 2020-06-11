// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.
// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.shell;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.locale.SupportedLocales;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.core.*;
import veriblock.wallet.entities.StateInfoEntity;
import veriblock.wallet.features.wallet.WalletService;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.features.TabEnum;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.core.utilities.DiagnosticInfo;
import org.veriblock.core.utilities.DiagnosticUtility;

public class MainController {

    private static final Logger _logger = LoggerFactory.getLogger(MainController.class);
    private static final Integer BACKGROUND_LOOP_SECONDS_STATEINFO = 2;

    public Label lblTabTitle;
    public Label statusIcon;
    public TextArea lblStatusMessage;
    public HBox statusBox;

    public void setPrimaryStage(Stage primaryStage)
    {
        _primaryStage = primaryStage;
    }
    private Stage _primaryStage;

    private AppContext _appContext;
    public void initialize() {}

    public Button BtnTest2;


    public void start() {

        //Only show button in test mode
        this.BtnTest2.setVisible(Constants.IS_TEST_MODE);

        //TEMP
        //Hide PoP tab
        //this.btnPoP.setVisible(false);
        //-----


        _logger.info("Start Application Wallet");

        _appContext = new AppContext();
        _appContext.UIManager = new UIManager(this, _primaryStage);
        _appContext.Configuration = DefaultConfiguration.getInstance();

        logDiagnosticInfo();

        configureLocale();
        setLocale();

        NodeCoreGateway ngw = NodeCoreGateway.getInstance();
        ShellService.setupNodeCoreConnection(_appContext);


        //Add footer
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Footer.fxml"));
            AnchorPane panelFooter = loader.load();
            _footer = loader.getController();
            _footer.initData(_appContext, null);

            ControlHelper.makePaneFillContainer(panelFooter, this.pnlFooter);
            this.pnlFooter.getChildren().clear();
            this.pnlFooter.getChildren().add(panelFooter);
        } catch (Exception ex) {
            _logger.error("Error loading footer: {}", ex);
        }

        //reset form:
        this.closeModalPopup();

        this.clearStatusMessage();

        this.navigateToTab(TabEnum.MyAddresses);
        this.setNetworkVersion();   //need properties from config first
        kickoffBackground();

        //disable buttons not implemented yet
        //Hide console, was right below Transactions tab
        this.btnConsole.setDisable(true);
        this.btnConsole.setVisible(false);
        this.imgButtonConsole.setVisible(false);
        //this.btnConsole.setMaxHeight(0);
        //this.btnSettings.setDisable(true);

        setupImages();

        //Show splash page
        this.showModal("Welcome.fxml");
    }

    private LocaleModule _localeModule;
    private void logDiagnosticInfo()
    {
        DiagnosticInfo di = DiagnosticUtility.getDiagnosticInfo();
        String strDiagnostics = (new GsonBuilder().setPrettyPrinting().create().toJson(di));
        _logger.info(strDiagnostics);
    }

    //This is relatively rare
    public void updateLocale()
    {
        setLocale();

        //renavigate to current page to ensure it's updated

        if (_currentTab != TabEnum.Unknown)
        {
           navigateToTab(_currentTab);
        }
    }

    private void configureLocale()
    {
        LocaleManager lm = LocaleManager.getInstance();

        //First check user_settings, if not saved there, then check system
        String localOverride = UserSettings.getValue(SettingsConstants.LOCALE_OVERRIDE);
        SupportedLocales.SupportedLocale locale;
        if (localOverride != null && localOverride.length() > 0 )
        {
            locale = lm.convertStringLocal(localOverride);
        }
        else
        {
            locale = lm.getSystemLocale();
        }

        //This will also handle if unknown:
        lm.setLocal(locale);
    }

    //Set images in code so that the loading just works for deployment
    private void setupImages()
    {
        ControlHelper.setImage(imgLogo, "circle-logo.png");
    }

    public javafx.scene.image.ImageView imgLogo;
    public javafx.scene.image.ImageView imgButtonSend;
    public javafx.scene.image.ImageView imgButtonTransaction;
    public javafx.scene.image.ImageView imgButtonConsole;
    public javafx.scene.image.ImageView imgButtonAddress;
    public javafx.scene.image.ImageView imgButtonSettings;
    public javafx.scene.image.ImageView imgButtonPoP;
    public javafx.scene.image.ImageView imgButtonBackup;
    public javafx.scene.image.ImageView imgButtonHelp;
    public javafx.scene.image.ImageView imgButtonDiagnostics;
    public javafx.scene.image.ImageView imgButtonAbout;

    public ToggleButton btnSend;
    public ToggleButton btnTransactions;
    public ToggleButton btnConsole;
    public ToggleButton btnMyAddresses;
    public ToggleButton btnSettings;
    public ToggleButton btnPoP;
    public ToggleButton btnBackup;
    public ToggleButton btnHelp;
    public ToggleButton btnDiagnostics;
    public ToggleButton btnAbout;

    private FooterController _footer;
    public Pane pnlFooter;
    public Pane cMain;

    //region Set Main Panel

    public void navigateToTab(TabEnum tabName)
    {
        navigateToTab(tabName, null);
    }
    public void navigateToTab(TabEnum tabName, NavigationData navigationData)
    {
        SoundItem.playButtonClick();

        _currentTab = tabName;
        String pageName = null;
        switch (tabName) {
            //Shell
            case Help:
                pageName = "shell/TabHelp";
                break;
            case About:
                pageName = "shell/TabAbout";
                break;
            case NotConnected:
                pageName = "shell/NotConnected";
                break;

            //Tools
            case Diagnostics:
                pageName = "tools/TabDiagnostics";
                break;
            case Console:
                pageName = "tools/TabConsole";
                break;
            case Settings:
                pageName = "tools/TabSettings";
                break;

            //Wallet
            case Backup:
                pageName = "wallet/TabBackup";
                break;
            case Transactions:
                pageName = "wallet/TabTransactions";
                break;
            case MyAddresses:
                pageName = "wallet/TabMyAddresses";
                break;
            case Send:
                pageName = "wallet/TabSend";
                break;

            //PoP
            case PoPMine:
                pageName = "pop/TabPoP";
                break;

            default:
                pageName = "shell/TabHelp";
                break;
        }

        //Note to ensure we find the fxml, even when compiled into JAR:
        //1 - use absolute path
        //2 - use url
        //URL fxmlPath = getClass().getResource(String.format("/veriblock/wallet/features/%1$s.fxml", pageName));;
        SetPane(pageName, navigationData);
    }

    //Reference to current controller so we can dispose it before adding a new one
    private BaseController _controllerCurrent = null;

    private void SetPane(String pageName, NavigationData navigationData)
    {
        try
        {
            _logger.info("About to load tab {}", pageName);
            FXMLLoader loader = ControlBuilder.getFXMLLoader(pageName);
            //FXMLLoader loader = new FXMLLoader(fxmlPath);
            AnchorPane panel = loader.load();

            //Clear status first, so that anything called from initData will take precedent and call status
            this.clearStatusMessage();

            BaseController controller = loader.getController();
            if (controller != null) {
                controller.initData(_appContext, navigationData);
            }

            //Kill old
            if (_controllerCurrent != null) {
                try {
                    _controllerCurrent.dispose();
                }
                catch (Exception ex2)
                {
                    //swallow anything from dispose --> always want to be able to navigate to new tab
                    _logger.error("Exception calling dispose fromc controller: {}", ex2);

                }
            }

            this.cMain.getChildren().clear();

            //Add new
            this.cMain.getChildren().add(panel); //Yes, can add children here
            _controllerCurrent = controller;

            ControlHelper.makePaneFillContainer(panel, this.cMain);

        }
        catch (Exception ex)
        {
            String s = ex.getMessage();
            _logger.error("Error loading tab {}: {}", pageName, ex);
        }
    }

    private TabEnum _currentTab = TabEnum.Unknown;


    private void navigateToTabConnected(TabEnum tabEnum, boolean requiresConnection, String message)
    {
        //if not connected, then redirect
        boolean isConnected = NodeCoreGateway.getInstance().isConnected();
        String strPrefix = null;
        if (isConnected) {
            //Great, just pass through
            navigateToTab(tabEnum);
        }
        else
        {
            navigateToTab(TabEnum.NotConnected, new NavigationData(message));
        }
    }

    public void clickTab_Send(ActionEvent actionEvent) {
        navigateToTabConnected(TabEnum.Send, true,
                _localeModule.getString("notConnected.navigate.send"));
    }

    public void clickTab_Transactions(ActionEvent actionEvent) {
        navigateToTabConnected(TabEnum.Transactions, true,
                _localeModule.getString("notConnected.navigate.transactions"));
    }

    public void clickTab_Console(ActionEvent actionEvent) {
        //SetPane("TabTransactions.fxml");
    }

    public void clickTab_MyAddresses(ActionEvent actionEvent) {
        navigateToTab(TabEnum.MyAddresses);
        //Does not require a connection --> will show cached values if not connected

    }

    public void clickTab_Settings(ActionEvent actionEvent) {
        navigateToTab(TabEnum.Settings);
    }

    public void clickTab_PoP(ActionEvent actionEvent) {
        navigateToTabConnected(TabEnum.PoPMine, true,
                _localeModule.getString("notConnected.navigate.pop"));
    }

    public void clickTab_Backup(ActionEvent actionEvent) {
        navigateToTabConnected(TabEnum.Backup, true,
                _localeModule.getString("notConnected.navigate.backup"));
    }

    public void clickTab_Help(ActionEvent actionEvent) {
        navigateToTab(TabEnum.Help);
    }

    public void clickTab_Diagnostics(ActionEvent actionEvent) {
        navigateToTab(TabEnum.Diagnostics);
    }

    public void clickTab_About(ActionEvent actionEvent) {
        navigateToTab(TabEnum.About);
    }

    //endregion

    //region Background - update UI State task

    //First check getStateInfo every 2 seconds. --> fast call, ok to check it frequently (do off UI thread)
    //Always update AppContext.CurrentBlockHeight
    //If getStateInfo.blockHeight increases, then:
    //  1. call BaseController.onCurrentBlockChanged
    //  2. check balance (do off UI thread)
    //      If balance changed, then call BaseController.onBalanceChanged

    private void kickoffBackground() {
        _logger.info("Run Background call every {} second(s)", BACKGROUND_LOOP_SECONDS_STATEINFO);

        //THREAD 1: ConnectionState, which is very frequent
        Task task1 = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                while (true) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            updateBackground_getStateInfo_start();
                        }
                    });
                    Thread.sleep(1000 * BACKGROUND_LOOP_SECONDS_STATEINFO); //This will not block the UI thread
                }
            }
        };
        Thread th1 = new Thread(task1);
        th1.setDaemon(true);
        th1.start();

    }

    public void updateBackground_getStateInfo_start() {
        //do this every N seconds
        //This is still the UI thread, and hence blocking
        _logger.debug("Background call - updateBackground_start");

        //get current connected status
        //Continual heartbeat to getstateinfo
        if (_getStateInfoTask == null //don't start new ping until previous one finished
                ) {

            //this.updateProgressSpinner(true); //looks glitchy, runs every 2 seconds
            _getStateInfoTask = new GetStateInfoTask();
            _getStateInfoTask.setOnSucceeded(e -> {
                updateBackround_getStateInfo_done();
            });
            new Thread(_getStateInfoTask).start();
        } else {
            _logger.debug("GetStateInfoTask in progress...");
        }
    }

    private GetStateInfoTask _getStateInfoTask;
    private int currentBlockHeight = -1;
    private long newBalanceAmount = (long)-1;

    public class GetStateInfoTask extends Task<CommandResult<StateInfoEntity>> {
        @Override
        protected CommandResult<StateInfoEntity> call() throws Exception {
            _logger.debug("AsyncTask GetStateInfoTask - Start");

            //Long running task here

            NodeCoreGateway ngw = NodeCoreGateway.getInstance();
            CommandResult<StateInfoEntity> commandResult = ngw.getGetStateInfo();

            if (commandResult.isSuccess())
            {
                int newBlockHeight = commandResult.getPayload().LocalBlockchainHeight;

                if (newBlockHeight > currentBlockHeight)
                {
                    //block increased!
                    _logger.info("Detected blockheight increased from {} to {}", currentBlockHeight, newBlockHeight);

                    //should call getBalance - not on the UI thread
                    newBalanceAmount = WalletService.getTotalBalance();
                }
            }

            return commandResult;
        }
    }

    //Finish call (run continuously)
    private void updateBackround_getStateInfo_done()
    {
        CommandResult<StateInfoEntity> commandResult = _getStateInfoTask.getValue();
        _logger.debug("AsyncTask GetStateInfoTask - Done: value={}", commandResult.getPayload());

        _footer.updateStateInfo(commandResult);

        //_logger.info("TEST - call updateBackround_getStateInfo_done!");
        if (_controllerCurrent != null && commandResult.isSuccess() && commandResult.getPayload() != null)
        {
            int newBlockHeight = commandResult.getPayload().LocalBlockchainHeight;
            //Always set this, keep it current:
            _appContext.setCurrentBlockHeight(newBlockHeight);

            //Only call when changes
            if (currentBlockHeight != newBlockHeight) {
                //Call BaseController method!
                _controllerCurrent.onCurrentBlockChanged(currentBlockHeight, newBlockHeight);

                //Also check for balance changed
                updateBackground_getBalance_done(newBalanceAmount);
            }

            //Reset it!
            currentBlockHeight = newBlockHeight;
        }

        //this.updateProgressSpinner(false);  //looks glitchy, runs every 2 seconds

        _getStateInfoTask = null;   //reset to allow next loop to call it
    }

    //endregion

    //region Background - update balance

    /*
    Other places could call this
     */
    public void updateFooterBalance(long amount)
    {
        _logger.info("Calling updateFooterBalance, outside of background loop. Amount={}",
                VbkUtils.convertAtomicToVbkString(amount));

        updateBackground_getBalance_done(amount);
    }

    //Finish call (run continuously)
    private long _oldAmount = (long)-1;
    private void updateBackground_getBalance_done(long amount)
    {
        //_logger.info("TEST - call updateBackground_getBalance_done! {}", amount);

        _footer.updateBalance(amount);

        //_logger.info("test - VBK Amount increased from {} to {}, diff={}", _oldAmount, amount,
        //        VbkUtils.KILL_formatVbkAmount(amount - _oldAmount));
        //if new amount > old amount, then call
        if (amount > _oldAmount && _oldAmount > -1.0)
        {
            increasedVbkAmount(_oldAmount, amount);
        }

        _oldAmount = amount;
    }

    private void increasedVbkAmount(long oldAmount, long newAmount) {
        SoundItem.playCoinsReceived();
        _logger.info("VBK Amount increased from {} to {}, diff={}",
                VbkUtils.convertAtomicToVbkString(oldAmount),
                VbkUtils.convertAtomicToVbkString(newAmount),
                VbkUtils.convertAtomicToVbkString(newAmount - oldAmount));

        if (_controllerCurrent != null) {
            boolean didChange = (oldAmount != newAmount);
            _controllerCurrent.onBalanceChanged(didChange, oldAmount, newAmount);
        }
    }


    //endregion

    /*
    //OnDemand --> currently footer gets updated solely from loop
    public void updateFooter(CommandResult<StateInfoEntity> commandResult)
    {
        _footer.updateStateInfo(commandResult);
    }
    */

    public void updateProgressSpinner(boolean shouldStart) {
        if (_footer != null) {
            _footer.updateProgressSpinner(shouldStart);
        }
    }


    public void clickTest2()
    {
        SetPane("tools/TabTest", null);
    }

    //region Modal Dialogue

    public AnchorPane pnlModalBlocker;
    public AnchorPane pnlModalDialogue;

    public void closeModalPopup()
    {
        showModal(false, null);
    }

    public void showModal(String fxmlPath)
    {
        showModal(true, fxmlPath);
    }

    private void showModal(boolean show, String fxmlPath)
    {
        //this.Lbltest1.setText("click Test" + show.toString());

        if (show)
        {
            _logger.info("Show popup: " + fxmlPath);
            //Add the control
            try
            {
                this.pnlModalBlocker.setVisible(true);
                this.pnlModalDialogue.setVisible(true);

                FXMLLoader.load(getClass().getResource(fxmlPath));
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                AnchorPane panel = loader.load();
                BaseController controller = loader.getController();
                controller.initData(_appContext, null);   //must pass this in!
                //Set X/Y position

                this.pnlModalDialogue.getChildren().clear();

                //add some padding --> can do in FXML?
                panel.setLayoutX(5);
                panel.setLayoutY(5);

                this.pnlModalDialogue.getChildren().add(panel);
            }
            catch (Exception ex)
            {
                //TODO
                _logger.info("Error showing panel: {}", ex);
            }
        }
        else
        {
            _logger.info("Hide popup" );

            //hide the control
            this.pnlModalBlocker.setVisible(false);
            this.pnlModalDialogue.setVisible(false);

            this.pnlModalBlocker.getChildren().clear();
        }


    }

    //endregion

    //region Status Bar



    public void setTitle(String sectionTitle)
    {
        this.lblTabTitle.setText(sectionTitle);
    }

    //if null, then clear
    public void clearStatusMessage()
    {
        setStatusMessage(null);
    }
    public void setStatusMessage(ValidationInfo result) {
        if (result == null) {
            this.statusBox.setVisible(false);
            ControlHelper.setStyleClass(this.statusBox, "");
        } else {
            this.statusBox.setVisible(true);

            String status = result.getStatus().toString();
            String strMessage = String.format("%2$s", status, result.getMessage());
            this.lblStatusMessage.setText(strMessage);

            ValidationInfo.Status statusEnum = result.getStatus();

            Text icon;
            switch (statusEnum) {
                case Success:
                    this.lblStatusMessage.setStyle("-fx-text-fill: #0ab45b");
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.CHECK_CIRCLE);
                    icon.setFill(Color.valueOf("#0ab45b"));
                    statusIcon.setGraphic(icon);
                    break;
                case Info:
                    this.lblStatusMessage.setStyle("-fx-text-fill: #ffffff");
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.INFO_CIRCLE);
                    icon.setFill(Color.valueOf("#ffffff"));
                    statusIcon.setGraphic(icon);
                    break;
                case Warning:
                    this.lblStatusMessage.setStyle("-fx-text-fill: orange");
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.EXCLAMATION);
                    icon.setFill(Color.ORANGE);
                    statusIcon.setGraphic(icon);
                    break;
                case Error:
                    this.lblStatusMessage.setStyle("-fx-text-fill: #ff6666");
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.TIMES_CIRCLE);
                    icon.setFill(Color.valueOf("#ff6666"));
                    statusIcon.setGraphic(icon);
                    break;
            }
        }
    }

    //endregion

    public Label lblLeftNavTop;

    private void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        lblLeftNavTop.setText(_localeModule.getString("leftnav.toplabel.text"));

        btnSend.setText(_localeModule.getString("leftnav.button.send.text"));
        btnTransactions.setText(_localeModule.getString("leftnav.button.transaction.text"));
        btnConsole.setText(_localeModule.getString("leftnav.button.console.text"));
        btnMyAddresses.setText(_localeModule.getString("leftnav.button.myaddress.text"));
        btnSettings.setText(_localeModule.getString("leftnav.button.settings.text"));
        btnPoP.setText(_localeModule.getString("leftnav.button.pop.text"));
        btnBackup.setText(_localeModule.getString("leftnav.button.backup.text"));
        btnHelp.setText(_localeModule.getString("leftnav.button.help.text"));
        btnDiagnostics.setText(_localeModule.getString("leftnav.button.diagnostics.text"));
        btnAbout.setText(_localeModule.getString("leftnav.button.about.text"));

        if (_footer != null) {
            _footer.setLocale();
        }
    }

    private void setNetworkVersion()
    {
         String  strTitle = String.format(_localeModule.getString("window.label"),
                 _appContext.Configuration.getNetworkName());

        _appContext.UIManager.getPrimaryStage().setTitle(strTitle);
    }

}

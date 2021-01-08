// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.shell;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.syncEstimator.*;
import veriblock.wallet.entities.StateInfoEntity;
import veriblock.wallet.entities.WalletLockState;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.wallet.WalletService;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.features.TabEnum;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import veriblock.wallet.uicommon.Styles;

public class FooterController  extends BaseController  {

    private static final Logger _logger = LoggerFactory.getLogger(FooterController.class);

    public void initialize()
    {
    }

    public Label lblConnected_1;
    public Hyperlink lblConnectedLink;
    public Label lblConnected2;
    public Label lblRow2Text;
    public AnchorPane panelRoot;
    public ProgressIndicator progressSpinner;

    private LocaleModule _localeModule;

    private Estimator _syncEstimator;

    public Label lblWalletLockIcon;
    public Tooltip lblWalletLockIconTooltip;

    public void init()
    {
        setLocale();

        int defaultPointsToKeep = 31;   //at 2-second block, -1, force a 60-second duration

        _syncEstimator = new Estimator(defaultPointsToKeep);

        //Default do not spin:
        updateProgressSpinner(false);
    }

    public void updateProgressSpinner(boolean shouldStart) {
        if (shouldStart) {
            this.progressSpinner.setVisible(true);
        } else {
            //stop it!
            this.progressSpinner.setVisible(false);
        }
    }

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        setWalletIconState(_currentLockState, true);    //re-setting locale
    }

    //Optimization - only update if changed
    private WalletLockState _currentLockState = WalletLockState.UNKNOWN;
    private void setWalletIconState(WalletLockState lockState, boolean forceOverride)
    {
        if (!forceOverride) {
            if (_currentLockState == lockState) {
                return;
            }
        }
        _currentLockState = lockState;

        //Update:
        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);

        if (lockState == WalletLockState.LOCKED) {
            final Text icon = GlyphsDude.createIcon(FontAwesomeIcon.LOCK);
            icon.setFill(Color.valueOf(Styles.VALUE_COLOR_WHITE));
            lblWalletLockIcon.setGraphic(icon);
            lblWalletLockIcon.setVisible(true);
            this.lblWalletLockIconTooltip.setText(lm.getString("footer_lockwallet_shouldUnlocked"));
        } else if (lockState == WalletLockState.UNLOCKED) {
            final Text icon = GlyphsDude.createIcon(FontAwesomeIcon.UNLOCK);
            icon.setFill(Color.valueOf(Styles.VALUE_COLOR_WHITE));
            lblWalletLockIcon.setGraphic(icon);
            lblWalletLockIcon.setVisible(true);
            this.lblWalletLockIconTooltip.setText(lm.getString("footer_lockwallet_shouldLocked"));
        } else {
            lblWalletLockIcon.setVisible(false);
            this.lblWalletLockIconTooltip.setText("");
        }
    }

    public void clickWalletLocked() {
        //if locked, then prompt to unlock
        _logger.info("clicked Wallet lock icon");

        //if current state is locked --> then prompt with unlock
        //if current stats is unlocked --> prompt with lock


        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);

        if (_currentLockState == WalletLockState.LOCKED)
        {
            //Prompt to unlock --> will request password
            boolean setPassword = WalletService.promptUnlockWallet(_appContext, NodeCoreGateway.getInstance(), true);
            if (setPassword)
            {
                NodeCoreGateway.getInstance().unlockWallet();
            }
        }
        else if (_currentLockState == WalletLockState.UNLOCKED)
        {
            //Prompt to lock --> simply erase password
            String message = lm.getString("footer_lockwallet_locked");
            boolean shouldContinue = ControlHelper.showAlertYesNoDialog(message);
            if (shouldContinue)
            {
                //action!
                NodeCoreGateway.getInstance().lockwallet();
                setWalletIconState(WalletLockState.LOCKED, false);
            }
            else
            {
                //do nothing
            }
        }

    }

    //endregion

    //region Connection Status


    //Multiple footer components (connectionInfo, stateInfo, balance)... not a single model
    public void updateStateInfo(CommandResult<StateInfoEntity> commandResult)
    {
        //TODO --> only update if changed, but block changes every 30 seconds
        //Locked state changes on demand

        //CASES:
        //  Not Connected_Synced
        //  Connected_Synced to 127.0.0.1:10501 (Block 1234)
        //  Connected_Synced to 127.0.0.1:10501 (Syncing, at block 2 of 9999)
        //  Trying to connect to 127.0.0.1:10501
        //  Could not connect to 127.0.0.1:10501

        String address = commandResult.getConnectionResult().ConnectionInput.getAddress();
        Integer port = commandResult.getConnectionResult().ConnectionInput.getPort();
        ConnectionState connectionState = commandResult.getConnectionResult().ConnectionState;

        int localBlockchainHeight = 0;
        int networkHeight = 0;

        if (commandResult.getPayload() != null) {
            localBlockchainHeight = commandResult.getPayload().LocalBlockchainHeight;
            networkHeight = commandResult.getPayload().NetworkHeight;
        }

        //Set locked state
        if (commandResult.getPayload() != null)
        {
            setWalletIconState(commandResult.getPayload().walletLockState, false);
        }

        //Start simple:
        String strConnected = "...";
        String strConnectedBlock = "";
        String strConnected2 = "";
        String strBlock = "";

        //ensure minimum version
        String actualVersion = commandResult.getPayload().ProgramVersion;
        String minRequiredVersion = Constants.MINIMUM_NODECORE_VERSION;
        boolean isSufficientNCVersion = Utils.isHigherNodeCoreVersion(actualVersion, minRequiredVersion);

        this.lblRow2Text.setText("");

        if (!isSufficientNCVersion)
        {
            //Check the basics first!
            ControlHelper.setStyleClass(this.panelRoot, "footerCannotConnect");
            strConnected = String.format("Required NodeCore minimum version is %1$s, but this is only version %2$s. Please upgrade NodeCore",
                    minRequiredVersion, actualVersion);
        }
        else if (connectionState == ConnectionState.Connected_Synced) {
            //GOOD, all other scenarios mean NC is not ready yet
            ControlHelper.setStyleClass(this.panelRoot, "footerConnected");

            strConnected = String.format(_localeModule.getString("footer.connection.connectsync"),address, port);
            if (commandResult.getPayload().LocalBlockchainHeight > 0) {
                //strBlock = String.format(" (Block %1$s)", localBlockchainHeight);
                strConnected = strConnected + " (" + _localeModule.getString("footer.connection.block") + " ";
                strConnectedBlock = String.format("%1$s", localBlockchainHeight);
                strConnected2 = ")";
            }
        }
        else if (connectionState == ConnectionState.ErrorCouldNotConnect)
        {
            ControlHelper.setStyleClass(this.panelRoot, "footerCannotConnect");
            strConnected = String.format(_localeModule.getString("footer.connect.error"),address, port);
        }
        else if (connectionState == ConnectionState.NotSpecified)
        {
            ControlHelper.setStyleClass(this.panelRoot, "footerDefault");
            strConnected = _localeModule.getString("footer.connect.notspecified");
        }
        else if (connectionState == ConnectionState.Connected_Syncing)
        {
            _syncEstimator.addBlockHeight(localBlockchainHeight, networkHeight);
            String syncMessage = getSyncMessage();
            this.lblRow2Text.setText(syncMessage);

            ControlHelper.setStyleClass(this.panelRoot, "footerSyncing");
            //Connected_Synced to 127.0.0.1:10501 (Syncing, at block 2 of 9999)
            strConnected = String.format(_localeModule.getString("footer.connect.syncing"), address, port);
            if (networkHeight == 0)
            {
                //TT_Finding peers and initializing, this may take a few minutes...
                strConnected = strConnected + " " + "(" + _localeModule.getString("footer.connect.syncing.init") + ")";
            }
            else if (localBlockchainHeight > 0) {
                strConnected = strConnected + String.format(" (" + _localeModule.getString("footer.connection.block") + " ");
                strConnectedBlock = String.format("%1$s", localBlockchainHeight);
                // (Block X of Y.)
                strConnected2 = String.format(" " + _localeModule.getString("footer.connection.blockof") + " %1$s)", networkHeight);
            }
        }

        //Assemble via 3 parts of TextFlow
        //Connected_Synced to 127.0.0.1:10501 (Syncing, at block 2 of 9999)
        //TODO --> Could dynamically assemble these controls, but pattern is fixed enough to be static for now
        //(1) - easy
        this.lblConnected_1.setText(strConnected);

        //(2) - block link
        if (strConnectedBlock.length() == 0)
        {
            this.lblConnectedLink.setVisible(false);
        }
        else
        {
            this.lblConnectedLink.setText(strConnectedBlock);
            this.lblConnectedLink.setVisible(true);
        }

        //(3) - closing text
        if (strConnected2.length() == 0)
        {
            this.lblConnected2.setVisible(false);
        }
        else
        {
            this.lblConnected2.setText(strConnected2);
            this.lblConnected2.setVisible(true);
        }

        //if still syncing, then don't show balance in footer - it may be misleading
        if (connectionState == ConnectionState.Connected_Synced && isSufficientNCVersion)
        {
            //good!
        }
        else
        {
            //hide balance
            updateBalance(null);
            setWalletIconState(WalletLockState.UNKNOWN, true);
        }
    }

    private String getSyncMessage() {
        SyncResult result = _syncEstimator.getConvergeEstimate();
        String s = null;
        if (result.Status == SyncStatusEnum.SUCCESS_ESTIMATE) {
            s = String.format(
                    _localeModule.getString("footer.connect.syncing.estimate.success"),
                    Math.max(result.MinutesToSync, 1), //round to at least 1 minute
                    result.SyncFinalHeight.NetworkHeight, result.LocalSyncRateBlocksPerSecond, result.getSecondDuration()
                    );
        }
        else
        {
            //No message if error, this is optional help
            s = _localeModule.getString("footer.connect.syncing.estimate.notyet");
        }

        return s;
    }

    public void clickBlockLink() {
        //https://testnet.explore.veriblock.org/block/130684
        String urlRoot = _appContext.Configuration.getExplorerUrl();
        String strBlock = this.lblConnectedLink.getText();
        String url = String.format("%1$s/block/%2$s", urlRoot, strBlock);

        if (strBlock.length() == 0) {
            return;
        }
        Utils.openLink(url);
    }

    //endregion

    public void clickMyBalance() {
        //Navigate to MyBalance page
        if (!_isBalanceLinkEnabled)
        {
            return;
        }
        _appContext.UIManager.navigateToTab(TabEnum.MyAddresses);
    }

    public Hyperlink lnkBalance;

    private boolean _isBalanceLinkEnabled = false;
    public void updateBalance(Long amount) {


        if (amount != null) {
            //great!
            String sAmount = VbkUtils.convertAtomicToVbkString(amount)
                    + " " + _appContext.Configuration.getVbkUnit();
            this.lnkBalance.setText(sAmount);
            this.lnkBalance.setDisable(false);
            _isBalanceLinkEnabled = true;
        } else {
            //Null = error
            //just don't display (and disable link)
            this.lnkBalance.setText("...");
            this.lnkBalance.setDisable(true);
            _isBalanceLinkEnabled= false;
        }
    }

}

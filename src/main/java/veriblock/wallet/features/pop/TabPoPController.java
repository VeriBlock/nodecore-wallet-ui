// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.pop;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.pop.*;
import veriblock.wallet.core.pop.entities.*;
import veriblock.wallet.core.pop.entities.OperationSummaryEntity;
import veriblock.wallet.entities.PoPEndorsementInfoEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.features.shell.ShellService;
import veriblock.wallet.features.wallet.WalletService;
import veriblock.wallet.uicommon.BackgroundTask;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.DialogGetValueInput;
import veriblock.wallet.uicommon.IntegrationLinks;

import java.util.ArrayList;
import java.util.List;

import static veriblock.wallet.core.pop.ConfigConstants.Key.BITCOIN_FEE_MAX;
import static veriblock.wallet.core.pop.ConfigConstants.Key.BITCOIN_FEE_PERKB;

public class TabPoPController extends BaseController {

    private static final Logger _logger = LoggerFactory.getLogger(TabPoPController.class);
    private static final Integer BACKGROUND_LOOP_SECONDS = 5;
    private static final Integer BACKGROUND_LOOP_SECONDS_REWARDS = 60;

    public void initialize() {
    }

    private LocaleModule _localeModule;

    public void init() {
        //this.btnTest.setVisible(false);
        //Have different background threads with different frequencies, so don't show the update stamp
        //this.lblUpdateStatus.setVisible(false);

        setLocale();

        //connect to http
        _clientProxy = PopService.getApiProxy();

        setupModel();
        setupUI();

        _appContext.UIManager.setStatusMessage(_localeModule.getString("TabPoP_status_checking"));
        updateUIForConnectedStatus(false);  //default assume not connected, until we establish a connection
        this.btnConnect.setVisible(false);  //don't enable this till system had a chance to see if PoP service is up, else user could prematurely-click

        _backgroundTask = new BackgroundTask();
        _backgroundTask.start(
                BACKGROUND_LOOP_SECONDS,
                () -> {
                    updateModelFromServiceLoop();
                    return null;
                },
                () -> {
                    applyModelToUI_enforceOneInstance();
                    return null;
                }
        );

        _backgroundRewardTask = new BackgroundTask();
        _backgroundRewardTask.start(
                BACKGROUND_LOOP_SECONDS_REWARDS,
                () -> {
                    updateModelFromServiceLoop_Reward();
                    return null;
                },
                () -> {
                    //Just update the model. Main thread can apply to UI
                    return null;
                }
        );

    }

    public void dispose() {
        _backgroundTask.dispose();
        _backgroundRewardTask.dispose();
    }

    private BackgroundTask _backgroundTask;
    private BackgroundTask _backgroundRewardTask;
    private ApiProxy _clientProxy;
    private TabPoPModel _popModel;
    public Label lblSubHeaderSettings;
    public Label lblBtcBalance;
    public Label lblVbkAddress;
    public Label lblSpecificBlock;
    public Label lblSubHeaderCurrentOps;
    public Label lblSubHeaderRewards;

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabPoP);

        this._appContext.UIManager.setTitle(_localeModule.getString("main.title"));

        this.btnConnect.setText(_localeModule.getString("TabPoP_button_connect"));

        this.lblSubHeaderSettings.setText(_localeModule.getString("TabPoP_subheader_Settings"));
        this.lblBtcBalance.setText(_localeModule.getString("TabPoP_label_btcBalance"));
        this.lblVbkAddress.setText(_localeModule.getString("TabPoP_label_vbkAddress"));
        this.btnAddMoreBtc.setText(_localeModule.getString("TabPoP_button_AddBtc"));
        this.btnMine.setText(_localeModule.getString("TabPoP_button_mine"));
        this.lblSpecificBlock.setText(_localeModule.getString("TabPoP_label_specificBlock"));
        this.btnAutoMineSetup.setText(_localeModule.getString("TabPoP_button_automine"));
        this.lblSubHeaderCurrentOps.setText(_localeModule.getString("TabPoP_subheader_currentOps"));
        this.lblSubHeaderRewards.setText(_localeModule.getString("TabPoP_subheader_rewards"));

        ControlBuilder.setupExportButton(this.btnExportOps);
        ControlBuilder.setupExportButton(this.btnExportReward);

        //Grid:Ops
        this.colId.setText(_localeModule.getString("TabPoP_gridOps_colId"));
        this.colState.setText(_localeModule.getString("TabPoP_gridOps_colState"));
        this.colAction.setText(_localeModule.getString("TabPoP_gridOps_colAction"));
        this.colEndorsedBlock.setText(_localeModule.getString("TabPoP_gridOps_colEndorsedBlock"));
        this.colRound.setText(_localeModule.getString("TabPoP_gridOps_colRound"));
        this.colRewardBlock.setText(_localeModule.getString("TabPoP_gridOps_colRewardBlock"));
        this.colMessage.setText(_localeModule.getString("TabPoP_gridOps_colMessage"));

        //Grid: Reward
        this.colRewardEndorsedBlock.setText(_localeModule.getString("TabPoP_gridRewards_colRewardEndorsedBlock"));
        this.colRewardRound.setText(_localeModule.getString("TabPoP_gridRewards_colRewardRound"));
        this.colRewardProjected.setText(_localeModule.getString("TabPoP_gridRewards_colRewardProjected"));
        this.colRewardPaidInBlock.setText(_localeModule.getString("TabPoP_gridRewards_colRewardPaidInBlock"));
        this.colRewardVbkTxId.setText(_localeModule.getString("TabPoP_gridRewards_colRewardVbkTxId"));
        this.colRewardBtcTxId.setText(_localeModule.getString("TabPoP_gridRewards_colRewardBtcTxId"));

    }

    public Label lblUpdateStatus;
    public Button btnTest;
    public Button btnConnect;
    public Button btnMine;
    public Button btnAddMoreBtc;
    public Button btnSpecificBlockClear;
    public Label lblAutoMineRounds;

    public TableView<OperationRow> mainGrid;
    public TableColumn<OperationRow, String> colId;
    public TableColumn<OperationRow, String> colState;
    public TableColumn<OperationRow, String> colAction;
    public TableColumn<OperationRow, Integer> colEndorsedBlock;
    public TableColumn<OperationRow, Integer> colRound;
    public TableColumn<OperationRow, String> colRewardBlock;
    public TableColumn<OperationRow, String> colMessage;

    public TableView<RewardRow> rewardGrid;
    public TableColumn<RewardRow, Integer> colRewardEndorsedBlock;
    public TableColumn<RewardRow, Integer> colRewardRound;
    public TableColumn<RewardRow, String> colRewardProjected;
    public TableColumn<RewardRow, Integer> colRewardPaidInBlock;
    public TableColumn<RewardRow, String> colRewardVbkTxId;
    public TableColumn<RewardRow, String> colRewardBtcTxId;
    public Label lblRewardSummary;

    public Hyperlink hlnkBtcFeeKB;
    public Hyperlink hlnkBtcMax;

    private void setupModel()
    {
        _popModel = new TabPoPModel();
        _popModel.showInitialMessage = true;

        _popModel.currentBlock = _appContext.getCurrentBlockHeight();
    }

    private void setupUI()
    {
        //set UI binding
        ControlHelper.setTextFieldProperties(txtSpecificBlock, ControlHelper.MaskType.DigitsOnly, 1000000);
        ControlHelper.setToolTip(btnSpecificBlockClear, _localeModule.getString("TabPoP_specificBlock_tooltip"));
        ControlHelper.setPromptText(txtSpecificBlock, _localeModule.getString("TabPoP_specificBlock_prompt"));

        ControlHelper.setLabelCopyProperties(lnkBtcAmount);

        this.lnkBtcAmount.setText("...");
        this.lnkVbkAddress.setText("...");

        ControlHelper.setToolTip(this.lnkVbkAddress, _localeModule.getString("TabPoP_vbkAddress_tooltip"));

        ControlHelper.setToolTip(this.btnAddMoreBtc, String.format(_localeModule.getString("TabPoP_button_AddBtc_tooltip")));

        ControlHelper.setTableViewNoResultsFoundMessage(this.mainGrid, _localeModule.getString("TabPoP_grid_checkingForData"));
        ControlHelper.setTableViewNoResultsFoundMessage(this.rewardGrid, _localeModule.getString("TabPoP_grid_checkingForData"));

        setupGrid();

        lblRewardSummary.setText("");
    }

    private void setupGrid() {
        //GRID: OPERATIONS
        mainGrid.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ControlHelper.installCopyPasteHandler(this.mainGrid);
        mainGrid.setEditable(false);

        //Bind property to column!
        //NOTE --> need properties of the form getAddressMine(), follow standard Java convention
        colId.setCellValueFactory(new PropertyValueFactory<>("operationId"));
        colState.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("actionMessage"));
        colEndorsedBlock.setCellValueFactory(new PropertyValueFactory<>("endorsedBlockNumber"));
        colRound.setCellValueFactory(new PropertyValueFactory<>("roundNumber"));
        colRewardBlock.setCellValueFactory(new PropertyValueFactory<>("rewardBlockMessage"));
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));

        GenericFunction<Void, OperationRow> testFunc = (row) -> {
            clickOpIdLink(row);
            return null;
        };
        ControlHelper.setGridColumnHyperlink(colId, testFunc);

        _tableList = FXCollections.observableArrayList();
        mainGrid.setItems(_tableList);


        //GRID: VIEW RECENT REWARDS
        rewardGrid.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ControlHelper.installCopyPasteHandler(this.rewardGrid);
        rewardGrid.setEditable(false);

        //Bind property to column!
        //NOTE --> need properties of the form getAddressMine(), follow standard Java convention
        colRewardEndorsedBlock.setCellValueFactory(new PropertyValueFactory<>("endorsedBlock"));
        colRewardRound.setCellValueFactory(new PropertyValueFactory<>("roundNumber"));
        colRewardProjected.setCellValueFactory(new PropertyValueFactory<>("projectedRewardString"));
        colRewardPaidInBlock.setCellValueFactory(new PropertyValueFactory<>("rewardBlockMessage"));
        colRewardVbkTxId.setCellValueFactory(new PropertyValueFactory<>("vbkTxId"));
        colRewardBtcTxId.setCellValueFactory(new PropertyValueFactory<>("btcTxId"));

        ControlHelper.setGridColumnHyperlink(colRewardVbkTxId, (row) -> {
            clickRewardLinkVbk((RewardRow)row);
            return null;
        });
        ControlHelper.setGridColumnHyperlink(colRewardBtcTxId, (row) -> {
            clickRewardLinkBtc((RewardRow)row);
            return null;
        });

        _tableListReward = FXCollections.observableArrayList();
        rewardGrid.setItems(_tableListReward);

    }

    private boolean autoMineEnsureUnlockedWallet()
    {
        String messageWalletUnlocked = _localeModule.getString("AutoMine_dialog_unlock_prompt");
        boolean successfullyUnlocked = WalletService.promptUnlockWalletLongTerm(_appContext, messageWalletUnlocked);
        if (!successfullyUnlocked)
        {
            LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
            _appContext.UIManager.setStatusMessageWarning(lm.getString("walletService_passwordCancelled"));
            return successfullyUnlocked;
        }

        return successfullyUnlocked;
    }

    public void clickAutoMineSetup()
    {
        //ensure wallet is unlocked
        boolean successfullyUnlocked = autoMineEnsureUnlockedWallet();
        if (!successfullyUnlocked)
        {
            return;
        }


        //call dialog!
        AutoMineModel autoMineModel = (AutoMineModel)ControlHelper.showCustomDialog("pop/AutoMine",
                _appContext, _localeModule.getString("TabPoP_autoMineSetup_dialog"), null);
        if (autoMineModel == null)
        {
            String message = String.format(_localeModule.getString("TabPoP_autoMineSetup_noChange"));
            this._appContext.UIManager.setStatusMessage(message);
        }
        else
        {
            //update UI, message
            AutoMineConfigEntity testConfig = new AutoMineConfigEntity();
            testConfig.round1 = autoMineModel.shouldMineRound1;
            testConfig.round2 = autoMineModel.shouldMineRound2;
            testConfig.round3 = autoMineModel.shouldMineRound3;
            testConfig.round4 = autoMineModel.shouldMineRound4;

            String roundList = updateAutoMineUI(testConfig);

            String message = null;
            if (roundList == null)
            {
                message = String.format(_localeModule.getString("TabPoP_autoMineSetup_turnedOff"));
            }
            else
            {
                message = String.format(_localeModule.getString("TabPoP_autoMineSetup_setup"), roundList);
            }

            this._appContext.UIManager.setStatusMessageSucess(message);
        }
    }

    private void clickOpIdLink(OperationRow row)
    {
        //open up
        if (row == null)
        {
            return;
        }

        OperationSummaryEntity input = new OperationSummaryEntity();
        input.operationId = row.operationId;
        input.action = row.actionMessage;

        //String opId = row.operationId;
        NavigationData navigationData = new NavigationData();
        navigationData.setData(input);
        ControlHelper.showCustomDialog("pop/OperationDetail",
                _appContext, _localeModule.getString("TabPoP_operationDetails_Dialog"), navigationData);

    }

    private void clickRewardLinkVbk(RewardRow row)
    {
        if (row == null)
        {
            return;
        }

        IntegrationLinks link = IntegrationLinks.getInstance();
        Utils.openLink(link.getVbkTransactionUrl(row.vbkTxId));
    }

    private void clickRewardLinkBtc(RewardRow row)
    {
        if (row == null)
        {
            return;
        }

        IntegrationLinks link = IntegrationLinks.getInstance();
        Utils.openLink(link.getBtcTransactionUrl(row.btcTxId));
    }

    ObservableList<RewardRow> _tableListReward;

    //region Operation Grid logic

    ObservableList<OperationRow> _tableList;

    //returns any new entities in grid that weren't passed in
    private List<OperationSummaryEntity> refreshGridOperations(List<OperationSummaryEntity> entities) {
        entities = applyFilter(entities, _popModel.currentBlock);
        if (entities != null && entities.size() == 0) {
            //no longer just checking for data
            ControlHelper.setTableViewNoResultsFoundMessage(this.mainGrid, _localeModule.getString("TabPoP_grid_nothingFound"));
        }

        //Merge in | indeed have listoperations return the official data and refresh it
        //Makes the grid much more responsive, especially as we auto-update every N seconds

        List<OperationSummaryEntity> newEntities = new ArrayList<>();
        //STEP 1: Check Entities
        for (int i = 0; i < entities.size(); i++) {
            OperationSummaryEntity entity = entities.get(i);

            //Does entity exist in grid?
            //  Yes -->
            //  No -->

            //Does anything from grid no longer exist in entites
            //  Yes --> remove it

            OperationRow row = getOperationRow(entity.operationId);
            if (row == null) {
                //entity does not yet exist in grid --> add it
                row = new OperationRow(entity);
                _tableList.add(row);

                //also update the status message. These new entities could come from auto-mine
                if (!_popModel.isInitialSetup) {
                    newEntities.add(entity);
                }

                continue;
            } else {
                //entity does exist --> update it!
                row.updateFromEntity(entity);
            }
        }

        //STEP 2: Check Grid
        List<OperationRow> itemsToRemove = new ArrayList<>();
        for (OperationRow row : _tableList) {
            OperationSummaryEntity entity = getOperationSummaryResponse(row.getOperationId(), entities);
            if (entity == null) {
                //Exists in tableview, does NOT exist in entities, therefore remove from tableview
                itemsToRemove.add(row);
            } else {
                //Exists in both table and entites - this would already be handled above.
            }
        }
        for (OperationRow row : itemsToRemove) {
            _tableList.remove(row);
        }

        //Force UI to update
        this.mainGrid.refresh();

        return newEntities;
    }

    private OperationRow getOperationRow(String operationId)
    {
        if (_tableList == null || operationId == null)
        {
            return null;
        }

        for(OperationRow row : _tableList)
        {
            if (row.getOperationId() != null && row.getOperationId().equals(operationId))
            {
                //found it!
                return row;
            }
        }

        return null;
    }

    private OperationSummaryEntity getOperationSummaryResponse(String operationId, List<OperationSummaryEntity> entities)
    {
        if (entities == null || operationId == null)
        {
            return null;
        }

        for(OperationSummaryEntity row : entities)
        {
            if (row.operationId != null && row.operationId.equals(operationId))
            {
                //found it!
                return row;
            }
        }

        return null;
    }

    private List<OperationSummaryEntity> applyFilter(List<OperationSummaryEntity> entities, int iCurrentBlock)
    {
        if (entities == null || entities.size() == 0)
        {
            return entities;
        }

        int iThresholdToKeepFailedBlock = 40;  //small threshold, as these have zero future to the PoP user

        //if for whatever reason current block is unknown, then default to zero and thus don't use it as a filter
        if (iCurrentBlock <= 0)
        {
            iCurrentBlock = 0;
        }

        //purge all blocks older than N with status = FAILED
        int rewardInXBlocks = PopService.rewardPaidInXBlocks();
        List<OperationSummaryEntity> entities2 = new ArrayList<>();
        for (OperationSummaryEntity op : entities)
        {
            //RULE: purge old failed blocks, as they have no future
            if (op.state.equalsIgnoreCase(PoPOperationState.FAILED.name()) &&
                    op.endorsedBlockNumber + iThresholdToKeepFailedBlock < iCurrentBlock)
            {
                //BAD, don't add it
                continue;
            }

            //RULE: Purge anything over 500 blocks ago
            if (iCurrentBlock > 0) {
                if (op.endorsedBlockNumber + rewardInXBlocks < iCurrentBlock)
                {
                    //BAD, already paid out
                    continue;
                }
            }

            //Passed all filters, so add it
            entities2.add(op);
        }
        return entities2;
    }

    //endregion

    private void refreshGridRewards(List<PoPEndorsementInfoEntity> rows)
    {
        if (rows != null && rows.size() == 0)
        {
            //no longer just checking for data
            ControlHelper.setTableViewNoResultsFoundMessage(this.rewardGrid, _localeModule.getString("TabPoP_grid_nothingFound"));
        }

        _tableListReward.clear();

        for (int i = 0; i < rows.size(); i++) {
            PoPEndorsementInfoEntity e = rows.get(i);
            RewardRow et = new RewardRow();
            et.setEndorsedBlock(e.endorsedBlockNumber);
            et.setProjectedReward(e.reward);
            et.setPaidInBlock(e.endorsedBlockNumber + PopService.rewardPaidInXBlocks());
            et.setBtcTxId(e.bitcoinTransactionId);
            et.setVbkTxId(e.veriBlockTransactionId);
            _tableListReward.add(et);
        }

        //Force UI to update
        this.rewardGrid.refresh();

        //also update label
        updateRewardSummaryLabel(rows);
    }

    private void updateRewardSummaryLabel(List<PoPEndorsementInfoEntity> rows)
    {
        long rewardSum = 0;
        for (PoPEndorsementInfoEntity info : rows)
        {
            rewardSum = rewardSum + info.reward;
        }

        String s = String.format(_localeModule.getString("TabPoP_label_rewardSummary"),
                VbkUtils.convertAtomicToVbkString(rewardSum),
                _appContext.Configuration.getVbkUnit(),
                rows.size(),
                PopService.getRecentRewardsSearchLength());

        this.lblRewardSummary.setText(s);

    }

    public void clickViewVbkAddress() {

        String vbkAddress = _popModel.getMinerProperties().minerAddress;

        //https://testnet.explore.veriblock.org/address/V6K4rSEzyJCbcHVvGkj2D7zHHC8888
        String url = _appContext.Configuration.getExplorerUrl();
        String fullUrl = String.format("%1$s/address/%2$s", url, vbkAddress);
        Utils.openLink(fullUrl);
    }

    public void clickTest() {

        //doTest();
/*
        String opId = "942bd892";
        NavigationData navigationData = new NavigationData();
        navigationData.setData(opId);
        ControlHelper.showCustomDialog("pop/OperationDetail",
                _appContext, "Operation Details", navigationData);
                */

    }

    //region Update Config Values

    public void clickUpdateBtcMax() {
        SoundItem.playButtonClick();
        String fieldLabel = _localeModule.getString("TabPoP_clickUpdateBtcMax");
        GenericFunction<ValidationInfo, String> testFunc = getValidationFunction(fieldLabel);
        updateConfig(fieldLabel, BITCOIN_FEE_MAX, testFunc);
     }

    public void clickUpdateBtcFeeKB() {
        SoundItem.playButtonClick();
        String fieldLabel = _localeModule.getString("TabPoP_clickUpdateBtcFeeKB");
        GenericFunction<ValidationInfo, String> testFunc = getValidationFunction(fieldLabel);
        updateConfig(fieldLabel, BITCOIN_FEE_PERKB, testFunc);
    }

    //TODO --> differentiate this further between fee and max (200x different)
    private GenericFunction<ValidationInfo, String> getValidationFunction(String fieldLabel)
    {
        GenericFunction<ValidationInfo, String> testFunc = (strInput) -> {
            ValidationInfo vi = new ValidationInfo();
            long result = Long.valueOf(strInput);
            if (result > 100000)
            {
                vi.setMessageError(String.format(_localeModule.getString("TabPoP_updateConfig_error"), fieldLabel,
                        result));
            }
            else if (result > 50000)
            {
                vi.setMessageWarning(String.format(_localeModule.getString("TabPoP_updateConfig_warning"), fieldLabel,
                        result));
            }

            return vi;
        };

        return testFunc;
    }

    //return true if updated
    public void updateConfig(String fieldLabel, ConfigConstants.Key configKey, GenericFunction<ValidationInfo, String> testFunc ) {

        _logger.info("updateConfig: {}", configKey.toString());

        long initValue = 0;
        switch (configKey) {
            case BITCOIN_FEE_MAX:
                initValue = _popModel.getBtcFeeConfigEntity().maxFee;
                break;
            case BITCOIN_FEE_PERKB:
                initValue = _popModel.getBtcFeeConfigEntity().feePerKB;
                break;
        }

        DialogGetValueInput dialogInput = new DialogGetValueInput();
        dialogInput.valueEmptyPrompt = VbkUtils.getEmptyVbkString();
        dialogInput.message = String.format(_localeModule.getString("TabPoP_updateConfig_header"), fieldLabel);
        dialogInput.initialValue = String.valueOf(initValue);
        dialogInput.dataMaskType = ControlHelper.MaskType.VbkAmountPositive;
        dialogInput.validationFunc = testFunc;

        String strResult = ControlBuilder.showGetValueDialog(_appContext, dialogInput);
        if (strResult == null || strResult.length() == 0)
        {
            //cancelled
            _appContext.UIManager.setStatusMessage(String.format(_localeModule.getString("TabPoP_updateConfig_cancelled"),
                    fieldLabel));
            return;
        }

        //Got value from UI
        long newValue = Long.parseLong(strResult);

        final BtcFeeConfigEntity btcFeeConfigEntity = new BtcFeeConfigEntity();
        if (configKey == BITCOIN_FEE_MAX) {
            btcFeeConfigEntity.maxFee = newValue;
            btcFeeConfigEntity.feePerKB = _popModel.getBtcFeeConfigEntity().feePerKB;
        }
        if (configKey == BITCOIN_FEE_PERKB) {
            btcFeeConfigEntity.maxFee = _popModel.getBtcFeeConfigEntity().maxFee;
            btcFeeConfigEntity.feePerKB = newValue;
        }
        _clientProxy.setBtcFee(btcFeeConfigEntity);

        switch (configKey) {
            case BITCOIN_FEE_MAX:
                _popModel.getBtcFeeConfigEntity().maxFee = newValue;
                break;
            case BITCOIN_FEE_PERKB:
                _popModel.getBtcFeeConfigEntity().feePerKB = newValue;
                break;
        }

        applyModelToUI_Config();

        _appContext.UIManager.setStatusMessageSucess(
                String.format(_localeModule.getString("TabPoP_updateConfig_success"), fieldLabel, strResult));
    }

    //endregion

    public void clickAddMoreBtc() {
        SoundItem.playButtonClick();

        if (_popModel == null || _popModel.getMinerProperties() == null)
        {
            _appContext.UIManager.setStatusMessageWarning(
                    String.format(_localeModule.getString("TabPoP_button_AddBtc_notReady")));
            return;
        }
        else
        {
            String promptMessage = _localeModule.getString("TabPoP_button_AddBtc_dialog");
            Object result = ControlHelper.showCustomDialog("pop/AddBtc", _appContext, promptMessage);
        }
    }

    public void clickMine() {
        //do sound after success
        _logger.info("clicked MineVbk");

        boolean successDidMine = mineVbk();

        //enforce can only mine every ~5 seconds
        if (successDidMine)
        {
            _popModel.lastManualMineTimeStamp = Utils.getEpochCurrent();
            this.btnMine.setDisable(true);
            ControlHelper.setToolTip(btnMine,
                    String.format(_localeModule.getString("TabPoP_mine_waitNSeconds"),  _popModel.secondsToWaitBetweenMining));
        }
    }

    public TextField txtSpecificBlock;
    public void clickClearMineBlock()
    {
        this.txtSpecificBlock.setText("");
    }

    public void clickConnect()
    {
        SoundItem.playButtonClick();
        //trigger popup and apply its effects. Easiest way is to re-apply model
        _logger.info("click connect button");
        connectWithDialog();
    }

    private int getBlockToMine()
    {
        return ControlHelper.getInteger(txtSpecificBlock, -1);
    }

    private ValidationInfo validateBlockToMine(int blockToMine)
    {
        //Must be within 350 blocks
        int currentBlock = _popModel.currentBlock;
        int allowedThrehold = 350;

        ValidationInfo vi = new ValidationInfo();
        if (blockToMine + allowedThrehold < currentBlock)
        {
            vi.setMessageError(String.format(_localeModule.getString("TabPoP_mineBlock_validation1"),
                    blockToMine,
                    currentBlock - blockToMine,
                    allowedThrehold));
        }

        return vi;
    }

    private boolean mineVbk() {
        boolean successDidMine = false;
        //Cleaned model --> ensure that NC unlocked prior to each mine
        boolean isWalletLocked = NodeCoreGateway.getInstance().isWalletLocked();
        if (isWalletLocked)
        {
            boolean unlockedWalletSuccess = ensureWalletUnlocked();
            if (!unlockedWalletSuccess)
            {
                return false;
            }
        }

        int blockToMine = getBlockToMine();
        if (blockToMine > 0) {
            _logger.info("Start PoP Mined block: {}", blockToMine);
        }
        else
        {
            _logger.info("Start PoP Mined block");
        }


        //Validation
        ValidationInfo viBlockToMine = validateBlockToMine(blockToMine);
        if (blockToMine > 0 && viBlockToMine.isWarningOrError())
        {
            _appContext.UIManager.setStatusMessage(viBlockToMine);
            return successDidMine;
        }

        MineResultEntity result = _clientProxy.mine(blockToMine);

        if (result.failed)
        {
            String errorMessage= "";
            //1st try
            if (result.messages.size() > 0) {
                errorMessage = result.messages.get(0).details.get(0);
            }
            //2nd try:
            if (errorMessage == null || errorMessage.length() == 0)
            {
                errorMessage = result.messages.get(0).message;
            }
            //3rd try
            if (errorMessage == null || errorMessage.length() == 0)
            {
                errorMessage = _localeModule.getString("TabPoP_mine_error");
            }
            _appContext.UIManager.setStatusMessageError(String.format(_localeModule.getString("TabPoP_mine_didNotWork"),
                    result.operationId, errorMessage));
            _logger.info("Done PoP Mined block - failed: {}", errorMessage);

            successDidMine = false;
        }
        else
        {
            //YAY!! It worked
            _appContext.UIManager.setStatusMessageSucess(String.format(_localeModule.getString("TabPoP_mine_Success"),
                    result.operationId));
            _logger.info("Done PoP Mined block - success. OperationId {}.", result.operationId);

            //update grid
            addOperationToGrid(result);

            SoundItem.playPoP();

            successDidMine = true;
        }

        return successDidMine;
    }

    //refresh grid

    private void addOperationToGrid(MineResultEntity result)
    {
        OperationRow et = new OperationRow();
        et.setOperationId(result.operationId);

        et.setMessage(_localeModule.getString("TabPoP_mine_submitted"));

        //Merely adding to the list, it's already bound
        //put at first position so it shows at the top:
        _tableList.add(0, et);
    }

    private boolean ensureWalletUnlocked()
    {
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();
        boolean setPassword = WalletService.promptUnlockWallet(_appContext, ngw, true);
        if (setPassword)
        {
            //Unlock
            boolean successUnlock = ngw.unlockWallet();
            if (successUnlock) {
                _appContext.UIManager.setStatusMessageSucess(_localeModule.getString("TabPoP_unlockwallet_success"));
                return true;
            }
            else
            {
                _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("TabPoP_unlockwallet_error"));
                return false;
            }
        }
        else {
            _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("TabPoP_unlockwallet_cancelled"));
            return false;
        }
    }

    //region Background loop

    private void updateModelFromServiceLoop()
    {
        try {
            AutoMineConfigEntity autoMineConfigEntity = _clientProxy.getAutoMineConfig();
            BtcFeeConfigEntity btcFeeConfigEntity = _clientProxy.getBtcFeeConfig();
            if (autoMineConfigEntity != null && btcFeeConfigEntity != null)
            {
                _popModel.isConnected = true;
                //If config isn't ready, no point to check the others
                _popModel.setBtcFeeConfigEntity(btcFeeConfigEntity);
                _popModel.setAutoMineConfig(autoMineConfigEntity);
                _popModel.setPopOperationEntities(_clientProxy.getOperations());

                //MinerProperties is special - very first hit when PoP first starts will have seeds
                _popModel.setMinerProperties(_clientProxy.getMinerPropertiesEntity());
            }
            else
            {
                _popModel.isConnected = false;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    //Separata cycle as VWR is a heavy hit, and don't want to bog down NodeCore, and it changes more slowly
    private void updateModelFromServiceLoop_Reward()
    {
        try {

            _logger.info("Check viewrecentrewards");

            if (!_popModel.isConnected ) {
                //wait 2 seconds for timing rather than a full 30 seconds for loop
                Utils.waitNSeconds(2);
            }

            if (_popModel.isConnected ) {
                int searchLength = PopService.getRecentRewardsSearchLength();
                CommandResult<List<PoPEndorsementInfoEntity>> commandResults = NodeCoreGateway.getInstance()
                        .getPoPEndorsementInfo(searchLength);
                if (commandResults.isSuccess()) {
                    //yay!
                    List<PoPEndorsementInfoEntity> data = commandResults.getPayload();
                    _popModel.setViewRecentRewards(data);
                }
            }
            else
            {
                //Clear it
                List<PoPEndorsementInfoEntity> data = new ArrayList<>();
                _popModel.setViewRecentRewards(data);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    //endregion

    public void connectWithDialog()
    {
        if (_popModel.isConnected) {
            //already connected (button should have been disabled, but be defensive)
            return;
        }

        ValidationInfo vi = (ValidationInfo) ControlHelper.showCustomDialog("pop/ConnectPoP", _appContext,
                _localeModule.getString("TabPoP_connect_header"));

        if (vi.isWarningOrError())
        {
            if (vi.isWarning())
            {
                //assume WARNING == CANCEL
                //Don't display anything if cancelled --> could be different cases
                //  (1) user cancelled because already running (ok)
                //  (2) user cancelled and not running
                _appContext.UIManager.clearStatusMessage();
                //_appContext.UIManager.setStatusMessageWarning(_localeModule.getString("TabPoP_connect_clickButton"));
                //_popModel.statusHasPleaseConnectConnectWarning = true;
            }
            else if (vi.isError())
            {
                _appContext.UIManager.setStatusMessageError(String.format(_localeModule.getString("TabPoP_connect_error"), vi.getMessage()));
            }

            _popModel.isConnected = false;
            updateUIForConnectedStatus(false);
            return;
        }
        else
        {
            //If success --> repopulate data and continue!
            _popModel.isConnected = true;
            updateModelFromServiceLoop();
            updateModelFromServiceLoop_Reward();
            _appContext.UIManager.setStatusMessageSucess(_localeModule.getString("TabPoP_connect_success"));
        }

        applyModelToUI_enforceOneInstance();
    }

    //TODO --> extract to pattern
    private boolean _isMethodRunning = false;
    private void applyModelToUI_enforceOneInstance()
    {
        _logger.info("PoP applyModelToUI_enforceOneInstance - start");
        if (_isMethodRunning) {
            _logger.info("PoP applyModelToUI_enforceOneInstance - already running - exit");
            return;
        }
        _isMethodRunning = true;

        try {
            applyModelToUI();
        }
        catch (Exception ex)
        {

        }
        _isMethodRunning = false;
        _logger.info("PoP applyModelToUI_enforceOneInstance - done");
    }

    //Could run every 10 seconds
    private void applyModelToUI()
    {
        //Update timestamp
        //this.lblUpdateStatus.setText(String.format("Updated %1$s", Utils.getTimeOfDayNow()));
        if (!_popModel.isConnected) {

            //only on initial setup, just once prompt the popup
            if (_popModel.isInitialSetup && !_popModel.didAlreadyPromptConnectPopup)
            {
                connectWithDialog();
                _popModel.didAlreadyPromptConnectPopup = true;
            }

            _appContext.UIManager.setStatusMessageWarning(_localeModule.getString("TabPoP_connect_clickButton"));
            //_popModel.statusHasPleaseConnectConnectWarning = true;

            updateUIForConnectedStatus(false);
            return;
        }

        //Have a connection!

        //Check latest version
        if (!_popModel.getIsSufficientPoPVersion())
        {
            String popMinimumVersion = PopService.getMinimumPopVersion();
            _appContext.UIManager.setStatusMessageWarning(
                    String.format(_localeModule.getString("TabPoP_connect_popVersion"), popMinimumVersion)
            );
            return;
        }

        /*
        if (_popModel.statusHasPleaseConnectConnectWarning )
        {
            _popModel.statusHasPleaseConnectConnectWarning = false;
            _appContext.UIManager.clearStatusMessage();
        }
        */

        if (_popModel.showInitialMessage)
        {
            _appContext.UIManager.setStatusMessageSucess(_localeModule.getString("TabPoP_connect_alreadyDone"));
            _popModel.showInitialMessage = false;
        }

        updateUIForConnectedStatus(true);

        AutoMineConfigEntity autoMineConfigEntity = _popModel.getAutoMineConfig();
        if (autoMineConfigEntity == null)
        {
            //Should not be hit
            return;
        }

        applyModelToUI_Config();

        //update MinerProperties
        MinerPropertiesEntity minerProperties = _popModel.getMinerProperties();
        if (minerProperties == null)
        {
            this.lnkBtcAmount.setText("...");
            this.lnkVbkAddress.setText("...");
        }
        else
        {
            //Have BTC amount
            this.lnkBtcAmount.setText(String.format("%1$s BTC",
                    VbkUtils.convertAtomicToVbkString(minerProperties.bitcoinBalance)));
            this.lnkVbkAddress.setText(minerProperties.minerAddress);
        }

        //Update automine
        updateAutoMineUI(autoMineConfigEntity);

        //Bind to grid
        List<OperationSummaryEntity> ops = _popModel.getPopOperationEntities();
        List<OperationSummaryEntity> newEntities = refreshGridOperations(ops);
        String strNewEntitiesStatus = createNewEntityStatusMessage( newEntities);

        //Bind to rewards grid
        List<PoPEndorsementInfoEntity> rewards = _popModel.getViewRecentRewards();
        refreshGridRewards(rewards);

        //Set status message. There is precedence here.
        if (minerProperties.bitcoinBalance <= 0)
        {
            //need more BTC!
            _appContext.UIManager.setStatusMessageWarning(String.format(_localeModule.getString("TabPoP_button_AddBtc_empty")));
        }
        if (strNewEntitiesStatus != null)
        {
            _appContext.UIManager.setStatusMessageSucess(String.format(strNewEntitiesStatus));

        }

        //finished setup, no longer initial
        this._popModel.isInitialSetup = false;
    }

    //Returns list of rounds
    private String updateAutoMineUI(AutoMineConfigEntity entity) {
        String message = "";
        String roundList = null;
        if (entity == null) {
            message = "";
        } else {
            List<Integer> aint = entity.getAutoMineRounds();
            if (aint.size() == 0) {
                message = _localeModule.getString("TabPoP_autoMineSetup_notSet");
            } else {
                String[] astr = new String[aint.size()];
                for (int i = 0; i < aint.size(); i++) {
                    int round = aint.get(i);
                    if (round == 4) {
                        astr[i] = Integer.toString(round) + " (" + _localeModule.getString("TabPoP_keystone") + ")";
                    } else {
                        astr[i] = Integer.toString(round);
                    }
                }
                roundList = String.join(", ", astr);
                message = String.format(_localeModule.getString("TabPoP_autoMineSetup_rounds"), roundList);
            }
        }

        this.lblAutoMineRounds.setText(message);
        return roundList;
    }

    private String createNewEntityStatusMessage(List<OperationSummaryEntity> newEntities)
    {
        if (newEntities == null || newEntities.size() == 0)
        {
            return  null;
        }

        String[] astr = new String[newEntities.size()];
        //aaaa (endorsed 1233), bbbbb (endorsed 4567),
        for (int i =0 ; i < astr.length; i++)
        {
            astr[i] = String.format(_localeModule.getString("TabPoP_mine_new_endorsed"),
                    newEntities.get(i).operationId,
                    newEntities.get(i).endorsedBlockNumber,
                    PopService.getRoundNumber(newEntities.get(i).endorsedBlockNumber)
            );
        }

        String strNewEntities = String.format(_localeModule.getString("TabPoP_mine_new"),
                String.join(",", astr));
        return strNewEntities;
    }

    private void shouldEnableMineButton(boolean isConnected) {
        if (!isConnected) {
            this.btnMine.setDisable(true);
            return;
        }

        //RULE: if btn amount = 0, then don't enable mine button
        if (_popModel == null || _popModel.getMinerProperties() == null
                || _popModel.getMinerProperties().bitcoinBalance == 0) {
            this.btnMine.setDisable(true);
            ControlHelper.setToolTip(btnMine,
                    String.format(_localeModule.getString("TabPoP_button_mine_valid_addbtc")));

            return;
        }

        //RULE: If we just mined N seconds ago, then don't enable it
        if (_popModel.lastManualMineTimeStamp > 0)
        {
            //we have already successfully mined, so now check if enough time has passed...
            long currentEpoch = Utils.getEpochCurrent();
            if (currentEpoch - _popModel.lastManualMineTimeStamp < _popModel.secondsToWaitBetweenMining)
            {
                this.btnMine.setDisable(true);
                ControlHelper.setToolTip(btnMine,
                        String.format(_localeModule.getString("TabPoP_button_mine_valid_waitN"),  _popModel.secondsToWaitBetweenMining));
                return;
            }
        }

        //Enable it!
        this.btnMine.setDisable(false);
        ControlHelper.setToolTip(btnMine,
                String.format(_localeModule.getString("TabPoP_button_mine_tooltip")));
    }

    private void applyModelToUI_Config() {
        final BtcFeeConfigEntity btcFeeConfigEntity = _popModel.getBtcFeeConfigEntity();
        this.hlnkBtcFeeKB.setText(String.valueOf(btcFeeConfigEntity.feePerKB));
        this.hlnkBtcMax.setText(String.valueOf(btcFeeConfigEntity.maxFee));
    }

    public TextField lnkBtcAmount;
    public Hyperlink lnkVbkAddress;
    public Button btnAutoMineSetup;

    private void updateUIForConnectedStatus(boolean isConnected)
    {
        //Enable
        this.btnConnect.setVisible(!isConnected);

        //Disable
        this.hlnkBtcFeeKB.setDisable(!isConnected);
        this.hlnkBtcMax.setDisable(!isConnected);

        //this.btnMine.setDisable(!isConnected);
        shouldEnableMineButton(isConnected);

        this.mainGrid.setDisable(!isConnected);
        this.rewardGrid.setDisable(!isConnected);

        this.btnAddMoreBtc.setDisable(!isConnected);

        this.btnAutoMineSetup.setDisable(!isConnected);
        this.btnSpecificBlockClear.setDisable(!isConnected);
        this.txtSpecificBlock.setDisable(!isConnected);
    }

    //Called by main program as part of loop
    public void onCurrentBlockChanged(int oldBlockHeight, int newBlockHeight) {

        //grid should autobind
        _popModel.currentBlock = newBlockHeight;
        this.mainGrid.refresh();    //force data binding update
        this.rewardGrid.refresh();
    }

    public class RewardRow implements Exportable {

        public String createCsvRow(boolean isHeader) {
            if (isHeader) {
                return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s",
                        "Endorsed Block",
                        "Round#",
                        "CurrentBlock",
                        "Projected Reward",
                        "Paid In Block",
                        "VBK Tx Id",
                        "BTC Tx Id"
                );
            } else {
                //return data here
                return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s",
                        this.endorsedBlock,
                        this.getRoundNumber(),
                        _popModel.currentBlock,
                        VbkUtils.convertAtomicToVbkString(this.getProjectedReward()),
                        this.getPaidInBlock(),
                        this.getVbkTxId(),
                        this.getBtcTxId()
                );
            }
        }

        public int getEndorsedBlock() {
            return endorsedBlock;
        }

        public void setEndorsedBlock(int endorsedBlock) {
            this.endorsedBlock = endorsedBlock;
        }

        public long getProjectedReward() {
            return projectedReward;
        }
        public String getProjectedRewardString() {
            //If not yet awarded, then add "(Projected)" to cell. I.e. "1.33749999 (Projected)"

            int currentBlock = 0;
            if (_popModel != null)
            {
                currentBlock = _popModel.currentBlock;
            }

            int futureRewardBlock = endorsedBlock + PopService.rewardPaidInXBlocks();

            String strAmount = VbkUtils.convertAtomicToVbkString(projectedReward);
            String resultString = "";
            if (currentBlock >= futureRewardBlock)
            {
                //Already done, therefore a known quantity
                resultString = strAmount;
            }
            else
            {
                //Future, therefore only a projection
                resultString = String.format(_localeModule.getString("TabPoP_gridRewards_colRewardProjected_cell2"), strAmount);
            }

            return resultString;
        }

        public void setProjectedReward(long projectedReward) {
            this.projectedReward = projectedReward;
        }

        public int getPaidInBlock() {
            return paidInBlock;
        }

        public void setPaidInBlock(int paidInBlock) {
            this.paidInBlock = paidInBlock;
        }

        public String getVbkTxId() {
            return vbkTxId;
        }

        public void setVbkTxId(String vbkTxId) {
            this.vbkTxId = vbkTxId;
        }

        private int endorsedBlock;
        private long projectedReward;
        private int paidInBlock;
        private String vbkTxId;

        public String getBtcTxId() {
            return btcTxId;
        }

        public void setBtcTxId(String btcTxId) {
            this.btcTxId = btcTxId;
        }

        private String btcTxId;

        public String getRewardBlockMessage() {

            int currentBlock = 0;
            if (_popModel != null)
            {
                currentBlock = _popModel.currentBlock;
            }
            return getColumnRewardMessage(PoPOperationState.INITIAL.name(),
                    this.endorsedBlock, currentBlock);
        }

        public Integer getRoundNumber() {
            return PopService.getRoundNumber(this.endorsedBlock);
        }
    }

    public class OperationRow implements Exportable
    {
        public OperationRow()
        {

        }

        public String createCsvRow(boolean isHeader) {
            if (isHeader) {
                return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s",
                        "Id",
                        "State",
                        "Action",
                        "Endorsed",
                        "Round#",
                        "Paid In Block",
                        "Message"
                );
            } else {
                //return data here
                return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s",
                        this.operationId,
                        this.status,
                        Utils.isNull(this.actionMessage),
                        this.endorsedBlockNumber,
                        this.getRoundNumber(),
                        this.getPaidInFutureBlock(),
                        Utils.isNull(this.message)
                );
            }
        }

        public OperationRow(OperationSummaryEntity e) {
            updateFromEntity(e);
        }

        public void updateFromEntity(OperationSummaryEntity e) {
            this.setOperationId(e.operationId);
            this.setStatus(e.state);
            this.setActionMessage(e.action);
            this.setEndorsedBlockNumber(e.endorsedBlockNumber);
        }

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getActionMessage() {
            return actionMessage;
        }

        public void setActionMessage(String actionMessage) {
            this.actionMessage = actionMessage;
        }

        public int getEndorsedBlockNumber() {
            return endorsedBlockNumber;
        }

        public void setEndorsedBlockNumber(int endorsedBlockNumber) {
            this.endorsedBlockNumber = endorsedBlockNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        private String operationId;
        private String status;
        private String actionMessage;
        private int endorsedBlockNumber;
        private String message;

        public int getPaidInFutureBlock()
        {
            return endorsedBlockNumber + PopService.rewardPaidInXBlocks();
        }

        public String getRewardBlockMessage() {

            int currentBlock = 0;
            if (_popModel != null)
            {
                currentBlock = _popModel.currentBlock;
            }
            return getColumnRewardMessage(this.status, this.endorsedBlockNumber, currentBlock);
        }

        public Integer getRoundNumber() {
            return PopService.getRoundNumber(this.endorsedBlockNumber);
        }
    }

    private String getColumnRewardMessage(String status, int endorsedBlockNumber, int currentBlock)
    {
        //Case: State == Failed
        if (status != null && status.equalsIgnoreCase(PoPOperationState.FAILED.name()))
        {
            //nothing to show
            return "";
        }

        //int iCurrentBlock = _popModel.currentBlock;
        int futureRewardBlock  =  endorsedBlockNumber + PopService.rewardPaidInXBlocks();

        if (endorsedBlockNumber == 0)
        {
            return "";
        }
        else if (currentBlock == futureRewardBlock)
        {
            //this is the exact block it's paid out
            return String.format(_localeModule.getString("TabPoP_column_reward1"),
                    futureRewardBlock);
        }
        else if (currentBlock > futureRewardBlock)
        {
            //PAST
            //already paid out!
            return String.format(_localeModule.getString("TabPoP_column_reward2"),
                    futureRewardBlock,
                    currentBlock - futureRewardBlock
            );
        }
        else if (currentBlock > 0 )
        {
            //FUTURE
            return String.format(_localeModule.getString("TabPoP_column_reward3"),
                    futureRewardBlock,
                    futureRewardBlock - currentBlock
            );
        }
        else
        {
            return String.format("%1$s", futureRewardBlock);
        }
    }

    //region Export

    public Button btnExportOps;
    public Button btnExportReward;

    public void clickExportOps()
    {
        List items = this.mainGrid.getItems();
        ValidationInfo vi = ShellService.doExport(items);
        _appContext.UIManager.setStatusMessage(vi);
    }

    public void clickExportReward()
    {
        List items = this.rewardGrid.getItems();
        ValidationInfo vi = ShellService.doExport(items);
        _appContext.UIManager.setStatusMessage(vi);
    }

    //endregion

}

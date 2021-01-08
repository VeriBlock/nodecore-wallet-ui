// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.layout.Region;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.entities.AddressBalanceEntity;
import veriblock.wallet.entities.StateInfoEntity;
import veriblock.wallet.entities.WalletTransactionEntity;
import veriblock.wallet.entities.WalletTransactionsEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.features.shell.ShellService;
import veriblock.wallet.uicommon.BackgroundTask;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.FormatHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.joda.time.DateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TabTransactionsController extends BaseController {

    private static final Logger _logger = LoggerFactory.getLogger(TabTransactionsController.class);
    public Button btnRefresh;

    public void init()
    {
        //Can setup model before any UI called
        setupModel();
        setLocale();

        setupAddressDropdown();
        setupPagingConfirmed();
        setupGridConfirmed();
        setupGridPending();
        setupPageSizeDropdown(getTabTransactionsModel().getResultsPerPage());

        startEventUpdateSelectedAddress(_tabTransactionsModel.getSelectedAddress());
        setUpColumnCellFactory();

        checkUIWalletCache();
    }

    public void dispose()
    {
        if (_backgroundTaskWalletCache != null) {
            _backgroundTaskWalletCache.dispose();
        }
    }

    private void checkUIWalletCache()
    {
        if ( NodeCoreGateway.getInstance().getGetStateInfo().getPayload().isWalletCacheBuilding()) {
            kickoffBackgroundWalletStatus();
        }
    }

    //region Model - setup and update

    public Button btnExport;

    private LocaleModule _localeModule;
    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabTransactions);

        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));

        lblSelectAddress.setText(_localeModule.getString("label.selectAddress"));
        lblPageSize.setText(_localeModule.getString("label.pageSize"));
        headerPending.setText(_localeModule.getString("label.headerPending"));
        headerConfirmed.setText(_localeModule.getString("label.headerConfirmed"));
        btnRefresh.setText(_localeModule.getString("button.refresh"));
        btnRebuildCache.setText(_localeModule.getString("button.rebuildCache"));

        ControlHelper.setToolTip(btnRefresh, _localeModule.getString("button.refresh.tooltip"));
        ControlBuilder.setupExportButton(btnExport);
    }

    private TabTransactionsModel _tabTransactionsModel;
    private TabTransactionsModel getTabTransactionsModel()
    {
        return _tabTransactionsModel;
    }
    private void setupModel()
    {
        _tabTransactionsModel = new TabTransactionsModel();
        _tabTransactionsModel.setPageNumber(1);

        int iResultsPerPage = UserSettings.getValueInt(SettingsConstants.TRANSACTION_PAGESIZECONFIRMED, 50);
        _tabTransactionsModel.setResultsPerPage(iResultsPerPage);

        //If wasn't changed, initially get the default address
        //Check selected address from user settings
        String address = null;
        String selectedAddress = UserSettings.getValue(SettingsConstants.TRANSACTION_SELECTEDADDRESS);
        if (selectedAddress == null)
        {
            //If not saved to settings, then use default:
            NodeCoreGateway ngw = NodeCoreGateway.getInstance();
            address =ngw.getInfo().getPayload().getDefaultAddress();
        }
        else if (selectedAddress.equals(NO_ADDRESS_SELECTED))
        {
            address = null;
        }
        else
        {
            address = selectedAddress;
        }

        _tabTransactionsModel.setSelectedAddress(address);

    }

    private void updateModelTransactionsConfirmed()
    {
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();

        List<WalletTransactionEntity> walletTransactions = null;

        String address = getTabTransactionsModel().getSelectedAddress();

        //Punt to  NCGW call - if address==NULL (all), then will call LIST command
        //NOTE - could loop through all addresses in getBalance, but use LIST instead
        CommandResult<WalletTransactionsEntity> walletTransactionsResult = ngw.getWalletTransactions(
                address,
                getTabTransactionsModel().getPageNumber(),
                getTabTransactionsModel().getResultsPerPage(),
                false);

        if (walletTransactionsResult.isSuccess())
        {
            //great! proceed like normal
        }
        else
        {
            //has error, display this
            getTabTransactionsModel().setValidationInfo(walletTransactionsResult.getValidationInfo());
            return;
        }

        WalletTransactionsEntity entity = walletTransactionsResult.getPayload();

        getTabTransactionsModel().setReplyMessage(entity.getReplyMessage());
        getTabTransactionsModel().setCacheState(entity.getCacheState());
        if (entity.getCacheState() == WalletTransactionsEntity.CacheState.CURRENT)
        {
            //great!
            getTabTransactionsModel().setWalletTransactions(entity.getWalletTransactions());
        }
        else
        {
            //even if not CURRENT, set this to a non-null value
            getTabTransactionsModel().setWalletTransactions(new ArrayList<>());
        }
    }

    private void updateModelTransactionsPending() {
        int maxPendingRows = 10000; //don't page on pending, as there should be so few
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();
        //GRPC command already gets all, and we're not doing paging anyway
        CommandResult<WalletTransactionsEntity> walletTransactionsResult = ngw.getWalletTransactions(
                getTabTransactionsModel().getSelectedAddress(),
                1,
                maxPendingRows,
                true);

        WalletTransactionsEntity entity = walletTransactionsResult.getPayload();
        if (entity == null)
        {
            entity = new WalletTransactionsEntity();
        }
        getTabTransactionsModel().setWalletTransactionsPending(entity.getWalletTransactions());

        //set cacheState both here and in ConfirmedGrid
        getTabTransactionsModel().setCacheState(entity.getCacheState());
    }

    //endregion

    //region UI - Update

    /*
    private void updateDefaultAddressLabel()
    {
        String strAddress=  getTabTransactionsModel().getSelectedAddress();
        this.LblDefaultAddress.setText(strAddress);
    }
    */

    private void updateGridPending()
    {
        //update model
        updateModelTransactionsPending();

        //update UI
        List<WalletTransactionEntity> rows = this.getTabTransactionsModel().getWalletTransactionsPending();

        ObservableList<WalletTransactionEntity> tableList = FXCollections.observableArrayList(rows);
        gridPending.setItems(tableList);

        if (rows.size() == 0)
        {
            if (_tabTransactionsModel.isCacheStateRebuilding())
                ControlHelper.setTableViewNoResultsFoundMessage(gridPending, _localeModule.getString("grid.cacheStillRebuilding"));
            else
                ControlHelper.setTableViewNoResultsFoundMessage(gridPending, _localeModule.getString("grid.noResults"));
        }
    }


    //gets called when clicking Next/Prev, or even setting the max number of pages
    private Node startEventCreatePageConfirmed(int pageIndex)
    {
        /*
        if (_skipNextCreatePage)
        {
            _skipNextCreatePage = false;
            return new VBox();
        }
        */
        //_logger.info("startEventCreatePageConfirmed {}", this.PageControl.getCurrentPageIndex());
        //this.LblTest2.setText(LblTest2.getText() + "," + Integer.toString(pageIndex + 1));

        //page index 0-based, model is 1-based, so pass in +1
        updatePageDataConfirmed(pageIndex+1);
        updateStatusMessage();
        return new VBox();  //some non-null object
    }

    //Pass in pageIndex 1-based
    private void updatePageDataConfirmed(Integer pageIndex)
    {
        //System.out.println("updatePageDataConfirmed " + pageIndex);

        getTabTransactionsModel().setPageNumber(pageIndex);
        updateModelTransactionsConfirmed();

        List<WalletTransactionEntity> entities = this.getTabTransactionsModel().getWalletTransactions();

        //Connvert
        List<WalletTransactionConfirmedRow> rows = new ArrayList<>();
        for(WalletTransactionEntity entity : entities) {
            rows.add(new WalletTransactionConfirmedRow(entity));
        }

        ObservableList<WalletTransactionConfirmedRow> tableList = FXCollections.observableArrayList(rows);
        mainGrid.setItems(tableList);

        if (tableList.size() == 0)
        {
            if (_tabTransactionsModel.isCacheStateRebuilding())
                ControlHelper.setTableViewNoResultsFoundMessage(mainGrid, _localeModule.getString("grid.cacheStillRebuilding"));
            else
                ControlHelper.setTableViewNoResultsFoundMessage(mainGrid, _localeModule.getString("grid.noResults"));
        }

        //Special case if on last page:
        //if results < max allowed for page, then we're at the end
        //if (this.getTabTransactionsModel().getWalletTransactions().size() <
        //        this.getTabTransactionsModel().getResultsPerPage())
        int iResultsFound = this.getTabTransactionsModel().getWalletTransactions().size();
        setPagingMaxPages(iResultsFound);
    }

    //private boolean _skipNextCreatePage = false;
    private void setPagingMaxPages(int iResultsFound)
    {
        //CASE 1: clicked next and nothing there
        if (iResultsFound == 0) {
            //Naive - just punt back a page
            int iPageCount = pageControl.getCurrentPageIndex(); //off by 1, but this is 1 ahead, so they cancel out
            _appContext.UIManager.setStatusMessage(_localeModule.getString("pager.onlyNPages"));
            this.pageControl.setCurrentPageIndex( pageControl.getCurrentPageIndex() - 1);
            //will re-trigger the startEventCreatePageConfirmed, which will retrigger this
        }


        //int currentPage_1based = PageControl.getCurrentPageIndex()+ 1;

        //TODO - PROBLEM --> paging control does not have an easy way to just disable the "NEXT" button, without re-triggering
        //  hitting a new page
        //setpagecount could disable next... but it reset the control

        //CASE 2: current page doesn't have enough rows, so can hide the next button
        /*
        if (iResultsFound < getTabTransactionsModel().getResultsPerPage())
        {

            //simply make this our last page... not changing the page index
            _skipNextCreatePage = true;
            this.PageControl.setPageCount(currentPage_1based);

            _skipNextCreatePage = true;
            this.PageControl.set(currentPage_1based);

        }
 */


        /*
        if (iResultsFound == -1)
        {
            //set the last page so we can be smart

            _paging_lastPage_1based = currentPage_1based;

            //Hide next button
            //bumps us back a page... will trigger create page
            this.PageControl.setCurrentPageIndex( PageControl.getCurrentPageIndex() - 1);


            //Send message
            //_appContext.UIManager.setStatusMessage("No more rows");

            //int i = PageControl.getCurrentPageIndex() + 1; //off by one,
            this.PageControl.setPageCount(_paging_lastPage_1based);   //TEST - hardcore
            //on last page
            //this.PageControl.setPageCount(this.getTabTransactionsModel().getPageNumber());
        }
*/



    }

    //endregion

    //region UI - Setup

    public ChoiceBox<AddressBalanceEntity> ddAddresses;

    public TextField LblDefaultAddress;
    public ChoiceBox<Integer> ddPageSize;
    public Pagination pageControl;

    public TableView<WalletTransactionConfirmedRow> mainGrid;
    public TableColumn<WalletTransactionConfirmedRow, DateTime> colTimeStamp;
    public TableColumn<WalletTransactionConfirmedRow, Integer> colBlockNum;
    public TableColumn<WalletTransactionConfirmedRow, Integer> colConfirmations;
    public TableColumn<WalletTransactionConfirmedRow, String> colTxType;
    public TableColumn<WalletTransactionConfirmedRow, String> colAddressMine;
    public TableColumn<WalletTransactionConfirmedRow, String> colAddressFrom;
    public TableColumn<WalletTransactionConfirmedRow, String> colAddressTo;
    public TableColumn<WalletTransactionConfirmedRow, Double> colAmount;
    public TableColumn<WalletTransactionConfirmedRow, Double> colBalance;
    public TableColumn<WalletTransactionConfirmedRow, String> colTxId;
    public TableColumn<WalletTransactionConfirmedRow, String> colStatus;

    public TableView<WalletTransactionEntity> gridPending;
    public TableColumn<WalletTransactionEntity, String> colTxTypePending;
    public TableColumn<WalletTransactionEntity, String> colAddressMinePending;
    public TableColumn<WalletTransactionEntity, String> colAddressFromPending;
    public TableColumn<WalletTransactionEntity, String> colAddressToPending;
    public TableColumn<WalletTransactionEntity, Double> colAmountPending;
    public TableColumn<WalletTransactionEntity, String> colTxIdPending;
    public TableColumn<WalletTransactionEntity, String> colStatusPending;

    public Label lblSelectAddress;
    public Label lblPageSize;
    public Label headerPending;
    public Label headerConfirmed;
    public Button btnRebuildCache;
    public Label lblRefreshTime;

    private void setupPageSizeDropdown(int selectedValue)
    {
        //this.ddPageSize.getItems().add(5);
        this.ddPageSize.getItems().add(10);
        this.ddPageSize.getItems().add(20);
        this.ddPageSize.getItems().add(50);
        this.ddPageSize.getItems().add(200);

        //set default item
        //this.DdPageSize.selectionModelProperty(SingleSelectionModel<Integer>);
        ddPageSize.setValue(selectedValue);

        //TODO --> push to Control helper?
        ddPageSize.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                startEventUpdatePageSize((int)newValue);
            }
        });
    }

    private void startEventUpdatePageSize(int itemIndex)
    {
        int newPageSize =  ddPageSize.getItems().get(itemIndex);

        this.getTabTransactionsModel().setResultsPerPage(newPageSize);

        //this.updateGridConfirmed();
        //refresh!
        //System.out.println("Hit!");

        int iFirstPage = 1; //1-based
        updatePageDataConfirmed(iFirstPage); //restart 1 1

        this.pageControl.setCurrentPageIndex(iFirstPage-1);    //does not trigger getting data

        //Persist
        UserSettings.save(SettingsConstants.TRANSACTION_PAGESIZECONFIRMED, newPageSize);

        updateStatusMessage();
    }

    //1-based index
    //private int _paging_lastPage_1based= -1;
    private void setupPagingConfirmed()
    {
        _logger.info("setupPagingConfirmed {}", this.pageControl.getCurrentPageIndex());
        //Set pagination
        this.pageControl.setPageFactory(new Callback<Integer, Node>() {
                                            @Override
                                            public Node call(Integer pageIndex) {
                                                return startEventCreatePageConfirmed(pageIndex);
                                            }
                                        }
        );
        this.pageControl.setCurrentPageIndex(0);
        this.pageControl.setMaxPageIndicatorCount(1);   //number of boxes to show on the bottom
        //this.PageControl.setPageCount(5); //don't set if you don't know the total amount
    }

    private void setupGridConfirmed() {
        //Pull all into one standard grid format method?
        mainGrid.setEditable(false);
        mainGrid.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ControlHelper.installCopyPasteHandler(this.mainGrid);

        colTimeStamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        //NOTE - add filter --> can add dropdowns/etc...
        //ColTxType.setGraphic(new Button("Some Filter"));

        colConfirmations.setCellValueFactory(new PropertyValueFactory<>("confirmCountUpdated"));
        colBlockNum.setCellValueFactory(new PropertyValueFactory<>("blockHeight"));

        colTxType.setCellValueFactory(new PropertyValueFactory<>("txType"));

        colAddressMine.setCellValueFactory(new PropertyValueFactory<>("addressMine"));
        colAddressFrom.setCellValueFactory(new PropertyValueFactory<>("addressFrom"));
        colAddressTo.setCellValueFactory(new PropertyValueFactory<>("addressTo"));

        colAmount.setCellValueFactory(new PropertyValueFactory<>("amountAsString"));


        //ColBalance.setCellValueFactory(new PropertyValueFactory<>("xxxxxx"));
        colTxId.setCellValueFactory(new PropertyValueFactory<>("txId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        //set locale
        colTimeStamp.setText(_localeModule.getString("colTimeStamp"));
        colBlockNum.setText(_localeModule.getString("colBlockNum"));
        colConfirmations.setText(_localeModule.getString("colConfirmations"));
        colTxType.setText(_localeModule.getString("colTxType"));
        colAddressMine.setText(_localeModule.getString("colAddressMine"));
        colAddressFrom.setText(_localeModule.getString("colAddressFrom"));
        colAddressTo.setText(_localeModule.getString("colAddressTo"));
        colAmount.setText(String.format(
                _localeModule.getString("colAmount"), _appContext.Configuration.getVbkUnit()));
        colBalance.setText(_localeModule.getString("colBalance"));
        colTxId.setText(_localeModule.getString("colTxId"));
        colStatus.setText(_localeModule.getString("colStatus"));
    }

    private void setupGridPending() {
        //Pull all into one standard grid format method?
        gridPending.setEditable(false);
        gridPending.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ControlHelper.installCopyPasteHandler(this.gridPending);

        colTxTypePending.setCellValueFactory(new PropertyValueFactory<>("txType"));
        colAddressMinePending.setCellValueFactory(new PropertyValueFactory<>("addressMine"));
        colAddressFromPending.setCellValueFactory(new PropertyValueFactory<>("addressFrom"));
        colAddressToPending.setCellValueFactory(new PropertyValueFactory<>("addressTo"));
        colAmountPending.setCellValueFactory(new PropertyValueFactory<>("amountAsString"));

        colTxIdPending.setCellValueFactory(new PropertyValueFactory<>("txId"));
        colStatusPending.setCellValueFactory(new PropertyValueFactory<>("status"));

        //set locale
        colTxTypePending.setText(_localeModule.getString("colTxTypePending"));
        colAddressMinePending.setText(_localeModule.getString("colAddressMinePending"));
        colAddressFromPending.setText(_localeModule.getString("colAddressFromPending"));
        colAddressToPending.setText(_localeModule.getString("colAddressToPending"));
        colAmountPending.setText(String.format(
                _localeModule.getString("colAmountPending"), _appContext.Configuration.getVbkUnit()));
        colTxIdPending.setText(_localeModule.getString("colTxIdPending"));
        colStatusPending.setText(_localeModule.getString("colStatusPending"));

        final double grid_def_height = 200;
        gridPending.setPrefHeight(grid_def_height);
        gridPending.itemsProperty().addListener(
            (observable, oldValue, newValue) -> {
                if(gridPending.getItems().size()>1)
                    gridPending.setPrefHeight(Region.USE_COMPUTED_SIZE);
                else
                    gridPending.setPrefHeight(grid_def_height);
                gridPending.layout();

            });
    }

    private AddressSummary _addressesSummary;

    //TODO --> make reusable for any address dropdown
    private void setupAddressDropdown() {
        CommandResult<AddressSummary> commandResult = WalletService.getAddressSummary();;
        if (!commandResult.isSuccess()) {
            //bad, should not happen by the time this can be called
            return;
        }

        _addressesSummary = commandResult.getPayload();
        _addressesSummary.sortByHighestConfirmed();
        List<AddressBalanceEntity> addresses = _addressesSummary.getAddresses();
        WalletService.setupModelMergeNickNames(addresses);

        //bind all
        ddAddresses.getItems().addAll(addresses);

        //select all option
        boolean enableSelectAll = true;
        if (enableSelectAll)
        {
            this.ddAddresses.getItems().add(0, null);
        }

        //set value from model
        //Assume default address
        String selectedAddress = this.getTabTransactionsModel().getSelectedAddress();
        FormatHelper.setAddressDropdownByValue(this.ddAddresses, selectedAddress);

        ddAddresses.valueProperty().addListener((obs, oldval, newval) -> {
            String address = null;
            if (newval == null)
            {
                //show all
                address= null;
                UserSettings.save(SettingsConstants.TRANSACTION_SELECTEDADDRESS, NO_ADDRESS_SELECTED);
            }
            else
            {
                address = newval.getAddress();
                UserSettings.save(SettingsConstants.TRANSACTION_SELECTEDADDRESS, address);
            }

            startEventUpdateSelectedAddress(address);
        });

        FormatHelper.formatAddressDropdown(this.ddAddresses, _appContext.Configuration.getVbkUnit());
    }

    private final String NO_ADDRESS_SELECTED = "NoAddressSelected";

    private void startEventUpdateSelectedAddress(String address) {
        //update model
        getTabTransactionsModel().setSelectedAddress(address);

        //update data
        refreshPage();

        updateStatusMessage();
    }

    private void updateStatusMessage()
    {
        //pull from model

        ValidationInfo vi = getTabTransactionsModel().getValidationInfo();
        if (vi != null && vi.isWarningOrError())
        {
            _appContext.UIManager.setStatusMessage(vi);
            return;
        }

        WalletTransactionsEntity.CacheState cacheState = getTabTransactionsModel().getCacheState();
        if (cacheState == WalletTransactionsEntity.CacheState.CURRENT)
        {
            //great!
            if (getTabTransactionsModel().getSelectedAddress() == null) {
                _appContext.UIManager.setStatusMessage(String.format(_localeModule.getString("status.gotAllAddresses")));
            } else {
                _appContext.UIManager.setStatusMessage(String.format(_localeModule.getString("status.gotSpecificAddress"), getTabTransactionsModel().getSelectedAddress()));
            }
        }
        else if (cacheState == WalletTransactionsEntity.CacheState.BUILDING)
        {
            //Boo!
            updateStatusMessage_rebuildCache(cacheState);
        }
        else
        {
            //Error
            HashMap<String, String> patternLookups = new HashMap<String, String>();
            patternLookups.put("No best block could be found", "apiMessage.noBestBlockFound");

            String techMessage = _localeModule.getLookupPattern(
                    getTabTransactionsModel().getReplyMessage(), patternLookups);

            String sFinal = String.format(
                    "%1$s %2$s %3$s",
                    String.format(_localeModule.getString("status.couldNotGet"), cacheState),
                    _localeModule.getString("status.error.corrupt"),
                    String.format(_localeModule.getString( "status.error.technicalWrapper"), techMessage)
            );

            _appContext.UIManager.setStatusMessageWarning(sFinal);
        }

    }

    private void updateStatusMessage_rebuildCache( WalletTransactionsEntity.CacheState cacheState)
    {
        if (cacheState != WalletTransactionsEntity.CacheState.BUILDING)
            return;

        //get stateinfo
        StateInfoEntity gsi = NodeCoreGateway.getInstance().getGetStateInfo().getPayload();
        String s = String.format(_localeModule.getString("status.couldNotGet.cacheRebuilding"),
                gsi.WalletCacheSyncHeight, gsi.LocalBlockchainHeight,
                FormatHelper.formatPercentWhole(
                        (double) gsi.WalletCacheSyncHeight / (double) gsi.LocalBlockchainHeight));
        _appContext.UIManager.setStatusMessageWarning(s);
    }

    /**
     * Create cellfactory for convenient styling and manipulating cells
     * */
    private void setUpColumnCellFactory(){
        colAddressFrom.setCellFactory(FormatHelper.addressCellCallBacks(null));
        colAddressFromPending.setCellFactory(FormatHelper.addressCellCallBacks(null));
        colAddressMine.setCellFactory(FormatHelper.addressCellCallBacks(null));
        colAddressMinePending.setCellFactory(FormatHelper.addressCellCallBacks(null));
        colAddressTo.setCellFactory(FormatHelper.addressCellCallBacks(null));
        colAddressToPending.setCellFactory(FormatHelper.addressCellCallBacks(null));

        colTimeStamp.setCellFactory(FormatHelper.dateFormatCallback());

    }

    public void clickRefreshView() {
        SoundItem.playButtonClick();

        _logger.info("Clicked refresh_page button");

        //update data
        refreshPage();

        updateStatusMessage();
    }

    public void onBalanceChanged(boolean didChange, long oldAmount, long newAmount)
    {
        _logger.info("Call onBalanceChanged");

        //Update main grid --> not worth trying to catch a Pending transaction
        //  User can refresh if expecting
        //  User can wait

        refreshPage();
    }

    //Called by main program as part of loop
    public void onCurrentBlockChanged(int oldBlockHeight, int newBlockHeight) {

        //NOTE: Performance - don't reget all of transactions (potentially heavy hit),
        // as only the confirmationCount would change upon blockheight increase
        //if there is a new transaction, then onBalanceChanged would catch that
        //Most users will not have new balance every 30 seconds.. but everyone has new block every ~30 seconds

        //grid should autobind
        if (_tabTransactionsModel == null)
            return;
        _tabTransactionsModel.currentBlock = newBlockHeight;
        this.mainGrid.refresh();    //force data binding update

        //Update the timestamp
        refreshPage_timestamp();
    }

    private boolean refreshConfirmed_InProgress = false;
    private void refreshPage() {
        _logger.info("refreshPage. AlreadyRunning={}", refreshConfirmed_InProgress);

        if (refreshConfirmed_InProgress)
            return;

        refreshConfirmed_InProgress = true;


        //Pending Grid
        updateGridPending();

        //Confirmed Grid
        int iFirstPage = 1; //1-based
        updatePageDataConfirmed(iFirstPage); //restart 1 1  //TODO --> persist current page
        this.pageControl.setCurrentPageIndex(iFirstPage - 1);

        //update label
        refreshPage_timestamp();

        refreshConfirmed_InProgress = false;
    }
    private void refreshPage_timestamp()
    {
        this.lblRefreshTime.setText(_localeModule.getString("refreshLabel_update") +
                " " + Utils.getTimeOfDayNow());
    }

    //region Refresh Wallet Cache

    private BackgroundTask _backgroundTaskWalletCache;

    public void clickRefreshWalletCache()
    {
        SoundItem.playButtonClick();

        String message = "";

        boolean shouldContinue = ControlHelper.showAlertYesNoDialog(_localeModule.getString("refreshCache.warning"));

        if (shouldContinue)
        {
            //do actionMessage!

            //it GRPC
            CommandResult<Void> result = NodeCoreGateway.getInstance().refreshWalletCache();
            if (result.isSuccess())
            {
                //great!
                _appContext.UIManager.setStatusMessageSucess(_localeModule.getString("refreshCache.warning.proceed"));

                kickoffBackgroundWalletStatus();
            }
            else
            {
                //bad
                _appContext.UIManager.setStatusMessageCommandError(result);
            }
        }
        else
        {
            _appContext.UIManager.setStatusMessage(_localeModule.getString("refreshCache.warning.cancel"));
        }
    }

    //Different frequency than main refresh: blockheight=30 seconds, or balance=possibly hours.
    //Assumes cache is being rebuilt
    private void kickoffBackgroundWalletStatus()
    {
        //kick off refresh every ~10 seconds until done
        if (_backgroundTaskWalletCache != null)
            _backgroundTaskWalletCache.dispose();

        _backgroundTaskWalletCache = new BackgroundTask();
        _backgroundTaskWalletCache.initialDelaySeconds = 3; //let original prompt be seen

        int walletCacheRefreshPingSeconds = 3;
        _gsiWallet = null;  //reset
        _backgroundTaskWalletCache.start(
                walletCacheRefreshPingSeconds,
                () -> {
                    background_loop_refreshCache();
                    return null;
                },
                () -> {
                    background_done_refreshCache();
                    return null;
                }
        );
    }

    private  StateInfoEntity _gsiWallet;

    private void background_loop_refreshCache()
    {
        _gsiWallet = NodeCoreGateway.getInstance().getGetStateInfo().getPayload();
    }

    private void background_done_refreshCache()
    {
        //update status
        StateInfoEntity gsi = _gsiWallet;

        //is still updating?
        if (gsi.isWalletCacheBuilding())
        {
            //Still building wallet cache
            //update just status
            String s = String.format(_localeModule.getString("status.couldNotGet.cacheRebuilding"),
                    gsi.WalletCacheSyncHeight, gsi.LocalBlockchainHeight,
                    FormatHelper.formatPercentWhole(
                            (double) gsi.WalletCacheSyncHeight / (double) gsi.LocalBlockchainHeight));
            _appContext.UIManager.setStatusMessageWarning(s);
        }
        else
        {
            //caught up!
            _appContext.UIManager.clearStatusMessage();
            _backgroundTaskWalletCache.stop();
            _gsiWallet = null;
            //refresh entire page:
            refreshPage();
        }
    }

    //endregion

    public void clickExport() {
        List items = mainGrid.getItems();
        ValidationInfo vi = ShellService.doExport(items);
        _appContext.UIManager.setStatusMessage(vi);
    }

    public class WalletTransactionConfirmedRow extends WalletTransactionEntity implements Exportable {

        public String createCsvRow(boolean isHeader) {
            if (isHeader) {
                return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s",
                        "Timestamp",
                        "BlockHeight",
                        "TxType",
                        "AddressMine",
                        "AddressFrom",
                        "AddressTo",
                        "Amount",
                        "TxId",
                        "Status"
                );
            } else {
                //return data here
                return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s",
                        this.getTimestamp(),
                        this.getBlockHeight(),
                        this.getTxType(),
                        this.getAddressMine(),
                        Utils.isNull(this.getAddressFrom()),
                        Utils.isNull(this.getAddressTo()),
                        this.getAmountAsString(),
                        this.getTxId(),
                        this.getStatus()
                );
            }
        }

        //add extra field for confirmed

        public int getConfirmedUpdated() {
            //pull from current block
            return 0;
        }

        public WalletTransactionConfirmedRow() {

        }

        public WalletTransactionConfirmedRow(WalletTransactionEntity entity) {

            this.setTxId(entity.getTxId());
            this.setTimestamp(entity.getTimestamp());

            this.setConfirmations(entity.getConfirmations());
            this.setBlockHeight(entity.getBlockHeight());
            this.setAddressMine(entity.getAddressMine());
            this.setAddressTo(entity.getAddressTo());
            this.setAddressFrom(entity.getAddressFrom());

            this.amount = entity.amount;
            this.setTxType(entity.getTxType());
            this.setStatus(entity.getStatus());
        }

        public int getConfirmCountUpdated()
        {
            if (_tabTransactionsModel == null)
            {
                return 0;
            }

            if (_tabTransactionsModel.currentBlock <= 0)
            {
                return this.getConfirmations();
            }

            int updatedCount = _tabTransactionsModel.currentBlock -
                    this.getBlockHeight() + 1;
            return updatedCount;
        }
    }

}
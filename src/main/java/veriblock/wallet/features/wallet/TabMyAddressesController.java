// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.storage.OrmLiteAddressRepository;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.entities.AddressBalanceEntity;
import veriblock.wallet.entities.NewAddressEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.features.shell.ShellService;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.FormatHelper;
import veriblock.wallet.features.TabEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import veriblock.wallet.uicommon.Styles;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static veriblock.wallet.core.ValidationInfo.Status.Info;
import static veriblock.wallet.core.ValidationInfo.Status.Success;

public class TabMyAddressesController extends BaseController  {

    private static final Logger _logger = LoggerFactory.getLogger(TabMyAddressesController.class);

    public void initialize() {

    }

    private LocaleModule _localeModule;
    public void init() {
        setLocale();

        setupUI();

        loadAddressData_start();

    }

    public void setupUI()
    {
        ControlHelper.setLabelCopyProperties(this.lblTotalBalance);

        setupGrid();
    }

    //region Load data - model

    private TabMyAddressesModel _addressesModel;

    private void loadAddressData_start() {
        if (_getAddressDataTask == null) {
            _getAddressDataTask = new GetAddressDataTask();
            _getAddressDataTask.setOnSucceeded(e -> {
                getAddressDataTask_done();
            });
            new Thread(_getAddressDataTask).start();
        }
    }

    private GetAddressDataTask _getAddressDataTask;
    public class GetAddressDataTask extends Task<CommandResult<TabMyAddressesModel>> {
        @Override
        protected CommandResult<TabMyAddressesModel> call() throws Exception {
            //Do call here:
            CommandResult<TabMyAddressesModel> result = getTabAddressModel_start();
            return result;
        }
    }

    private CommandResult<TabMyAddressesModel> getTabAddressModel_start()
    {
        CommandResult<AddressSummary> commandResult = WalletService.getAddressSummary();

        TabMyAddressesModel model =new TabMyAddressesModel();
        model.setAddressSummary(commandResult.getPayload());

        CommandResult<TabMyAddressesModel> commandModel = new CommandResult<>(
                model,    commandResult.getConnectionResult(), commandResult.getValidationInfo()
        );

        return commandModel;
    }

    /*
    Model set here
     */
    private void getAddressDataTask_done()
    {
        CommandResult<TabMyAddressesModel> result = _getAddressDataTask.getValue();
        _logger.info("AsyncTask GetAddressDataTask - Done");
        _getAddressDataTask = null;   //reset to allow next loop to call it

        boolean savedHideZeroBalances = UserSettings.getValueBoolean(SettingsConstants.MYADDRESSES_HIDE_ZERO_BALANCE, false);


        if (result.isSuccess()) {
            //great!
            TabMyAddressesModel model = result.getPayload();
            HashMap<String, String> nicknames = WalletService.setupModelMergeNickNames(model.getAddressSummary().getAddresses());
            model.setNicknamesOriginal(nicknames);
            _addressesModel = model;
            updateUIHideZeroBalances(savedHideZeroBalances);

            //Persist addresses to cache!
            UserSettings.saveJson(SettingsConstants.MYADDRESSES_CACHEDADDRESSS,
                    _addressesModel.getAddressSummary().getAddresses());
            updateGrid();
        }
        else
        {
            //Show cached values!
            TabMyAddressesModel model = new TabMyAddressesModel();

            model.setIsCached(true);
            AddressBalanceEntity[] arr = UserSettings.getValueJson(SettingsConstants.MYADDRESSES_CACHEDADDRESSS,
                    AddressBalanceEntity[].class);
            if (arr == null)
            {
                //doesn't exist yet
                arr = new AddressBalanceEntity[0];
            }
            List<AddressBalanceEntity> cachedAddresses =  Arrays.asList(arr);

            HashMap<String, String> nicknames = WalletService.setupModelMergeNickNames(cachedAddresses);
            model.setNicknamesOriginal(nicknames);

            if (cachedAddresses != null && cachedAddresses.size() > 0) {
                //Have something from cache, use it!
                model.getAddressSummary().setAddresses(cachedAddresses);
                _addressesModel = model;
                updateUIHideZeroBalances(savedHideZeroBalances);

                updateGrid();
                updateIfCached();

                _appContext.UIManager.setStatusMessageWarning(
                        _localeModule.getString("status.usedCachedAddress"));
            }
            else
            {
                //got nothing (likely first time)
                _appContext.UIManager.navigateToTab(TabEnum.NotConnected);
            }

        }

        refreshPage_timestamp();
    }

    //endregion

    //region Create new address

    public void clickNewAddress()
    {
        SoundItem.playButtonClick();
        createNewAddress();
    }

    private void createNewAddress()
    {

        //do basic confirmation
        String message = _localeModule.getString("createNewAddress.areYouSure");

        //Add special warning if zero addresses would be hidden (as creating new address would unhide them)
        if (_addressesModel.hideZeroBalanceAddresses) {
            String messageHiddenZero = "\n\n"
                    + String.format(_localeModule.getString("hideZero_addNew_warning"),
                    _localeModule.getString("checkbox_hideZero"));
            message = message + messageHiddenZero;
        }

        boolean proceed = ControlHelper.showAlertYesNoDialog(message);


        if (proceed)
        {
            //kick off async call!
            this.btnCreateNew.setDisable(true);
            createNewAddress_start();
        }
        else
        {
            _appContext.UIManager.setStatusMessage(_localeModule.getString("createNewAddress.cancelled"));
        }
    }

    private void createNewAddress_start()
    {
        if (_getNewAddressTask == null) {
            NodeCoreGateway ngw = NodeCoreGateway.getInstance();
            boolean successfullyUnlocked = WalletService.promptUnlockWallet(_appContext, ngw);
            if (!successfullyUnlocked)
            {
                LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
                _appContext.UIManager.setStatusMessageWarning(lm.getString("walletService_passwordCancelled"));
                return;
            }

            _getNewAddressTask = new GetNewAddressTask();
            _getNewAddressTask.setOnSucceeded(e -> {
                createNewAddress_done();
            });
            new Thread(_getNewAddressTask).start();
        }

    }

    private GetNewAddressTask _getNewAddressTask;
    public class GetNewAddressTask extends Task<CommandResult<NewAddressEntity>> {
        @Override
        protected CommandResult<NewAddressEntity> call() throws Exception {
            //Do call here:
            NodeCoreGateway ngw = NodeCoreGateway.getInstance();
            CommandResult<NewAddressEntity> result = ngw.getNewAddress();
            return result;
        }
    }

    //Finish call (run continuously)
    private void createNewAddress_done()
    {
        CommandResult<NewAddressEntity> result = _getNewAddressTask.getValue();
        _logger.info("AsyncTask GetNewAddressTask - Done");
        _getNewAddressTask = null;   //reset to allow next loop to call it
        this.btnCreateNew.setDisable(false);
        if (result.isSuccess()) {
            //SUCCESS - great!
            String newAddress = result.getPayload().getNewAddress();
            _appContext.UIManager.setStatusMessage(new ValidationInfo(Success,
                    String.format(_localeModule.getString("createNewAddress.sucess"), newAddress)));

            _addressesModel.getAddressSummary().getAddresses().add(new AddressBalanceEntity(result.getPayload().getNewAddress()));

            //If adding a new address, it's default balance = zero, and we won't see it unless we unhide
            updateUIHideZeroBalances(false);

            updateGrid();

            //Prompt for backup here
            String headerText = String.format(_localeModule.getString("backup_prompt1"), newAddress);
            ValidationInfo viResult = WalletService.promptBackup(headerText);
            _appContext.UIManager.setStatusMessage(viResult);
        }
        else
        {
            if (!result.getConnectionResult().isConnectedAndSynced())
            {
                _appContext.UIManager.navigateToTab(TabEnum.NotConnected);
            }
        }
    }


    //endregion

    //region UI - Setup


    public TableView<MyAddressRow> mainGrid;
    public TableColumn<MyAddressRow, TextField> colNickName;
    public TableColumn<MyAddressRow, String> colAddress;
    public TableColumn<MyAddressRow, Long> colPending;
    public TableColumn<MyAddressRow, Long> colConfirmed;
    public TableColumn<MyAddressRow, Boolean> colDefault;

    public  Button btnCreateNew;
    public Label lblTotalBalanceLabel;
    public TextField lblTotalBalance;

    public Button btnExport;

    public CheckBox chkHideZeroBalance;

    //Update from model. Could populate by (1) pulling from NC, (2) creating a new address
    ObservableList<MyAddressRow> _tableList;

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabMyAddresses);

        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));
        btnCreateNew.setText(_localeModule.getString("button.createNew.text"));
        lblTotalBalanceLabel.setText(_localeModule.getString("label.balance.text"));

        chkHideZeroBalance.setText(_localeModule.getString("checkbox_hideZero"));

        ControlBuilder.setupExportButton(btnExport);
    }

    private void setupGrid()
    {
        mainGrid.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ControlHelper.installCopyPasteHandler(this.mainGrid);

        mainGrid.setEditable(true);

        //Bind property to column!
        //NOTE --> need properties of the form getAddressMine(), follow standard Java convention
        colNickName.setCellValueFactory(new PropertyValueFactory<>("nickNameTextField"));
        colNickName.setVisible(true);  //hide for now

        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPending.setCellValueFactory(new PropertyValueFactory<>("amountPendingAtomic"));
        colConfirmed.setCellValueFactory(new PropertyValueFactory<>("amountConfirmedAtomic"));
        colDefault.setCellValueFactory(new PropertyValueFactory<>("isDefault"));

        String unit = _appContext.Configuration.getVbkUnit();

        //Set text header (use locale)
        colNickName.setText(_localeModule.getString("column.nickName.header"));
        colAddress.setText(_localeModule.getString("column.address.header"));
        colPending.setText(String.format(_localeModule.getString("column.amountPending.header"), unit));
        colConfirmed.setText(String.format(_localeModule.getString("column.amountConfirmed.header"), unit));
        colDefault.setText(_localeModule.getString("column.isDefault.header"));

        colNickName.setEditable(true);



        colAddress.setCellFactory(
                new Callback<TableColumn<MyAddressRow, String>, TableCell<MyAddressRow, String>>() {
                    @Override
                    public TableCell<MyAddressRow, String> call(final TableColumn<MyAddressRow, String> param) {
                        final TableCell<MyAddressRow, String> cell = new TableCell<MyAddressRow, String>() {

                            final Label label = new Label("-");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty ? null : item);
                                setStyle("-fx-text-fill: #00a688");
                            }
                        };
                        return cell;
                    }
                }
        );

        colPending.setCellFactory(
                new Callback<TableColumn<MyAddressRow, Long>, TableCell<MyAddressRow, Long>>() {
                    @Override
                    public TableCell<MyAddressRow, Long> call(final TableColumn<MyAddressRow, Long> param) {
                        final TableCell<MyAddressRow, Long> cell = new TableCell<MyAddressRow, Long>() {

                            @Override
                            public void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty);
                                String sValue = VbkUtils.convertAtomicToVbkString(item);
                                setText(empty ? null : "" + sValue);
                                setAlignment(Pos.CENTER_LEFT);
                                if (item != null) {
                                    Long value = item.longValue();
                                    String color = value >= 0 ? "#00a688" : value < 0 ? "red" : "orange";
                                    setStyle("-fx-text-fill: " + color);
                                }
                            }
                        };
                        return cell;
                    }
                }
        );

        colConfirmed.setCellFactory(new Callback<TableColumn<MyAddressRow, Long>, TableCell<MyAddressRow, Long>>() {
                    @Override
                    public TableCell<MyAddressRow, Long> call(final TableColumn<MyAddressRow, Long> param) {
                        final TableCell<MyAddressRow, Long> cell = new TableCell<MyAddressRow, Long>() {

                            @Override
                            public void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty);
                                String sValue = VbkUtils.convertAtomicToVbkString(item);
                                setText(empty ? null : "" + sValue);
                                setAlignment(Pos.CENTER_LEFT);
                              if(item!=null){
                                  Long value = item.longValue();
                                String color = value >=0? "#00a688" : value <0? "red" : "orange";
                                setStyle("-fx-text-fill: "+color);
                              }
                            }
                        };
                        return cell;
                    }
                }
        );

        colDefault.setCellFactory(
                new Callback<TableColumn<MyAddressRow, Boolean>, TableCell<MyAddressRow, Boolean>>() {

                    @Override
                    public TableCell<MyAddressRow, Boolean> call(final TableColumn<MyAddressRow, Boolean> param) {
                        final TableCell<MyAddressRow, Boolean> cell = new TableCell<MyAddressRow, Boolean>() {

                            //Use final, else UI gets screwy
                            final Button btn = new Button("...");



                            //NOTE --> this could be re-called, it MUST be idempotent
                            @Override
                            public void updateItem(Boolean item, boolean empty) {
                                super.updateItem(item, empty);

                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    //Have data
                                    AddressBalanceEntity row = getTableView().getItems().get(getIndex());//messup if selected index and scroll grid down
                                    String address = row.getAddress();

                                    boolean isDefault = false;
                                    if (item != null) {
                                        isDefault = item.booleanValue();
                                    }
                                    int iRowIndex = getIndex();
                                    setCellButtonDefault(btn, isDefault);
                                    btn.setOnAction(event -> {
                                        updateDefaultAddress(address, iRowIndex);
                                    });

                                    btn.getStyleClass().add("outline-button");
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                }
        );
    }


    //endregion

    //region UI - Update

    private void updateBalance() {
        String unit = _appContext.Configuration.getVbkUnit();

        if (_addressesModel == null) {
            this.lblTotalBalance.setText("-");
            //getTabAddressModel_start().getPayload().TotalBalance.set("-");
        } else {
            this.lblTotalBalance.setText(
                    String.format("%1$s %2$s",
                            VbkUtils.convertAtomicToVbkString(_addressesModel.getAddressSummary().getTotalSumAtomic()), unit)
            );
        }

    }

    private void updateGrid()
    {
        //Update balance
        updateBalance();

        _tableList = FXCollections.observableArrayList();
        List<AddressBalanceEntity> addresses = _addressesModel.getAddressSummary().getAddresses();

        boolean shouldHideZeroBalance = _addressesModel.hideZeroBalanceAddresses;

        for (int i = 0, addressesSize = addresses.size(); i < addressesSize; i++) {
            AddressBalanceEntity e = addresses.get(i);

            //check if we should add
            if (shouldHideZeroBalance)
            {
                if (e.getAmountPendingAtomic() + e.getAmountConfirmedAtomic() == 0)
                {
                    //totally empty, don't add it
                    continue;
                }
            }

            MyAddressRow et = new MyAddressRow();
            et.setAddress(e.getAddress());
            et.setAmountConfirmedAtomic(e.getAmountConfirmedAtomic());
            et.setAmountPendingAtomic(e.getAmountPendingAtomic());
            et.setIsDefault(e.getIsDefault());
            et.setNickNameTextField(nickNameTextField(e.getNickName(), et));
            _tableList.add(et);
        }

        mainGrid.setItems(_tableList);
    }

    private TextField nickNameTextField(String value, MyAddressRow idx){
        TextField textField = new TextField(value);
        textField.setPromptText(_localeModule.getString("column.nickName.promptType"));
        int textFieldWidth = 200; //allow for 15 char
        textField.setMaxWidth(textFieldWidth);
        textField.setUserData(idx);
        ControlHelper.setTextFieldProperties(textField, ControlHelper.MaskType.None, 15);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                final MyAddressRow selectedItem = (MyAddressRow) textField.getUserData();
                selectedItem.getNickNameTextField().setText(newValue);
                selectedItem.setNickName(newValue); // This wont be neccessary if we modify how data is converted from GSON
            }
            catch (Exception ex)
            {
                //swallow rather than corrupt UI
                //Example: undo not hooked up
            }
        });

        textField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                {
                    //System.out.println("----> LEAVE GRID, DO SAVE HERE");
                    final MyAddressRow selectedItem = (MyAddressRow) textField.getUserData();
                    //System.out.println(String.format("Address=%1$s, NewValue=%2$s", selectedItem.getAddress(),
                    //        selectedItem.getNickName() ));
                    updatePersistNicknames(selectedItem);
                }
            }
        });


        return textField;
    }

    private void updatePersistNicknames(MyAddressRow selectedItem)
    {
        //NOTE: this rewrites all of userSettings, so it is not high performance
        //But given how small userSettings is, this is ok for alpha release

        //persist to userSettings if different
        String address = selectedItem.getAddress();

        //Performance optimization: onyl persist if changed
        //Get value from the textfield itself, it's more reliable. Something sometimes fishy if user clicks rapidly where the binding hasn't happened
        //  yet, and selectedItem.getNickName() could return null
        String nicknameNew = selectedItem.getNickNameTextField().getText();
                //selectedItem.getNickName();
        if (nicknameNew == null)
        {
            nicknameNew = "";
        }
        String nicknameOld = _addressesModel.getNicknamesOriginal().getOrDefault(address, null);
        if (nicknameOld == null)
        {
            nicknameOld = "";
        }

        if (nicknameNew.equals(nicknameOld))
        {
            //no change, nothing to persist
        }
        else
        {
            //change!
            OrmLiteAddressRepository db = new OrmLiteAddressRepository();
            db.save(address, nicknameNew);

            _appContext.UIManager.setStatusMessageSucess(
                    String.format(_localeModule.getString("status.nickname.updateSuccess"), address, nicknameNew));

            _logger.info("Updated address={} nickname from='{}' to='{}'", address, nicknameOld, nicknameNew);
        }

    }

    private void updateIfCached()
    {
        //if cached, then update
        //disable the make_default buttons
        //disable createNewAddress button

        boolean isCached = _addressesModel.getIsCached();
        if (isCached)
        {
            this.btnCreateNew.setDisable(true);
        }
    }

    private void refreshPage() {

        _logger.info("refreshPage");
        loadAddressData_start();
    }

    public void onBalanceChanged(boolean didChange, long oldAmount, long newAmount)
    {
        _logger.info("Call onBalanceChanged");
        refreshPage();
    }

    public void onCurrentBlockChanged(int oldBlockHeight, int newBlockHeight)
    {
        //on every block change, we're checking for updatebalance, so let the user know it is updated
        refreshPage_timestamp();
    }

    public void clickTestRefresh()
    {
        //_appContext.UIManager.setStatusMessage("Did refresh");
        refreshPage();
    }
    public Label lblRefreshTime;
    private void refreshPage_timestamp()
    {
        this.lblRefreshTime.setText(_localeModule.getString("refreshLabel_update") +
                " " + Utils.getTimeOfDayNow());
    }

    /*
    //TODO --> pull out to GridFormatHelper for reuse?
    private static void setCellVbkFormatting2(TableCell cell, long amount, boolean empty, Label label) {
        if (empty) {
            cell.setGraphic(null);
            cell.setText(null);
        }
        else
        {
            label.setText(VbkUtils.convertAtomicToVbkString(amount));
            if (amount > 0) {
                ControlHelper.setStyleClass(label, "cellPositiveVbk");
            }
            else if (amount < 0)
            {
                ControlHelper.setStyleClass(label, "cellNegativeVbk");
            }
            else
            {
                ControlHelper.setStyleClass(label, "cellDefault");
            }

            cell.setGraphic(label);
            cell.setText(null);
        }
    }
    */

    private void setCellButtonDefault(Button btn, boolean isDefault)
    {
        if (isDefault) {
            btn.setDisable(true);
            btn.setText(_localeModule.getString("column.isDefault.buttonCurrent"));
        }
        else {
            btn.setDisable(false);
            btn.setText(_localeModule.getString("column.isDefault.buttonMakeDefault"));
        }

        //Keep text, Override disable
        boolean isCached = _addressesModel.getIsCached();
        if (isCached)
        {
            //just disable and exist
            btn.setDisable(true);
            return;
        }
    }

    private void updateDefaultAddress(String newAddress, int iRowIndex) {
        SoundItem.playButtonClick();

        boolean shouldContinue = ControlHelper.showAlertYesNoDialog(String.format(
                _localeModule.getString("updateDefaultAddress.areYouSure"), newAddress));
        if (!shouldContinue) {
            String s = String.format(_localeModule.getString("updateDefaultAddress.cancelled"));
            _appContext.UIManager.setStatusMessage(s);
            return;
        }

        //Set everything to false
        int iCount = mainGrid.getItems().size();
        for (int i = 0; i < iCount; i++) {
            MyAddressRow row2 = mainGrid.getItems().get(i);
            row2.setIsDefault(false);
            mainGrid.getItems().set(i, row2);
        }

        //set selected row
        MyAddressRow row = mainGrid.getItems().get(iRowIndex);
        row.setIsDefault(true);
        mainGrid.getItems().set(iRowIndex, row);

        //----------- ASYNC --------------
        //call GRPC
        CommandResult<Void> commandResult = NodeCoreGateway.getInstance().setDefaultAddress(newAddress);

        //Update UI
        if (commandResult.isSuccess()) {
            String s2 = String.format(_localeModule.getString("updateDefaultAddress.success"), newAddress);
            _appContext.UIManager.setStatusMessage(new ValidationInfo(Success, s2));
        } else {
            _appContext.UIManager.setStatusMessageCommandError(commandResult);
        }


    }

    //endregion

    //NOTE - explicitly went this way instead of setCellFactory, as that wasn't binding, sorting, handling scrolling-off, and allowing us to add custom control events. There could be a way...
    public class MyAddressRow extends AddressBalanceEntity implements Exportable
    {
        private TextField _nickName;
        public TextField getNickNameTextField() {
            return _nickName;
        }
        public void setNickNameTextField(TextField nickName) {
            this._nickName = nickName;
        }

        public String createCsvRow(boolean isHeader) {
            if (isHeader) {
                return String.format("%1$s,%2$s,%3$s,%4$s",
                        "Address", "Amount_Confirmed",
                        "NickName", "IsDefault");
            } else {
                //return data here
                return String.format("%1$s,%2$s,%3$s,%4$s",
                        this.getAddress(),
                        VbkUtils.convertAtomicToVbkString(this.getAmountConfirmedAtomic()),
                        this.getNickName(),
                        this.getIsDefault());
            }
        }
    }

    //region Hide Zero balances

    public void clickHideZeroBalance()
    {
        boolean shouldHide = this.chkHideZeroBalance.isSelected();
        _logger.info("clickHideZeroBalance. ShouldHide={}", shouldHide);

        updateUIHideZeroBalances(shouldHide);
    }

    private void updateUIHideZeroBalances(boolean shouldHide) {
        _addressesModel.hideZeroBalanceAddresses = shouldHide;
        updateGrid();

        //Persist to settings
        UserSettings.saveJson(SettingsConstants.MYADDRESSES_HIDE_ZERO_BALANCE, shouldHide);

        //Update UI to reflect what is passed in
        boolean shouldHideCurrent = this.chkHideZeroBalance.isSelected();
        if (shouldHideCurrent != shouldHide) {
            this.chkHideZeroBalance.setSelected(shouldHide);
        }
    }

    //endregion

    public void clickExport() {
        List items = mainGrid.getItems();
        ValidationInfo vi = ShellService.doExport(items);
        _appContext.UIManager.setStatusMessage(vi);
    }

}

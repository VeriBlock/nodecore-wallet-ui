// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import javafx.scene.control.*;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.entities.AddressBalanceEntity;
import veriblock.wallet.entities.PendingTransactionEntity;
import veriblock.wallet.entities.SendAmountEntity;
import veriblock.wallet.entities.SendInput;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.FormatHelper;
import org.veriblock.core.utilities.AddressUtility;

import java.util.ArrayList;
import java.util.List;

public class TabSendController  extends BaseController  {

    private static final Logger _logger = LoggerFactory.getLogger(TabSendController.class);

    public void initialize() {
        //btnSend.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.SEND).color(Color.valueOf("#0ab45b")));
    }

    public void init() {

        this.btnDrainAddress.setVisible(false);

        //setup
        ControlHelper.setTextFieldProperties(this.txtFeePerByte, ControlHelper.MaskType.VbkAmountPositive);
        ControlHelper.setTextFieldProperties(this.txtSendTo, ControlHelper.MaskType.VbkAddress);
        ControlHelper.setTextFieldProperties(this.txtAmount, ControlHelper.MaskType.VbkAmountPositive);

        lblUnitsFee.setText(_appContext.Configuration.getVbkUnit());
        lblUnitsAmount.setText(_appContext.Configuration.getVbkUnit());

        setupModel();
        setLocale();

        setupAddressDropdown();
        loadData_start();

    }

    private LocaleModule _localeModule;
    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabSend);
        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));

        lblSendFrom.setText(_localeModule.getString("label.sendFrom"));
        lblSendToAddress.setText(_localeModule.getString("label.sendToAddress"));
        lblFeePerByte.setText(_localeModule.getString("label.feePerByte"));
        lblFeePerByteToolTip.setText(_localeModule.getString("label.feePerByte.toolTip"));
        lblAmount.setText(_localeModule.getString("label.amount"));
        btnSend.setText(_localeModule.getString("button.send.text"));

        btnDrainAddress.setText(_localeModule.getString("button_drain_text"));

    }

    //region Model - setup and update

    private TabSendModel _tabSendModel;
    private TabSendModel getTabSendModel()
    {
        return _tabSendModel;
    }

    private void setupModel() {

        _tabSendModel = new TabSendModel();

        _tabSendModel.feeperbyte_warning_belowthreshold = _appContext.Configuration.getSendFeeperbyteWarningBelowThreshold();
        _tabSendModel.feeperbyte_warning_abovethreshold = _appContext.Configuration.getSendFeeperbyteWarningAboveThreshold();
    }

    private String getTargetAddress()
    {
        return  this.txtSendTo.getText();
    }

    private String getFromAddress()
    {
        AddressBalanceEntity row = this.ddAddressFrom.getValue();
        if (row == null)
        {
            return  null;
        }
        return this.ddAddressFrom.getValue().getAddress();
    }

    private long getAmount()
    {
        return VbkUtils.convertDecimalCoinToAtomicLong(this.txtAmount.getText());
    }

    private long getTxtFeePerByte()
    {
        return VbkUtils.convertDecimalCoinToAtomicLong(this.txtFeePerByte.getText());
    }

    //endregion

    //region UI - Update

    //endregion

    //region UI - Setup

    //public ChoiceBox<FeeOption> DdFee;
    public Label lblValue;
    public TextField txtSendTo;
    public TextField txtAmount;
    public TextField txtFeePerByte;

    public Label lblUnitsFee;
    public Label lblUnitsAmount;

    public ChoiceBox<AddressBalanceEntity> ddAddressFrom;
    public Button btnSend;

    public Label lblSendFrom;
    public Label lblSendToAddress;
    public Label lblFeePerByte;
    public Tooltip lblFeePerByteToolTip;
    public Label lblAmount;

    public Button btnDrainAddress;

    private void setupAddressDropdown() {
        CommandResult<AddressSummary> commandResult = WalletService.getAddressSummary();
        if (!commandResult.isSuccess()) {
            //bad, should not happen by the time this can be called
            return;
        }

        AddressSummary domainModel = commandResult.getPayload();
        List<AddressBalanceEntity> addresses = domainModel.getAddresses();
        WalletService.setupModelMergeNickNames(addresses);

        //filter to only have positive value
        List<AddressBalanceEntity> addresses2 = new ArrayList<>();
        for(AddressBalanceEntity ae : addresses)
        {
            if (ae.getAmountConfirmedAtomic() + ae.getAmountPendingAtomic() > 0)
            {
                addresses2.add(ae);
            }
        }

        ddAddressFrom.getItems().addAll(addresses2);
        //Initial value is to skip any address
        ddAddressFrom.getItems().add(0, null);

        //Set - Assume default address
        //TODO --> abstract more to resuable util/walletService

        String selectedAddress = UserSettings.getValue(SettingsConstants.SEND_SELECTEDADDRESS);

        if (selectedAddress == null)
        {
            ddAddressFrom.getSelectionModel().selectFirst();
        }
        else if (selectedAddress.equals(NO_ADDRESS_SELECTED))
        {
            ddAddressFrom.getSelectionModel().selectFirst();
        }
        else
        {
            FormatHelper.setAddressDropdownByValue(ddAddressFrom, selectedAddress);
        }

        FormatHelper.formatAddressDropdown(ddAddressFrom, _appContext.Configuration.getVbkUnit(),
                _localeModule.getString("dropdown.fromAddress.leySystemChoose"));

        ddAddressFrom.valueProperty().addListener((obs, oldval, newval) -> {
            String address = null;
            if (newval == null)
            {
                address= NO_ADDRESS_SELECTED;
            }
            else
            {
                address = newval.getAddress();
            }
            //Persist to settings
            UserSettings.save(SettingsConstants.SEND_SELECTEDADDRESS, address);
        });
    }
    private final String NO_ADDRESS_SELECTED = "NoAddressSelected";

    //endregion

    public void clickSend()
    {
        boolean shouldDrain = false;
        sendCoins(shouldDrain);
    }

    /*
    Returns true if should still send
     */
    private boolean sendCoins_preValidate(boolean shouldDrain, SendInput sendInput)
    {
        boolean shouldSend = false;

        //Validate input
        long minThresholdAtomic = _appContext.Configuration.getSendFeeperbyteMinimum();
        if (sendInput.txFeePerByte < minThresholdAtomic)
        {
            _appContext.UIManager.setStatusMessage(new ValidationInfo(ValidationInfo.Status.Error,
                    String.format(_localeModule.getString("send.validation.feeTooLow"),
                            VbkUtils.convertAtomicToVbkString(minThresholdAtomic))));
            return false;
        }
        if (!AddressUtility.isValidStandardAddress(sendInput.targetAddress)
                && !AddressUtility.isValidMultisigAddress(sendInput.targetAddress)
                )
        {
            _appContext.UIManager.setStatusMessage(new ValidationInfo(ValidationInfo.Status.Error, _localeModule.getString("send.validation.addressValid")));
            return false;
        }

        if (shouldDrain)
        {
            //CASE: Validation for Drain only

            //ignore send sentAmount, as we'll send the whole balance
        }
        else
        {
            //CASE: Validation for Send only
            if (sendInput.sentAmount <= 0)
            {
                _appContext.UIManager.setStatusMessage(new ValidationInfo(ValidationInfo.Status.Error, _localeModule.getString("send.validation.ammountPositive")));
                return false;
            }
        }

        //Check fees for both send/drain
        ValidationInfo vi = getTabSendModel().validateSendInputFees(sendInput);
        if (vi.isWarning())
        {
            shouldSend = ControlHelper.showAlertYesNoDialog(vi);
            if (!shouldSend)
            {
                _appContext.UIManager.setStatusMessage(_localeModule.getString("send.validation.cancel"));
                return false;
            }
        }

        if (shouldDrain) {
            //prompt if they want to drain
            //"This will drain all coins from %1$s to %2$s using the fee per byte of %3$s.\nThe resulting balance for %1$s will be 0.00000000 %4$s.\nDo you want to continue?"
            String message = _localeModule.getString("drain_warning");
            shouldSend = ControlHelper.showAlertYesNoDialog(
                String.format(message,
                        sendInput.fromAddress,
                        sendInput.targetAddress,
                        VbkUtils.convertAtomicToVbkString(sendInput.txFeePerByte),
                        _appContext.Configuration.getVbkUnit()));
        }
        else {
            //Final confirm --> are you sure?
            shouldSend = ControlHelper.showAlertYesNoDialog(
                    String.format(_localeModule.getString("send.validation.areYouSure2"),
                            VbkUtils.convertAtomicToVbkString(sendInput.sentAmount),
                            _appContext.Configuration.getVbkUnit()));
        }

        return shouldSend;
    }

    private void sendCoins(boolean shouldDrain)
    {
        SendInput sendInput = new SendInput();
        sendInput.fromAddress = getFromAddress();
        sendInput.sentAmount = getAmount();
        sendInput.targetAddress = getTargetAddress();
        sendInput.txFeePerByte = getTxtFeePerByte();

        boolean shouldSend = sendCoins_preValidate(shouldDrain, sendInput);

        if (shouldSend)
        {
            //great, proceed!
        }
        else
        {
            _appContext.UIManager.setStatusMessage(_localeModule.getString("send.validation.cancel"));
            return;
        }

        NodeCoreGateway ngw = NodeCoreGateway.getInstance();

        //prompt for unlock
        boolean successfullyUnlocked = WalletService.promptUnlockWallet(_appContext, ngw);
        if (!successfullyUnlocked)
        {
            LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.WalletService);
            _appContext.UIManager.setStatusMessageWarning(lm.getString("walletService_passwordCancelled"));
            return;
        }

        //update the fee by calling settxfee
        ngw.setTxFeePerByte(sendInput.txFeePerByte);

        boolean didSend = false;
        if (shouldDrain)
        {
            didSend = send_drain(sendInput);
        }
        else
        {
            didSend = send_amount(sendInput);
        }

        if (didSend) {
            SoundItem.playSend();
        }
    }

    private boolean send_drain(SendInput sendInput) {

        boolean didSend = false;
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();

        //We expect this to be < 1 second, and act sync (user won't navigate away)...
        CommandResult<PendingTransactionEntity> commandResult = ngw.drainAddress(
                sendInput.fromAddress, sendInput.targetAddress);

        //Display results
        if (commandResult.isSuccess()) {
            //SUCCESS!
            didSend = true;

            PendingTransactionEntity transaction = commandResult.getPayload();

            //override from result of drain:
            sendInput.sentAmount = transaction.sourceAmountAtomic - transaction.totalFeeAtomic;

            SendAmountEntity sentOutput = new SendAmountEntity();
            sentOutput.input = sendInput;
            List<String> lstr = new ArrayList<>();
            lstr.add(transaction.txId);
            sentOutput.setOutputTxIdList(lstr);

            Long totalFees = transaction.totalFeeAtomic;
            int totalBytes = transaction.sizeBytes;

            send_displaySuccess(totalFees, totalBytes, sentOutput);

            //refresh dropdown list to remove this now-zero address
            //TODO

        } else {
            String suffix = "";
            if (commandResult.isErrorWalletLocked()) {
                suffix = " " + _localeModule.getString("general.badPassword");
            }
            _appContext.UIManager.setStatusMessageCommandError(commandResult,
                    _localeModule.getString("send.validation.errorSending") + suffix);
        }

        return didSend;
    }

    private boolean send_amount(SendInput sendInput)
    {
        boolean didSend = false;
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();

        //We expect this to be < 1 second, and act sync (user won't navigate away)...
        CommandResult<SendAmountEntity> commandResult = ngw.sendAmount(sendInput);

        //Display results
        if (commandResult.isSuccess())
        {
            //SUCCESS!
            didSend = true;

            SendAmountEntity sentOutput = commandResult.getPayload();

            if (sentOutput.getOutputTxIdList().size() == 0) {
                //  Very strange, this should NOT happen
                String s2 = String.format(_localeModule.getString("send.validation.sentButNoId"),
                        VbkUtils.convertAtomicToVbkString(sentOutput.input.sentAmount),
                        sentOutput.input.targetAddress);
                _appContext.UIManager.setStatusMessage(new ValidationInfo(ValidationInfo.Status.Success, s2));
            }
            else {
                //Most likely case here:

                Pair<Long, Integer> feeResults = successGetFeeDetails(sentOutput.getOutputTxIdList());
                Long totalFees = feeResults.getKey();
                int totalBytes = feeResults.getValue();

                send_displaySuccess(totalFees, totalBytes, sentOutput);
            }

        }
        else
        {
            String suffix = "";
            if (commandResult.isErrorWalletLocked())
            {
                suffix = " " + _localeModule.getString("general.badPassword");
            }
            _appContext.UIManager.setStatusMessageCommandError(commandResult,
                    _localeModule.getString("send.validation.errorSending") + suffix);
        }

        return didSend;
    }

    private Pair<Long, Integer> successGetFeeDetails(List<String> txInputs)
    {
        CommandResult<List<PendingTransactionEntity>> result = NodeCoreGateway.getInstance().getPendingTransaction(txInputs);
        long sum = 0;
        int bytesSize = 0;
        if (result.isSuccess())
        {
            if (txInputs.size() != result.getPayload().size() || result.getPayload().size() == 0)
            {
                //bad - they should match 1:1.
                //TODO --> optimization --> do TX lookup
                return null;
            }

            //expected
            for(PendingTransactionEntity entity : result.getPayload())
            {
                bytesSize = bytesSize + entity.sizeBytes;
                sum = sum + entity.totalFeeAtomic;
            }

            //YAY!
            return new Pair<Long, Integer>(sum, bytesSize);
        }

        //Should never happen, send back a placeholder
        return null;
    }


    private void send_displaySuccess(Long totalFees, int totalBytes, SendAmountEntity sentOutput )
    {
        String totalFeesString = "--";
        String totalSourceAmountString = "--";
        if (totalFees != null) {
            totalFeesString = VbkUtils.convertAtomicToVbkString(totalFees);
            totalSourceAmountString = VbkUtils.convertAtomicToVbkString(totalFees + sentOutput.input.sentAmount);
        }


        //Show (1) simple status message and (2) detailed confirmation
        //Short summary for status
        String sfinal = String.format(_localeModule.getString("send.validation.successSummary"),
                VbkUtils.convertAtomicToVbkString(sentOutput.input.sentAmount),
                sentOutput.input.targetAddress,
                sentOutput.getOutputTxIdList().size(),
                sentOutput.getOutputTxList(", "),

                //add in total transaction fees
                totalFeesString,
                _appContext.Configuration.getVbkUnit(),
                totalSourceAmountString
        );
        _appContext.UIManager.setStatusMessage(new ValidationInfo(ValidationInfo.Status.Success, sfinal));

        //Longer detail for popup
        //TT_Sent %1$s %6$s to address %2$s, resulting in %3$s transaction(s) with total fees of %5$s %6$s.
        // Total Source Amount: %7$s %6$s. Transaction(s): %4$s
        ValidationInfo viDialog = new ValidationInfo();
        viDialog.setMessageSuccess(_localeModule.getString("send.validation.successDetail1"));
        String successText = String.format(_localeModule.getString("send.validation.successDetail2"),
                VbkUtils.convertAtomicToVbkString(sentOutput.input.sentAmount),
                sentOutput.input.targetAddress,
                sentOutput.getOutputTxIdList().size(),
                sentOutput.getOutputTxList("\n"),

                //add in total transaction fees
                totalFeesString,
                _appContext.Configuration.getVbkUnit(),
                totalSourceAmountString,
                sentOutput.input.fromAddress,
                totalBytes,
                VbkUtils.convertAtomicToVbkString(sentOutput.input.txFeePerByte)

        );
        ControlHelper.showConfirmDialog(viDialog, successText);
    }

    private void loadData_start() {

        //Pull free from getinfo
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();
        long fee = ngw.getInfo().getPayload().transactionFeePerByte;
        this.txtFeePerByte.setText(VbkUtils.convertAtomicToVbkString(fee));

        //Default an sentAmount
        this.txtAmount.setText(
                VbkUtils.convertAtomicToVbkString(
                        VbkUtils.convertDecimalCoinToAtomicLong("1")));
        //----
        /*
        FeeOption f1 = new FeeOption("Low", 10.5);
        FeeOption f2 = new FeeOption("Medium", 23.5);
        FeeOption f3 = new FeeOption("High", 99);

        this.DdFee.getItems().add(f1);
        this.DdFee.getItems().add(f2);
        this.DdFee.getItems().add(f3);

        //TODO --> hook up on change
        this.DdFee.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("skhgbksjdhjshdjhsdhsdhklsjd");
            }
        });
        */

    }

    public void clickTest()
    {
        String txId = "F76861EF911C11C4A7AF31DED66298B2B4962B2116FB1D912F26A66E5163AF00";
        List<String> inputs = new ArrayList<>();
        inputs.add(txId);
        CommandResult<List<PendingTransactionEntity>> result = NodeCoreGateway.getInstance().getPendingTransaction(inputs);
        if (result.isSuccess())
        {
            PendingTransactionEntity entity = result.getPayload().get(0);
            int i = 0;
        }
    }

    public void clickDrain()
    {
        _logger.info("clickDrain");

        boolean shouldDrain = true;
        sendCoins(shouldDrain);

        //Similar to send command:
        //  Unlock wallet if needed
        //  Update TX fee
        //  Check for failure (such as no pending TX)

    }

    //region FUTURE CODE

    /*
    TODO --> too involved, too many types only in NodeCore. Punt to it's own GRPC command
    public void clickSetMaxAmount()
    {
        //from admin service: public void sendCoins(

        long txFee = NodeCoreGateway.KILL_convertVbkToAtomic(this.getTxtFeePerByte());
        long feeFudgeFactor = txFee * 500;

        int predictedTransactionSize =
                predictStandardTransactionToAllStandardOutputSize(
                        totalOutputAmount + feeFudgeFactor,
                        outputList,
                        _pendingTransactionContainer
                                .getPendingSignatureIndexForAddress(requestedSourceAddress) + 1, 0);
        long fee = predictedTransactionSize * txFee;
    }

    */
    /*
    //TODO --> pull to UTIL COMMON, from package nodecore.core.transactions;
    public static int predictStandardTransactionToAllStandardOutputSize(long inputAmount,
             List<Output> outputs, long sigIndex, int extraDataLength) {
        int totalSize = 0;
        totalSize += 1; // Transaction Version
        totalSize += 1; // Type of Input Address
        totalSize += 1; // Standard Input Address Length Byte
        totalSize += 22; // Standard Input Address Length

        byte[] inputAmountBytes = Utility.trimmedByteArrayFromLong(inputAmount);

        totalSize += 1; // Input Amount Length Byte
        totalSize += inputAmountBytes.length; // Input Amount Length

        totalSize += 1; // Number of Outputs

        for (int i = 0; i < outputs.size(); i++) {
            totalSize += 1; // ID of Output Address
            totalSize += 1; // Output Address Length Bytes
            totalSize += 22; // Output Address Length

            byte[] outputAmount = Utility.trimmedByteArrayFromLong(outputs.get(i).getAmount().value());
            totalSize += 1; // Output Amount Length Bytes
            totalSize += outputAmount.length; // Output Amount Length
        }

        byte[] sigIndexBytes = Utility.trimmedByteArrayFromLong(sigIndex);
        totalSize += 1; // Sig Index Length Bytes
        totalSize += sigIndexBytes.length; // Sig Index Bytes

        byte[] dataLengthBytes = Utility.trimmedByteArrayFromInteger(extraDataLength);
        totalSize += 1; // Data Length Bytes Length
        totalSize += dataLengthBytes.length; // Data Length Bytes
        totalSize += extraDataLength; // Extra data section

        return totalSize;
    }
    */

    /*
    public void onFeeChange()
    {

        //get value
        FeeOption item = (FeeOption) DdFee.getValue();

        String s = String.format("New value: %1$s, has fee: %2$s", item.Text, item.Value);

        this.LblValue.setText(s);
    }

    public class FeeOption
    {
        public FeeOption(String text, double value)
        {
            this.Text = text;
            this.Value = value;
        }
        public String Text;
        public double Value;

        @Override
        public String toString(){
            return String.format("%1$s (%2$s)", this.Text, this.Value);
        }
    }
    */

    //endregion
}

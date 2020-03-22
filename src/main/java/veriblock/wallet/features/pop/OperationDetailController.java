// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.pop;

import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.pop.*;
import veriblock.wallet.core.pop.entities.OperationDetailEntity;
import veriblock.wallet.core.pop.entities.OperationEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.IntegrationLinks;

public class OperationDetailController extends DialogController {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectPoPController.class);
    public void initialize() {
    }

    private LocaleModule _localeModule;

    public void init() {

        if (_navigationData.getData() == null)
        {
            //bad! Should never be called
            return;
        }

        setLocale();

        OperationEntity inputData = (OperationEntity)_navigationData.getData();
        operationId = inputData.operationId;
        actionMessage = inputData.actionMessage;
        detailMessage = inputData.message;

        //operationId = _
        //set current props
        _clientProxy = PopService.getApiProxy();
        if (_clientProxy == null) {
            //should not happen
            return;
        }

        setupUI();
        setupModel();

        applyModelToUI();
    }

    private String operationId;
    private String actionMessage;
    private String detailMessage;

    private ApiProxy _clientProxy;

    public TextField lblOpId;
    public Hyperlink hlnkVbkTxId;
    public Hyperlink hlnkBtcTxId;
    public TextField lblVbkAddress;
    public TextArea lblOpReturnData;
    public Label lblCurrentAction;

    public Pane workflow1;
    public Pane workflow2;
    public Pane workflow3;
    public Pane workflow4;
    public Pane workflow5;
    public Pane workflow6;
    public Pane workflow7;
    public Pane workflow8;
    public Pane workflow9;

    private OperationDetailEntity opDetailModel;

    private void setupUI()
    {
        //simply get fields and show, no tricks

        ControlHelper.setLabelCopyProperties(this.lblVbkAddress, true);
        ControlHelper.setLabelCopyProperties(this.lblOpId, true);
        ControlHelper.setLabelCopyProperties(this.lblOpReturnData, true);

        ControlHelper.setToolTip(this.hlnkVbkTxId, _localeModule.getString("OperationDetail_hlnk_vbk_tooltip"));
        ControlHelper.setToolTip(this.hlnkBtcTxId, _localeModule.getString("OperationDetail_hlnk_btc_tooltip"));

    }

    private void setupModel()
    {
        opDetailModel = _clientProxy.getOperation(operationId);

    }

    private void applyModelToUI()
    {
        this.lblOpId.setText(opDetailModel.operationId);
        this.lblVbkAddress.setText(opDetailModel.vbkMinerAddress);
        this.lblOpReturnData.setText(opDetailModel.opReturnHex);

        if (opDetailModel.vbkPopTransactionId == null || opDetailModel.vbkPopTransactionId.length() == 0) {
            this.hlnkVbkTxId.setText(_localeModule.getString("OperationDetail_status_noVbkTx"));
            this.hlnkVbkTxId.setDisable(true);
        }
        else
        {
            this.hlnkVbkTxId.setText(opDetailModel.vbkPopTransactionId);
        }

        if (opDetailModel.btcTransactionId == null || opDetailModel.btcTransactionId.length() == 0) {
            this.hlnkBtcTxId.setText(_localeModule.getString("OperationDetail_status_noBtcTx"));
            this.hlnkBtcTxId.setDisable(true);
        }
        else
        {
            this.hlnkBtcTxId.setText(opDetailModel.btcTransactionId);
        }

        this.lblCurrentAction.setText(actionMessage);
        this.lblMessage.setText(detailMessage);

        setWorkflowUI();
    }

    public Label lblWrkflow1;
    public Label lblWrkflow2;
    public Label lblWrkflow3;
    public Label lblWrkflow4;
    public Label lblWrkflow5;
    public Label lblWrkflow6;
    public Label lblWrkflow7;
    public Label lblWrkflow8;
    public Label lblWrkflow9;

    public Label lblFieldOpId;
    public Label lblFieldVbkTxId;
    public Label lblFieldBtcTxId;
    public Label lblFieldMinerAddress;
    public Label lblFieldOpReturnData;
    public Button btnClose;
    public Label lblMessage;
    public Label lblFieldMessage;

    public void setLocale()
    {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabPoP);

        lblWrkflow1.setText(_localeModule.getString("OperationDetail_label_workflow1"));
        lblWrkflow2.setText(_localeModule.getString("OperationDetail_label_workflow2"));
        lblWrkflow3.setText(_localeModule.getString("OperationDetail_label_workflow3"));
        lblWrkflow4.setText(_localeModule.getString("OperationDetail_label_workflow4"));
        lblWrkflow5.setText(_localeModule.getString("OperationDetail_label_workflow5"));
        lblWrkflow6.setText(_localeModule.getString("OperationDetail_label_workflow6"));
        lblWrkflow7.setText(_localeModule.getString("OperationDetail_label_workflow7"));
        lblWrkflow8.setText(_localeModule.getString("OperationDetail_label_workflow8"));
        lblWrkflow9.setText(_localeModule.getString("OperationDetail_label_workflow9"));

        lblFieldOpId.setText(_localeModule.getString("OperationDetail_label_opId"));
        lblFieldVbkTxId.setText(_localeModule.getString("OperationDetail_label_VbktxId"));
        lblFieldBtcTxId.setText(_localeModule.getString("OperationDetail_label_BtcTxId"));
        lblFieldMinerAddress.setText(_localeModule.getString("OperationDetail_label_minerAddress"));
        lblFieldOpReturnData.setText(_localeModule.getString("OperationDetail_label_opreturnData"));
        lblFieldMessage.setText(_localeModule.getString("OperationDetail_label_message"));

        btnClose.setText(_localeModule.getString("OperationDetail_button_close"));

    }


    public void clickClose()
    {
        SoundItem.playButtonClick();
        this.closeDialog(null);
    }

    public void clickVbkTxId()
    {
        if (opDetailModel.vbkPopTransactionId == null)
        {
            return;
        }
        IntegrationLinks links = IntegrationLinks.getInstance();
        String url = links.getVbkTransactionUrl(opDetailModel.vbkPopTransactionId);
        Utils.openLink(url);
    }

    public void clickBtcTxId()
    {
        if (opDetailModel.btcTransactionId == null)
        {
            return;
        }
        IntegrationLinks links = IntegrationLinks.getInstance();
        String url = links.getBtcTransactionUrl(opDetailModel.btcTransactionId);
        Utils.openLink(url);
    }

    public void setWorkflowUI() {
        //1 set current
        //2 set all past

        //TODO --> probably a way to put this in a loop and dynamically reference control by name

        int iWorkflowNum = opDetailModel.getWorkFlowNumber();
        Pane currentWorkflowPane = null;
        if (iWorkflowNum >= 1) {
            currentWorkflowPane = flipWorkflowOn(workflow1);
        }
        if (iWorkflowNum >= 2) {
            currentWorkflowPane = flipWorkflowOn(workflow2);
        }
        if (iWorkflowNum >= 3) {
            currentWorkflowPane = flipWorkflowOn(workflow3);
        }
        if (iWorkflowNum >= 4) {
            currentWorkflowPane = flipWorkflowOn(workflow4);
        }
        if (iWorkflowNum >= 5) {
            currentWorkflowPane = flipWorkflowOn(workflow5);
        }
        if (iWorkflowNum >= 6) {
            currentWorkflowPane = flipWorkflowOn(workflow6);
        }
        if (iWorkflowNum >= 7) {
            currentWorkflowPane = flipWorkflowOn(workflow7);
        }
        if (iWorkflowNum >= 8) {
            currentWorkflowPane = flipWorkflowOn(workflow8);
        }
        if (iWorkflowNum >= 9) {
            currentWorkflowPane = flipWorkflowOn(workflow9);
        }

        //set current style
        if (opDetailModel.status == PopStatus.FAILED)
        {
            ControlHelper.setStyleClass(currentWorkflowPane, "workflow-failed");
        }
        else {
            ControlHelper.setStyleClass(currentWorkflowPane, "workflow-current");
        }
    }

    private Pane flipWorkflowOn(Pane workflowPane)
    {
        ControlHelper.setStyleClass(workflowPane, "workflow-done");
        return workflowPane;
    }
}

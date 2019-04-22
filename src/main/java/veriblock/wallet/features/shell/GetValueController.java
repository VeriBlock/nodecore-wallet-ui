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

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.pop.*;
import veriblock.wallet.core.pop.entities.MinerPropertiesEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.uicommon.*;


public class GetValueController extends DialogController {

    private static final Logger _logger = LoggerFactory.getLogger(GetValueController.class);
    public void initialize() {
    }

    private LocaleModule _localeModule;

    public void init() {

        _input = (DialogGetValueInput)_navigationData.getData();

        setLocale();
        setupUI();
    }

    private DialogGetValueInput _input;

    public Button btnOk;
    public Button btnCancel;

    public Label lblError;
    public TextField txtValue;

    private void setupUI()
    {
        this.lblError.setVisible(false);

        this.txtValue.setPromptText(_input.valueEmptyPrompt);
        if (_input.initialValue != null && _input.initialValue.length() > 0) {
            this.txtValue.setText(_input.initialValue);
        }

        ControlHelper.setTextFieldProperties(this.txtValue, _input.dataMaskType);

    }

    public void setLocale() {

        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        this.btnOk.setText(_localeModule.getString("general.button.ok"));
        this.btnCancel.setText(_localeModule.getString("general.button.cancel"));
    }

    private boolean runValidation()
    {
        if (_input.validationFunc == null)
        {
            return true;
        }

        boolean shouldContinue = true;

        //CASES:
        //  SUCCESS|INFO --> show nothing
        //  ERROR --> show alert error, with only acknowledgement
        //  WARNING --> prompt

        String strInput = this.txtValue.getText();
        ValidationInfo vi = _input.validationFunc.doWork(strInput);

        if (vi.isError())
        {
            ControlHelper.showConfirmDialog(vi);
            shouldContinue = false;
        }
        else if (vi.isWarning())
        {
            shouldContinue = ControlHelper.showAlertYesNoDialog(vi);
        }

        return shouldContinue;
    }

    public void clickOk()
    {
        SoundItem.playButtonClick();

        boolean shouldContinue = runValidation();
        if (!shouldContinue)
        {
            return;
        }

        String result = this.txtValue.getText();
        this.closeDialog(result);
    }

    public void clickCancel()
    {
        SoundItem.playButtonClick();
        this.closeDialog(null);
    }

    //endregion

}

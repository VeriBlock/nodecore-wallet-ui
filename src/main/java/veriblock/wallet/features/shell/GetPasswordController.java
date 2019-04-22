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
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.FormatHelper;
import veriblock.wallet.uicommon.GetPasswordInput;
import veriblock.wallet.uicommon.IntegrationLinks;


public class GetPasswordController extends DialogController {

    private static final Logger _logger = LoggerFactory.getLogger(GetPasswordController.class);
    public void initialize() {
    }

    private LocaleModule _localeModule;

    public void init() {

        _input = (GetPasswordInput)_navigationData.getData();

        setLocale();
        setupUI();
    }

    private GetPasswordInput _input;

    public GridPane mainGrid;

    public Label lblPassword;
    public PasswordField password1;
    public Button btnTest;

    public RowConstraints rowConfirm;
    public Label lblConfirm;
    public PasswordField confirmPassword1;

    public RowConstraints rowShouldRemember;
    public Label lblShouldRemember;
    public CheckBox chkShouldRemember;

    public Button btnClose;
    public Button btnCancel;

    private void setupUI()
    {

        if (_input.testFunc == null)
        {
            this.btnTest.setVisible(false);
        }

        //hide optional rows
        if (!_input.createNewPassword)
        {
            //Must set rowHeight to 0 and hide all children
            this.rowConfirm.setMaxHeight(0);
            this.lblConfirm.setVisible(false);
            this.confirmPassword1.setVisible(false);
        }

        if (!_input.showShouldRemember)
        {
            this.rowShouldRemember.setMaxHeight(0);
            this.lblShouldRemember.setVisible(false);
            this.chkShouldRemember.setVisible(false);
        }

    }

    public void setLocale() {

        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        this.lblPassword.setText(_localeModule.getString("general.password.label"));
        this.lblConfirm.setText(_localeModule.getString("general.password.labelConfirm"));
        this.lblShouldRemember.setText(_localeModule.getString("general.password.shouldRemember"));

        this.btnTest.setText(_localeModule.getString("general.password.header"));
        this.btnClose.setText(_localeModule.getString("general.button.ok"));
        this.btnCancel.setText(_localeModule.getString("general.button.cancel"));
    }

    private void showPopupIfError(ValidationInfo vi, boolean onlyShowIfError) {
        if (onlyShowIfError && vi.isSuccess()) {
            return;
        }
        ControlHelper.showConfirmDialog(vi);
    }

    /*
    Return true if valid, else false
     */
    private boolean testPassword(boolean onlyShowIfError)
    {
        //Call method, expect return success = null, else error

        ValidationInfo vi = new ValidationInfo();

        //confirm that passwords are equal
        String strPassword = this.password1.getText();
        if (strPassword == null || strPassword.length() == 0)
        {
            vi.setMessageError(_localeModule.getString("general.password.validation.empty"));
            showPopupIfError(vi, onlyShowIfError);
            return false;
        }

        //CASE: creating a new password
        if (_input.createNewPassword)
        {
            String strPasswordConfirm = this.confirmPassword1.getText();
            if (strPasswordConfirm == null || strPasswordConfirm.length() == 0)
            {
                vi.setMessageError(_localeModule.getString("general.password.validation.confirmEmpty"));
                showPopupIfError(vi, onlyShowIfError);
                return false;
            }

            if (!strPasswordConfirm.equals(strPassword))
            {
                //bad
                vi.setMessageError(_localeModule.getString("general.password.validation.noMatch"));
                showPopupIfError(vi, onlyShowIfError);
                return false;
            }

            //check password length
            int minPasswordLength = 8;
            if (strPassword.length() < minPasswordLength)
            {
                //bad
                vi.setMessageError(String.format(_localeModule.getString("general.password.validation.minLength"), minPasswordLength));
                showPopupIfError(vi, onlyShowIfError);
                return false;
            }

            //Passed everything!
            vi.setMessageSuccess(_localeModule.getString("general.password.validation.newSuccess"));
            showPopupIfError(vi, onlyShowIfError);
            return vi.isSuccess();
        }

        //CASE: entering an existing password

        //Test function provided
        if (_input.testFunc != null) {
            String result = _input.testFunc.doWork(this.password1.getText());
            if (result == null) {
                vi.setMessageSuccess(_localeModule.getString("general.password.correct"));
            } else {
                vi.setMessageError(result);
            }
            showPopupIfError(vi, onlyShowIfError);
        }

        return vi.isSuccess();
    }

    //region Buttons

    public void clickTestPassword()
    {
        SoundItem.playButtonClick();
        testPassword(false);    //test mode --> shows success as well
    }

    public void clickOK()
    {
        SoundItem.playButtonClick();

        //only allow closing if test passes
        boolean success = testPassword(true);   //success means we close the dialog
        if (success) {
            String strPassword = this.password1.getText();
            boolean fshouldRemember = chkShouldRemember.isSelected();

            Pair<String, Boolean> result = new Pair(strPassword, fshouldRemember);

            this.closeDialog(result);
        }
    }

    public void clickCancel()
    {
        SoundItem.playButtonClick();
        this.closeDialog(null);
    }

    //endregion

}

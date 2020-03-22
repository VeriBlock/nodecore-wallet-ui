// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public abstract class DialogController extends BaseController {


    protected Dialog<Object> _dialog;

    public void setDialog(Dialog<Object> dialog)
    {
        _dialog = dialog;
    }

    protected void closeDialog(Object result)
    {
        _dialog.setResultConverter(dialogButton -> {
            return result;
        });

        //Java FX hack in order to programatically close:
        //https://stackoverflow.com/questions/28698106/why-am-i-unable-to-programmatically-close-a-dialog-on-javafx?noredirect=1&lq=1
        // Add dummy cancel button
        try {
            //ensure button only added once. This control only addes cancel button, so it shoudl be zero.
            if ( _dialog.getDialogPane().getButtonTypes().size() == 0) {
                _dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
            }
        }
        catch (Exception ex)
        {
            //swallow
            int i = 0;
        }
        finally
        {
            this.dispose();
        }

        _dialog.close();
    }
}

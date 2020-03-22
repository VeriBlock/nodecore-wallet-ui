// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.wallet;

import veriblock.wallet.core.VbkUtils;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.ValidationInfo;
import veriblock.wallet.entities.SendInput;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.uicommon.FormatHelper;

public class TabSendModel {

    public TabSendModel()
    {

    }

    public long feeperbyte_warning_belowthreshold;
    public long feeperbyte_warning_abovethreshold;

    public ValidationInfo validateSendInputFees(SendInput sendInput) {
        ValidationInfo vi = new ValidationInfo();

        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.TabSend);

        //check fee
        if (sendInput.txFeePerByte < feeperbyte_warning_belowthreshold) {
            vi.setMessageWarning(String.format(lm.getString("send.validation.belowWarning"),
                    VbkUtils.convertAtomicToVbkString(feeperbyte_warning_belowthreshold)));
        }
        else
        {
            //check how much higher
            double feeMultiplier = (double)sendInput.txFeePerByte / (double)feeperbyte_warning_abovethreshold;
            int multiplierTooBig = 2;
            if (feeMultiplier > multiplierTooBig)
            {
                vi.setMessageWarning(String.format(lm.getString("send.validation.aboveVeryWarning"),
                        VbkUtils.convertAtomicToVbkString(sendInput.txFeePerByte),
                        VbkUtils.convertAtomicToVbkString(feeperbyte_warning_abovethreshold),
                        (int)feeMultiplier));
            }
            else if (sendInput.txFeePerByte > feeperbyte_warning_abovethreshold) {
                vi.setMessageWarning(String.format(lm.getString("send.validation.aboveWarning2"),
                        VbkUtils.convertAtomicToVbkString(sendInput.txFeePerByte),
                        VbkUtils.convertAtomicToVbkString(feeperbyte_warning_abovethreshold)));
            }
        }

        //NOTE --> cautious to check if the from address has sufficient fees, as ultimately that validation is with nodecore.
        //And it could try to pick other addresses to pull from.
        //TODO --> could add a warning

        return vi;
    }
}

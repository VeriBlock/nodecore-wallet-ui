// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.uicommon;

import veriblock.wallet.core.GenericFunction;
import veriblock.wallet.core.ValidationInfo;

public class DialogGetValueInput {
    public DialogGetValueInput()
    {

    }

    public String initialValue;

    public String message;

    public String valueEmptyPrompt;

    public ControlHelper.MaskType dataMaskType;

    public GenericFunction<ValidationInfo, String> validationFunc;
}

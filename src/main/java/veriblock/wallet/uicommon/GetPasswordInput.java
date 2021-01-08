// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.uicommon;

import veriblock.wallet.core.GenericFunction;

public class GetPasswordInput
{
    public boolean showShouldRemember =false;
    public boolean defaultShouldRemember = false;
    public boolean createNewPassword = false;
    public GenericFunction<String, String> testFunc = null;
}

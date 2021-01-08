// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop.entities;

import java.util.List;

public class MessageEntity {
    public String code;
    public String message;
    public List<String> details;
    public boolean error;
}

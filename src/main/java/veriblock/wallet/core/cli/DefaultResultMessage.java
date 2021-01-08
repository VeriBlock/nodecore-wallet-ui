// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.cli;

//same code as CLI (removed base class), could refactor

public class DefaultResultMessage  {
    private String _code;
    private String _message;
    private String _details;
    private boolean _isError;

    public DefaultResultMessage(String code, String message, String details, boolean error) {
        _code = code;
        _message = message;
        _details = details;
        _isError = error;
    }

    public String getCode() {
        return _code;
    }

    public String getMessage() {
        return _message;
    }

    public String getDetails() {
        return _details;
    }

    public boolean isError() {
        return _isError;
    }
}

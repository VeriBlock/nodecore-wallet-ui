// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.cli;

import org.joda.time.Interval;

import java.util.ArrayList;

//same code as CLI (removed base class), could refactor

public class DefaultResult {
    private boolean _failed;
    private Interval _executionTime;
    private ArrayList<DefaultResultMessage> _messages;

    public DefaultResult() {
        _messages = new ArrayList<>();
    }

    public void fail() { _failed = true; }

    public boolean didFail() {
        return _failed;
    }

    public Interval getExecutionTime() {
        return _executionTime;
    }

    public void setExecutionTime(Interval executionTime) {
        _executionTime = executionTime;
    }

    public ArrayList<DefaultResultMessage> getMessages() {
        return _messages;
    }

    public void addMessage(String code, String message, String details, boolean error) {
        _messages.add(new DefaultResultMessage(code, message, details, error));
    }
}

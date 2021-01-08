// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

public class ValidationInfo {

    public ValidationInfo()
    {
        //default to success
        this.setStatus(Status.Success);
    }

    public ValidationInfo(Status status, String message)
    {
        _message = message;
        _status = status;
    }

    private String _message;
    private Status _status;
    private Exception _exception;

    public String getMessage() {
        return _message;
    }
    public void setMessageInfo(String message) {
        this._message = message;
        this._status = Status.Info;
    }

    public void setMessageSuccess(String message) {
        this._message = message;
        this._status = Status.Success;
    }

    public void setMessageWarning(String message) {
        this._message = message;
        this._status = Status.Warning;
    }

    public void setMessageError(String message) {
        this._message = message;
        this._status = Status.Error;
    }

    public Status getStatus() {
        return _status;
    }
    public void setStatus(Status value) {
        this._status = value;
    }

    public Exception getException() {
        return _exception;
    }
    public void setException(Exception value) {
        this._exception = value;

        //implied:
        this._status = Status.Error;
        if (_message == null || _message.length() == 0)
        {
            _message = value.getMessage();
        }
    }

    public boolean isWarningOrError() {
        return (this._status == Status.Warning
        || this._status == Status.Error);
    }

    public boolean isWarning()
    {
        return (this._status == Status.Warning);
    }

    public boolean isError()
    {
        return (this._status == Status.Error);
    }

    public boolean isSuccess()
    {
        return (this._status == Status.Success);
    }

    public enum Status {
        Success,
        Info,
        Warning,
        Error
    }

    @Override
    public String toString() {
        if (_exception == null)
        {
            return String.format("%1$s: %2$s", this._status, this._message);
        }
        else
        {
            return String.format("%1$s: %2$s, Exception=%3$s", this._status, this._message, _exception.getMessage());
        }

    }
}

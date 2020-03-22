// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

public class CommandResult<T> {

    public CommandResult()
    {
        _validationInfo = new ValidationInfo();
    }

    public CommandResult(T payload)
    {
        _payload = payload;
        _validationInfo = new ValidationInfo();
    }

    public CommandResult(T payload, ConnectionResult connectedResult, ValidationInfo validationInfo)
    {
        _payload = payload;
        _connectedResult = connectedResult;
        _validationInfo = validationInfo;
    }

    private T _payload;
    private ConnectionResult _connectedResult;
    private ValidationInfo _validationInfo;

    //Connected, no errors, payload exists
    public boolean isSuccess() {
        //wrapper for result info
        if (!_connectedResult.isConnectedAndSynced()) {
            //Could not connect and sync
            return false;
        }

        if (_validationInfo != null && _validationInfo.getStatus() != ValidationInfo.Status.Success) {

            return false;
        }

        //NOTE -> there could be null/Void payloads. If everything was successful, then the template method would set the payload
        /*
        if (_payload == null) {
            //no payload --> what's the point? Something is wrong
            return false;
        }
        */

        //all good!
        return true;
    }

    public void setValidationInfo(Exception ex)
    {
        ValidationInfo ri = new ValidationInfo();
        ri.setException(ex);
        _validationInfo = ri;
    }
    public void setValidationInfo(ValidationInfo value)
    {
        _validationInfo = value;
    }
    public ValidationInfo getValidationInfo()
    {
        if (_validationInfo == null)
        {
            _validationInfo = new ValidationInfo();
        }
        return _validationInfo;
    }

    public boolean isErrorWalletLocked()
    {
        if (_validationInfo == null || _validationInfo.getMessage() == null)
        {
            return false;
        }

        //Wallet must be unlocked ...
        String errorMessage = _validationInfo.getMessage().toLowerCase();

        if (errorMessage.toLowerCase().contains("invalid for unlocking"))
            return true;

        return (errorMessage.contains("wallet") && errorMessage.contains("unlocked"));
    }

    public void setConnectionResult(ConnectionResult value)
    {
        _connectedResult = value;
    }
    public ConnectionResult getConnectionResult()
    {
        return _connectedResult;
    }

    public void setPayload(T value)
    {
        _payload = value;
    }
    public T getPayload()
    {
        return _payload;
    }

    /*
    public void copyResult<Z>()
    {

    }
    */

    @Override
    public String toString() {
        return String.format("Success=%1$s, Connection=%2$s, Payload=%3$s", this.isSuccess(), this.getConnectionResult(), this.getPayload());
    }
}

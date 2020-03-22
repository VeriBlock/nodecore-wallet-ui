// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import io.grpc.StatusRuntimeException;

public class ConnectionResult {
    //Makes a clone of the connectionInput as a snapshot in case the reference changes later
    public ConnectionResult(ConnectionInput connectionInput)
    {
        setErrorCode(null);
        this.ConnectionInput = connectionInput.clone();
    }

    //Checks if connected. Still syncing is not yet connected
    public boolean isConnectedAndSynced()
    {
        //First check version before going further
        String actualVersion = this.NodeCoreVersion;
        String minRequiredVersion = Constants.MINIMUM_NODECORE_VERSION;
        boolean isSufficientNCVersion = Utils.isHigherNodeCoreVersion(actualVersion, minRequiredVersion);
        if (!isSufficientNCVersion)
        {
            return false;
        }

        if (this.ConnectionState == ConnectionState.Connected_Synced)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //Min NC version required
    public String NodeCoreVersion;

    public ConnectionState ConnectionState;
    public ConnectionInput ConnectionInput;
    public Exception Exception;

    // "io.grpc.StatusRuntimeException: UNAUTHENTICATED: Invalid password!"
    public boolean IsErrorBadGrpcPassword;

    //Exception=UNAVAILABLE: io exception
    public boolean IsErrorUnavailable;

    public void setErrorCode(StatusRuntimeException exGrpc)
    {
        if (exGrpc == null)
        {
            this.IsErrorUnavailable = false;
            this.IsErrorBadGrpcPassword = false;
            return;
        }

        this.ConnectionState = ConnectionState.ErrorCouldNotConnect;
        this.Exception = exGrpc;

        String errorCode = exGrpc.getStatus().getCode().name();
        switch (errorCode)
        {
            case "UNAVAILABLE":
                this.IsErrorUnavailable = true;
                break;
            case "UNAUTHENTICATED":
                this.IsErrorBadGrpcPassword = true;
                break;
        }
    }

    @Override
    public String toString() {
        if (Exception == null) {
            return String.format("%1$s", ConnectionState);
        }
        else
        {
            return String.format("%1$s, Exception=%2$s", ConnectionState, Exception.getMessage());
        }
    }
}

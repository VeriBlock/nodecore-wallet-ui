// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

public class ConnectionInput {

    public ConnectionInput()
    {

    }
    public ConnectionInput(String address, Integer port)
    {
        this.setAddress(address);
        this.setPort(port);
    }
    public ConnectionInput(String address, Integer port, String password)
    {
        this.setAddress(address);
        this.setPort(port);
        this.setPassword(password);
    }

    public boolean hasConnectionInfo()
    {
        //if address and port have data, then this has been specified
        if (_port > 0 && _address != null && _address.length() > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public ConnectionInput clone()
    {
        return new ConnectionInput(_address, _port);
    }

    private String _address;
    private Integer _port;
    private String _password;

    public void setAddress(String value)
    {
        _address = value;
    }
    public String getAddress()
    {
        return _address;
    }

    public void setPort(Integer value)
    {
        _port = value;
    }
    public Integer getPort()
    {
        return _port;
    }

    public void setPassword(String value)
    {
        _password = value;
    }
    public String getPassword()
    {
        return _password;
    }

    public boolean hasPassword() {
        if (_password != null && _password.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getAddressPort()
    {
        return String.format("%1$s:%2$s", _address, _port);
    }
    //Could have password in future...

}

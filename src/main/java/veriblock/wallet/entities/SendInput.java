// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

public class SendInput
{
    public SendInput()
    {

    }

    /*
    Amount sent, not sourceAmount (which is sentAmount + fees)
     */
    public long sentAmount;

    public long txFeePerByte;
    public String targetAddress;
    public String fromAddress;    //Null means no specific set
}

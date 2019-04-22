// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.uicommon;

import veriblock.wallet.core.DefaultConfiguration;

import javax.security.auth.login.Configuration;

public class IntegrationLinks {
    //TODO - eventually abstract out to config, let user set these

    //region Setup
    private static IntegrationLinks single_instance = null;

    private IntegrationLinks() {

    }

    // static method to create instance of Singleton class
    public static IntegrationLinks getInstance() {
        if (single_instance == null)
            single_instance = new IntegrationLinks();

        return single_instance;
    }

    //endregion
    
    public String getVbkTransactionUrl(String vbkTxId)
    {
        //https://testnet.explore.veriblock.org/tx/CF3AC25E52BF88A1FA1093932F74410E6B9F5CF16DD32B1DD9CB8C07B311D819
        String baseUrl= "%1$s/tx/%2$s";

        String finalUrl = String.format(baseUrl,
                DefaultConfiguration.getInstance().getExplorerUrl(),
                vbkTxId );
        return finalUrl;
    }

    public String getBtcTransactionUrl(String btcTxId)
    {
        //https://www.blockchain.com/btc/tx/cf3ac25e52bf88a1fa1093932f74410e6b9f5cf16dd32b1dd9cb8c07b311d819
        String baseUrl= "https://www.blockchain.com/btc/tx/%1$s";

        String finalUrl = String.format(baseUrl,btcTxId );
        return finalUrl;
    }

    public String getBtcAddressUrl(String btcAddress)
    {
        //https://www.blockchain.com/btc/address/17yXRuJ6jQNFJuR8Yi4Sj4h1P5KZmRb2Z8
        String baseUrl= "https://www.blockchain.com/btc/address/%1$s";

        String finalUrl = String.format(baseUrl,btcAddress );
        return finalUrl;
    }


}

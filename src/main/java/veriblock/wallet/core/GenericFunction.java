// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;


/*
    GenericFunction<String, String> testFunc = (strInput) -> {
        return myMethod(strInput);
    };
 */


//Used to pass in generic function as a param
public interface GenericFunction<T,K> {
    T doWork(K input);
}

// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.storage;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "myaddress_nickname")
public class MyAddressNicknameData {

    @DatabaseField(columnName = "address", id = true)
    public String address;

    @DatabaseField(columnName = "nickname")
    public String nickname;

}


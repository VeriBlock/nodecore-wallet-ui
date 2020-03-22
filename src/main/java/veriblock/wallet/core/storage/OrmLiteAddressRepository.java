// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class OrmLiteAddressRepository {

    private static final Logger logger = LoggerFactory.getLogger(OrmLiteAddressRepository.class);

    private ConnectionSource connectionSource;
    private Dao<MyAddressNicknameData, String> table_myaddress_nickname;

    public OrmLiteAddressRepository()
    {
        try {
            this.connectionSource = DefaultConnectionSource.getInstance().getConnection();
            TableUtils.createTableIfNotExists(connectionSource, MyAddressNicknameData.class);
            table_myaddress_nickname = DaoManager.createDao(connectionSource, MyAddressNicknameData.class);

            table_myaddress_nickname.executeRaw("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            logger.error("SQL Error: {}", e.getSQLState(), e);
        }
    }

    //Don't require creating object for two fields
    public void save(String address, String nickname) {
        MyAddressNicknameData row = new MyAddressNicknameData();
        row.address = address;
        row.nickname = nickname;
        try {
            table_myaddress_nickname.createOrUpdate(row);
        } catch (SQLException e) {
            logger.error("SQL Error: {}", e.getSQLState(), e);
        }
    }

    private List<MyAddressNicknameData> getAll() {
        try {
            return table_myaddress_nickname.queryForAll();
        } catch (SQLException e) {
            logger.error("SQL Error: {}", e.getSQLState(), e);
        }

        return null;
    }

    public HashMap<String, String> getAllAsHashMap()
    {
        List<MyAddressNicknameData> rows = getAll();
        HashMap<String, String> map = new HashMap<String, String>();

        for (MyAddressNicknameData row : rows)
        {
            map.put(row.address, row.nickname);
        }
        return map;
    }
}

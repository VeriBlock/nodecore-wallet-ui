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


public class OrmLiteGenericRepository {

    private static final Logger logger = LoggerFactory.getLogger(OrmLiteGenericRepository.class);

    private ConnectionSource connectionSource;
    private Dao<GenericCacheData, String> table_generic_cache;

    public OrmLiteGenericRepository()
    {
        try {
            this.connectionSource = DefaultConnectionSource.getInstance().getConnection();
            TableUtils.createTableIfNotExists(connectionSource, GenericCacheData.class);
            table_generic_cache = DaoManager.createDao(connectionSource, GenericCacheData.class);

            table_generic_cache.executeRaw("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            logger.error("SQL Error: {}", e.getSQLState(), e);
        }
    }

    //Don't require creating object for two fields
    public void save(String key, String value) {

        GenericCacheData row = new GenericCacheData();
        row.key = key;
        row.value = value;
        try {
            table_generic_cache.createOrUpdate(row);
        } catch (SQLException e) {
            logger.error("SQL Error: {}", e.getSQLState(), e);
        }
    }

    public String getValue(String key) {
        try {
            GenericCacheData row = table_generic_cache.queryForId(key);
            if (row == null)
            {
                return null;
            }
            else
            {
                return row.value;
            }
        } catch (SQLException e) {
            logger.error("SQL Error: {}", e.getSQLState(), e);
        }

        return null;
    }

}

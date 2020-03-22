// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.storage;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.DefaultConfiguration;
import veriblock.wallet.core.FileManager;
import veriblock.wallet.core.Utils;

import java.nio.file.Paths;

public class DefaultConnectionSource {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConnectionSource.class);

    private static String getDataFolder()
    {
        FileManager fm = new FileManager();
        return fm.getDataDirectory();
    }

    private static DefaultConnectionSource single_instance = null;

    // static method to create instance of Singleton class
    public static DefaultConnectionSource getInstance()
    {
        if (single_instance == null)
            single_instance = new DefaultConnectionSource();

        return single_instance;
    }

    private DefaultConnectionSource()
    {
        setConnection();
    }

    private ConnectionSource _connection;

    public ConnectionSource getConnection()
    {
        return _connection;
    }

    private void setConnection()
    {
        String databasePath = Paths.get(getDataFolder(), "meta.dat").toString();
        logger.info("SqlLite path: '{}'", databasePath);

        try {
            String url = String.format("jdbc:sqlite:%s", databasePath);
            _connection = new JdbcPooledConnectionSource(url);
        }
        catch (Exception ex)
        {
            logger.error("Could not create connection to database '{}', error: {}",
                    databasePath, ex.getMessage());
        }
    }
}

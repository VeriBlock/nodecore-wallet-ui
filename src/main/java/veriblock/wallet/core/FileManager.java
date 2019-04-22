// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.Main;

import java.nio.file.Paths;

public class FileManager {

    private static final Logger _logger = LoggerFactory.getLogger(FileManager.class);
    public FileManager()
    {

    }

    private static final String DATA_FOLDER = "data";
    private static final String BACKUP_FOLDER = "backups";
    private static final String EXPORT_FOLDER = "csv_export";

    public String getRootDirectory()
    {
        String userDir = System.getProperty("user.home");
        int i = 0;

        String name = "nodecore-wallet-ui-0.4";

        String sPath = Paths.get(userDir, name ).toString();

        ensureDirectoryExists(sPath, "application");

        return sPath;
    }

    public String getDataDirectory()
    {
        String s =  getRootDirectory();
        String sPath = Paths.get(s, DATA_FOLDER).toString();
        ensureDirectoryExists(sPath, "data");
        return sPath;
    }

    public String getBackupDirectory()
    {
        String s =  getRootDirectory();
        String sPath = Paths.get(s, BACKUP_FOLDER).toString();
        ensureDirectoryExists(sPath, "backups");
        return sPath;
    }

    public String getExportDirectory()
    {
        String s =  getRootDirectory();
        String sPath = Paths.get(s, EXPORT_FOLDER).toString();
        ensureDirectoryExists(sPath, "exports");
        return sPath;
    }

    private static void ensureDirectoryExists(String sPath, String purpose)
    {
        if (!Utils.doesFolderExist(sPath))
        {
            Utils.createFolder(sPath);
            _logger.info("Created {} directory '{}'", sPath, purpose);
        }
    }

    public String getLogDirectory()
    {
        //repeat logic from logback.groovy
        //Read: NODECORE_UI_LOG_PATH

        String override = System.getenv("NODECORE_UI_LOG_PATH");
        if (override != null && override.length() > 0)
        {
            return override;
        }

        String s =  getRootDirectory();
        String logSubFolder = "logs";
        String sPath = Paths.get(s, logSubFolder).toString();
        return sPath;
    }

}

// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.uicommon.FormatHelper;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;

//Wrapper for properties
public class DefaultConfiguration {

    private static final Logger _logger = LoggerFactory.getLogger(DefaultConfiguration.class);
    private static final String PROPERTY_FILE = "nodecore_wallet_ui.properties";

    private void setupDefault()
    {
        int iDefaultPort = 10500;   //MainNet

        this.setNodecoreAddress("127.0.0.1");
        this.setNodecorePort(iDefaultPort);
        this.setSendFeeperbyteMinimum(800);
        this.setSendFeeperbyteWarningBelowThreshold(800);
        this.setSendFeeperbyteWarningAboveThreshold(12000);
        this.setDebugLocaleUsefiles(false);
    }

    private final Properties _properties = new Properties();

    private static DefaultConfiguration single_instance = null;
    private DefaultConfiguration()
    {
        load();
    }

    // static method to create instance of Singleton class
    public static DefaultConfiguration getInstance()
    {
        if (single_instance == null)
            single_instance = new DefaultConfiguration();

        return single_instance;
    }

    private String _nodecoreAddress;
    private Integer _nodecorePort;
    private String _nodecorePassword;
    //private String _network;
    private boolean _debugLocaleUsefiles;

    private long _send_feeperbyte_minimum;
    private long _send_feeperbyte_warning_belowthreshold;
    private long _send_feeperbyte_warning_abovethreshold;

    //region get/set

    //private final String NETWORK = "network";
    private static final String NODECORE_PORT = "nodecore.port";
    private static final String NODECORE_ADDRESS = "nodecore.address";
    private static final String NODECORE_PASSWORD = "rpc.security.password";
    private static final String SEND_FEEPERBYTE_MINIMUM= "send.feeperbyte.minimum";
    private static final String SEND_FEEPERBYTE_WARNING_BELOWTHRESHOLD= "send.feeperbyte.warning.belowthreshold";
    private static final String SEND_FEEPERBYTE_WARNING_ABOVETHRESHOLD = "send.feeperbyte.warning.abovethreshold";
    private static final String DEBUG_LOCALE_USEFILES = "debug.locale.usefiles";


    public void setNodecoreAddress(String value)
    {
        _nodecoreAddress = value;
    }
    public String getNodecoreAddress()
    {
        return _nodecoreAddress;
    }

    public void setNodecorePort(Integer value)
    {
        _nodecorePort = value;
    }
    public Integer getNodecorePort()
    {
        return _nodecorePort;
    }

    public String getNodecorePassword() {
        if (_nodecorePassword == null) {
            return "";
        } else {
            return _nodecorePassword;
        }
    }

    public void setSendFeeperbyteMinimum(long value)
    {
        _send_feeperbyte_minimum = value;
    }
    public long getSendFeeperbyteMinimum()
    {
        return _send_feeperbyte_minimum;
    }

    public void setSendFeeperbyteWarningBelowThreshold(long value)
    {
        _send_feeperbyte_warning_belowthreshold = value;
    }
    public long getSendFeeperbyteWarningBelowThreshold()
    {
        return _send_feeperbyte_warning_belowthreshold;
    }

    public void setSendFeeperbyteWarningAboveThreshold(long value)
    {
        _send_feeperbyte_warning_abovethreshold = value;
    }
    public long getSendFeeperbyteWarningAboveThreshold()
    {
        return _send_feeperbyte_warning_abovethreshold;
    }

    public void setDebugLocaleUsefiles(boolean value)
    {
        _debugLocaleUsefiles = value;
    }
    public boolean getDebugLocaleUsefiles()
    {
        return _debugLocaleUsefiles;
    }

    //endregion

    public void save()
    {
        try {
            File configFile = new File(getConfigPath());
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            OutputStream stream = new FileOutputStream(configFile);
            save(stream);
            stream.close();
        } catch (IOException e) {
            _logger.warn("Unable to save custom properties file", e);
        }
    }
    public void save(OutputStream outputStream) {
        try {
            copyForSave();
            _properties.store(outputStream, "VeriBlock GUI Wallet");
            outputStream.flush();
        } catch (Exception e) {
            _logger.error("Unhandled exception in DefaultConfiguration.save", e);
        }
    }

    private void copyForSave()
    {
        _properties.setProperty(NODECORE_ADDRESS, this.getNodecoreAddress());
        _properties.setProperty(NODECORE_PORT, this.getNodecorePort().toString());
        _properties.setProperty(NODECORE_PASSWORD, this.getNodecorePassword().toString());

        _properties.setProperty(SEND_FEEPERBYTE_MINIMUM, VbkUtils.convertAtomicToVbkString(getSendFeeperbyteMinimum()));
        _properties.setProperty(SEND_FEEPERBYTE_WARNING_BELOWTHRESHOLD, VbkUtils.convertAtomicToVbkString(getSendFeeperbyteWarningBelowThreshold()));
        _properties.setProperty(SEND_FEEPERBYTE_WARNING_ABOVETHRESHOLD, VbkUtils.convertAtomicToVbkString(getSendFeeperbyteWarningAboveThreshold()));

        _properties.setProperty(DEBUG_LOCALE_USEFILES, Boolean.toString(getDebugLocaleUsefiles()));


    }

    public String getConfigPath() {
        FileManager fm = new FileManager();
        return Paths.get(fm.getRootDirectory(), PROPERTY_FILE).toString();
    }

    public void reload()
    {
        _logger.info("reload properties");
        load();
    }

    private void load() {
        //nodecore.address=127.0.0.1
        //nodecore.port=10502
        //network=alpha

        //_properties = new Properties();
        String configPath = getConfigPath();
        boolean exists = (new File(configPath)).exists();
        _logger.info("config path: '{}'", configPath);
        if (exists) {
            _logger.info("config path already exists");
        }
        else {
            _logger.info("config path does not exist, will create new file");
            setupDefault();
            save();
        }

        //Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(getConfigPath());

            // load a properties file
            _properties.load(input);

            //-------------
            //Got them!
            this._nodecoreAddress = _properties.getProperty(NODECORE_ADDRESS, "127.0.0.1");
            this._nodecorePort = Integer.parseInt(_properties.getProperty(NODECORE_PORT, "10500"));
            this._nodecorePassword = _properties.getProperty(NODECORE_PASSWORD, "");
            this._send_feeperbyte_minimum = VbkUtils.convertDecimalCoinToAtomicLong(_properties.getProperty(SEND_FEEPERBYTE_MINIMUM, ""));
            this._send_feeperbyte_warning_belowthreshold = VbkUtils.convertDecimalCoinToAtomicLong(_properties.getProperty(SEND_FEEPERBYTE_WARNING_BELOWTHRESHOLD, ""));
            this._send_feeperbyte_warning_abovethreshold = VbkUtils.convertDecimalCoinToAtomicLong(_properties.getProperty(SEND_FEEPERBYTE_WARNING_ABOVETHRESHOLD, ""));
            this._debugLocaleUsefiles = Boolean.parseBoolean(_properties.getProperty(DEBUG_LOCALE_USEFILES, "false"));
            //-------------

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //region inferred properties

    //Returns tVBK or VBK
    public String getVbkUnit()
    {
        if (_nodecorePort == 10500)
        {
            return "VBK";
        }
        else
        {
            return "tVBK";
        }
    }

    public String getNetworkName()
    {
        if (_nodecorePort == 10500)
        {
            return "MainNet";
        }
        else if (_nodecorePort == 10501)
        {
            return "TestNet";
        }
        else if (_nodecorePort == 10502)
        {
            return "Alpha";
        }
        else
        {
            return "Unknown";
        }
    }

    public String getExplorerUrl()
    {
        //TODO - maybe allow this to pull from prop file for more configurability (QA/Dev/Alpha?)
        if (_nodecorePort == 10500)
        {
            return "https://explore.veriblock.org";
        }
        else if (_nodecorePort == 10501)
        {
            return "https://testnet.explore.veriblock.org";
        }
        else if (_nodecorePort == 10502)
        {
            return "https://alpha.explore.veriblock.org";
        }
        else
        {
            return "https://testnet.explore.veriblock.org";
        }
    }

    //endregion

}

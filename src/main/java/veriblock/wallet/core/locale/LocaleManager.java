// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.DefaultConfiguration;
import veriblock.wallet.core.Utils;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.shell.SettingsConstants;

import java.util.*;

public class LocaleManager {

    public static final SupportedLocales.SupportedLocale DEFAULT_LOCALE = SupportedLocales.SupportedLocale.en_US;

    //region Supported Locales

    public java.util.Locale convertToJavaLocale() {
        SupportedLocales.SupportedLocale supportedLocale = this.getLocal();
        return SupportedLocales.convertToJavaLocale(supportedLocale);
    }

    public static SupportedLocales.SupportedLocale convertStringLocal(String localeString)
    {
        return SupportedLocales.convertStringLocal(localeString);
    }

    public String convertLocaleToLanguagePrefix(SupportedLocales.SupportedLocale locale)
    {
        //get first portion before '_'
         String s = locale.toString();

         int i = s.indexOf('_');
         if (i == -1)
         {
             return s;
         }
         else
         {
             return s.substring(0, i);
         }
    }

    public List<LocaleItem> getSupportedLocales()
    {
        String resourcePath = "locale/Locales.json";

        List<LocaleItem> list = new ArrayList<LocaleItem>();

        String jsonContent = Utils.getResourceFileAsString(resourcePath);
        if (jsonContent == null)
        {
            //Could not find resource file, just use default
            list.add(new LocaleItem("English", SupportedLocales.SupportedLocale.en_US));
        }
        else {
            HashMap<String, String> map = convertJsonToHashmap(jsonContent, resourcePath);

            for (Map.Entry<String, String> entry : map.entrySet()) {
                String displayText = entry.getKey();
                SupportedLocales.SupportedLocale locale = SupportedLocales.convertStringLocal(entry.getValue());
                list.add(new LocaleItem(displayText, locale));
            }
        }

        return list;
    }

    //endregion

    //region Setup

    private static final Logger _logger = LoggerFactory.getLogger(LocaleManager.class);

    private static LocaleManager single_instance = null;

    // static method to create instance of Singleton class
    public static LocaleManager getInstance() {
        if (single_instance == null)
            single_instance = new LocaleManager();

        return single_instance;
    }

    //endregion

    //region Swap locale


    public SupportedLocales.SupportedLocale getSystemLocale() {
        String lang = System.getProperty("user.language").toLowerCase();
        return SupportedLocales.convertStringLocal(lang);
    }

    public void setLocal() {
        SupportedLocales.SupportedLocale locale = getSystemLocale();
        setLocal(locale);

    }

    //Returns the local found. If not found, uses the default
    public SupportedLocales.SupportedLocale setLocal(SupportedLocales.SupportedLocale locale) {

        if (locale == SupportedLocales.SupportedLocale.UNKNOWN)
        {
            _logger.info("Local is unknown, override to default: {}", DEFAULT_LOCALE);
            locale= DEFAULT_LOCALE;
        }

        //could set automatically, or from override
        _locale = locale;
        _logger.info("Set local to: {}", locale);

        //Refresh cache
        _cachedLocaleFiles.clear();

        //convert to just before '_', like 'en' or 'zh'
        UserSettings.save(SettingsConstants.LOCALE_OVERRIDE, convertLocaleToLanguagePrefix(locale));

        return locale;
    }

    public SupportedLocales.SupportedLocale getLocal() {
        return _locale;
    }

    private SupportedLocales.SupportedLocale _locale;

    //endregion

    public static String getLocaleName(SupportedLocales.SupportedLocale locale)
    {
        return locale.toString().replace("_", "-");
    }

    //region File management

    private static String convertSupportedLocaleToFolder(SupportedLocales.SupportedLocale locale)
    {
        //assume all-lower case, no special char like '-'
        return locale.toString().toLowerCase();
    }
    private String getResourcePath(String module, SupportedLocales.SupportedLocale locale)
    {
        String localeFolder = convertSupportedLocaleToFolder(locale);
        //example: "locale/TEST/welcome.json"
        String resourcePath = String.format("locale/%1$s/%2$s.json", localeFolder, module);
        return resourcePath;
    }
    private String getResourceContent(String module) {

        SupportedLocales.SupportedLocale locale = getLocal();
        return getResourceContent(module, locale);
    }
    private String getResourceContent(String module, SupportedLocales.SupportedLocale locale)
    {
        String resourcePath = getResourcePath(module, locale);

        String s = null;
        try {
            boolean shouldReadFromExternalFile = DefaultConfiguration.getInstance().getDebugLocaleUsefiles();

            if (shouldReadFromExternalFile)
            {
                s = Utils.getResourceAsLocalFile(resourcePath);
            }
            else {
                s = Utils.getResourceFileAsString(resourcePath);
                int iLength = 0;
                if (s != null)
                {
                    iLength = s.length();
                }
                _logger.debug("Read resource: {}, length={}",resourcePath, iLength );
            }

            if (s == null)
            {
                _logger.error("Could not read language resource={}, Note that resourcePaths are CASE-SENSITIVE, shouldReadFromExternalFile={}", resourcePath, shouldReadFromExternalFile);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return s;
    }




    private HashMap<String, String> getLanguageMap(String module) {
        SupportedLocales.SupportedLocale locale = getLocal();
        return getLanguageMap(module, locale);
    }
    private HashMap<String, String> getLanguageMap(String module, SupportedLocales.SupportedLocale locale) {
        String jsonContent =  getResourceContent(module, locale);
        if (jsonContent == null)
        {
            //Could not find resource file
            return null;
        }

        String resourcePath = getResourcePath(module, locale);
        HashMap<String, String> map = convertJsonToHashmap(jsonContent, resourcePath);
        return map;
    }

    public static HashMap<String, String> convertJsonToHashmap(String jsonContent, String resourcePath) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (jsonContent == null) {
            return map;
        }

        JsonParser jp = new JsonParser();
        JsonElement root = null;
        try {
            //Very feasible a community contribution has a parse error
            //Want to make the error clear, and crash
            root = jp.parse(jsonContent);

        } catch (Exception ex) {
            _logger.error("Could not parse JSON file: {}, exception={}", resourcePath, ex.getMessage());
            throw ex;
        }

        JsonObject jsonObj = root.getAsJsonObject();
        for (Object key : jsonObj.keySet()) {
            //based on you key types
            String keyStr = (String) key;
            String keyvalue = jsonObj.get(keyStr).getAsString();
            map.put(keyStr, keyvalue);
        }

        return map;
    }



    //endregion

    private HashMap<String, LocaleModule> _cachedLocaleFiles = new HashMap<String, LocaleModule>();

    //Returns null if module does not exist
    public LocaleModule getModule(LocaleModuleResource moduleResource) {
        String moduleName = moduleResource.toString();

        //First check cache
        LocaleModule m = null;
        if (_cachedLocaleFiles != null && _cachedLocaleFiles.containsKey(moduleName))
        {
            m = _cachedLocaleFiles.get(moduleName);
        }
        else {
            SupportedLocales.SupportedLocale locale = SupportedLocales.SupportedLocale.UNKNOWN;
            HashMap<String, String> map = getLanguageMap(moduleName);
            if (map == null)
            {
                //Bad, developer/build error that should never happen
                //Get default module
                locale = DEFAULT_LOCALE;
                map = getLanguageMap(moduleName, locale);
                if (map == null)
                {
                    //really bad, not even default is there
                    _logger.error("Corrupt package: Missing DEFAULT_LOCALE={} for module={}", locale, moduleName );
                    return null;
                }
            }
            else
            {
                locale =  getLocal();
            }

            m = new LocaleModule();
            m.setup(map, moduleName, locale);
            _cachedLocaleFiles.put(moduleName, m);
        }

        return m;
    }

}

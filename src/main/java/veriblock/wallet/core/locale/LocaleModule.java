// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.Constants;

import java.util.HashMap;

public class LocaleModule {

    private static final Logger _logger = LoggerFactory.getLogger(LocaleModule.class);

    public LocaleModule()
    {
        _map = new HashMap<String, String>();
    }

    @Override
    public String toString() {
        int iCount = 0;
        if (_map != null)
        {
            iCount = _map.size();
        }
        return String.format("Module=%1$s, Locale=%2$s, items=%3$s",
                _moduleName, this.getLocalName(), iCount);
    }

    public String getModuleName() {
        return _moduleName;
    }

    private String _moduleName;
    private SupportedLocales.SupportedLocale _supportedLocale;
    public String getLocalName()
    {
        return LocaleManager.getLocaleName(_supportedLocale);
    }

    public void setup(HashMap<String, String> map, String moduleName, SupportedLocales.SupportedLocale locale)
    {
        _moduleName = moduleName;
        _supportedLocale = locale;
        _map = map;
    }

    private HashMap<String, String> _map;

    public String getString(String key)
    {
        try {
            //If not translated, then return default --> LocaleResourceAnalyzer will default to at least the Test TT_ equivalent
            if (_map.containsKey(key))
            {
                //great!
                String result = _map.get(key);

                if (!Constants.IS_TEST_MODE)
                {
                    //Live production, so remove the "TT_" prefix --> worst case show it in English rather than untranslated
                    if (result == null)
                    {
                        result = "";
                    }

                    //If locale==Test, then show the TT_

                    if (_supportedLocale == SupportedLocales.SupportedLocale.TEST)
                    {
                        //do nothing
                    }
                    else
                    {
                        //Something other than "TEST"
                        if (result.startsWith("TT_")) {
                            result = result.replace("TT_", "");
                        }
                    }

                }

                return result;
            }
            else
            {
                //bad, this should be caught before compile time with the LocaleResourceAnalyzer tool
                //should never show to UI
                _logger.info("Could not find key '{}' in module {}", key, this.getModuleName());
                return "NEED_TO_TRANSLATE: " + key;
            }

        }
        catch (Exception ex)
        {
            //Should never be hit
            ex.printStackTrace();
            return null;
        }
    }

    //If the text matches the pattern, then replace with key instead
    //NOTE --> this will not currently pull values from the source, such as numbers. It's a static replace
    //If Default locale, then keep original, as that will have more info
    public String getLookupPattern(String sourceText, HashMap<String, String> lookups)
    {
        if (LocaleManager.DEFAULT_LOCALE == this._supportedLocale)
        {
            //already default, so use the original
            return sourceText;
        }
        else
        {
            for (String pattern : lookups.keySet())
            {
                String localeKey=  lookups.get(pattern);
                if (sourceText.contains(pattern))
                {
                    return getString(localeKey);
                }
            }
        }

        //bad
        _logger.info("Could not find pattern to match source text '{}' in module {}", sourceText, this.getModuleName());

        //Worst case, return original source text:
        return sourceText;
    }

}

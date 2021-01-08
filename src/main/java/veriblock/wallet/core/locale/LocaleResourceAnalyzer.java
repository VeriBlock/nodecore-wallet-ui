// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import veriblock.wallet.core.Utils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocaleResourceAnalyzer {

    public LocaleResourceAnalyzer() {
        this.shouldMerge = false;
    }


    //Compare 2 locales. Assume 1st is the standard
    //output
    //Action required: File 'X' is missing
    //Action required: File 'X' is present but has these missing keys:
    //  -key=""
    //  -key=""
    //Good: File 'X' is up-to-date

    public boolean shouldMerge;

    public void compareLocales(String localePrimary, String localeSecondary, String resourceFolderOverride) {

        resourceFolderOverride = Utils.getCanonicalFolderPath(resourceFolderOverride);
        if (!Utils.doesFolderExist(resourceFolderOverride))
        {
            System.out.println(String.format("ERROR: Could not find override resource folder: '%1$s'", resourceFolderOverride));
            return;
        }

        Results results = getResult(localePrimary, localeSecondary, resourceFolderOverride);
        formatResults(results);
    }

    private void formatResults(Results results)
    {
        String sectionBreak = "------------------------------";
        System.out.println(sectionBreak);
        System.out.println(String.format("==Translation Summary Report: '%2$s' (compared to '%1$s')==",
                results.getLocalePrimary(), results.getLocaleSecondary()));

        //CASE 1: Action Required: Extra Files
        if (results.getExtraSecondaryFile().size() > 0)
        {
            System.out.println();
            System.out.println(String.format("Action Required for %1$s: These files are extra in %1$s and should be removed:",
                    results.getLocaleSecondary()));
            for (String s : results.getExtraSecondaryFile())
            {
                System.out.println(String.format("\t%1$s", s));
            }
        }
        else
        {
            System.out.println(String.format("Good: There are no are extra files in %1$s",
                    results.getLocaleSecondary()));
        }

        //CASE 2: Action Required: Missing Files
        if (results.getMissingSecondaryFiles().size() > 0)
        {
            System.out.println();
            System.out.println(String.format("Action Required for %1$s: These files are missing in %1$s and should be created:",
                    results.getLocaleSecondary()));
            for (String s : results.getMissingSecondaryFiles())
            {
                System.out.println(String.format("\t%1$s", s));
            }
        }
        else
        {
            System.out.println(String.format("Good: There are no are missing files in %1$s",
                    results.getLocaleSecondary()));
        }

        //CASE 3C: Action Required: entire file needs translating
        System.out.println();
        System.out.println(String.format("Action Required for %1$s:  Entire file needs to be translated:",
                results.getLocaleSecondary()));
        for (FileResults fr : results.getFileResults()) {
            if (!fr.isSuccess()) {
                if (fr.isJsonValid()) {
                    if (fr.doesEntireFileNeedTranslating()) {
                        System.out.println(String.format("\t%1$s - %2$s keys",
                                fr.getFileName(), fr.getTotalKeys()));
                    }
                }
            }
        }


        //CASE 3: Action Required: Keys don't match (either missing/extra)
        System.out.println();
        System.out.println(String.format("Action Required for %1$s: These files exist but have missing or extra keys:",
                results.getLocaleSecondary()));
        int iCount = 0;
        for (FileResults fr : results.getFileResults()) {
            if (!fr.isSuccess()) {
                String s1 = fr.getFileName();
                if (!fr.isJsonValid()) {
                    //CASE 3A
                    //Check if valid JSON
                    if (!fr.getIsValidJsonPrimary()) {
                        s1 = String.format("%1$s, Invalid JSON: %2$s", s1, results.getLocalePrimary());    // + ": Primary JSON not valid";
                    }
                    if (!fr.getIsValidJsonSecondary()) {
                        s1 = String.format("%1$s, Invalid JSON: %2$s", s1, results.getLocaleSecondary());
                    }
                    iCount++;
                    System.out.println(String.format("\t%1$s", s1));
                } else {
                    //JSON is valid, so compare content

                    if (fr.doesEntireFileNeedTranslating()) {
                        //Separte case handled elsewhere
                    } else {
                        //Check all keys
                        System.out.println(String.format("\t%1$s: %2$s of %3$s keys translated", s1,
                                fr.getTranslatedKeys().size(), fr.getTotalKeys()));
                        //loop!
                        if (fr.getMissingOrPlaceholderCount() > 0) {
                            System.out.println(String.format("\t\tThere are %1$s missing keys in %2$s, please create or translate these:",
                                    fr.getMissingOrPlaceholderCount(), results.getLocaleSecondary()));
                            //Missing --> provide hint that this is missing outright
                            for (String key : fr.getMissingKeys()) {
                                iCount++;
                                System.out.println(String.format("\t\t\t%1$s [add placeholder]", key));
                            }
                            //Placeholder --> would be default. Eventually tool auto-merges in keys to ensure at
                            //  least a placeholder
                            for (String key : fr.getPlaceholderKeys()) {
                                iCount++;
                                System.out.println(String.format("\t\t\t%1$s", key));
                            }
                        }

                        if (fr.getExtraKeys().size() > 0) {
                            System.out.println(String.format("\t\tThere are %1$s extra keys in %2$s, please remove these:",
                                    fr.getExtraKeys().size(), results.getLocaleSecondary()));
                            for (String key : fr.getExtraKeys()) {
                                iCount++;
                                System.out.println(String.format("\t\t\t%1$s", key));
                            }
                        }
                    }
                }

            }
        }
        if (iCount > 0) {
            //results existed
        }
        else
        {
            System.out.println("\tNothing found - all good!");
        }
        System.out.println();

        //CASE 4: No Action: individual file details --> only success
        System.out.println(String.format("Good, No Action Required: File exists and all keys match"));
        for (FileResults fr : results.getFileResults())
        {
            if (fr.isSuccess())
            {
                System.out.println(String.format("\t%1$s", fr.getFileName()));
            }
        }

        System.out.println();
        System.out.println("Done with Report");
        System.out.println(sectionBreak);
    }


    private Results getResult(String localePrimary, String localeSecondary, String resourceFolderOverride) {

        Results results = new Results(localePrimary, localeSecondary);

        String strRootFolder = null;
        if (resourceFolderOverride == null) {
            //infer it:
            strRootFolder = getRootLocaleFolder();
        }
        else
        {
            //use override
            System.out.println(String.format("Use resource folder override: '%1$s'", resourceFolderOverride));
            strRootFolder = resourceFolderOverride;
        }

        File[] primaryItems = new File(
                Paths.get(strRootFolder, localePrimary).toString()
        ).listFiles();

        File[] secondaryItems = new File(
                Paths.get(strRootFolder, localeSecondary).toString()
        ).listFiles();

        //find extra
        for (File f2 : secondaryItems) {
            File f1 = getFile(primaryItems, f2.getName());
            if (f1 == null)
            {
                //bad --> we have an extra file in f2
                results.addExraSecondaryFile(f2.getName());
            }
            else
            {
                //good
            }
        }

        //loop through each item in primary
        for (File f1 : primaryItems) {

            //TODO --> add to results output so we can format a sumamry at the end?

            //Does file exist?
            File f2 = getFile(secondaryItems, f1.getName());
            if (f2 == null)
            {
                //nuts
                results.addMissingSecondaryFile(f1.getName());
                System.out.println(String.format(
                        "Action Required: Missing file '%1$s/%2$s'", localeSecondary, f1.getName() ));
            }
            else
            {
                //good --> found it, so process it
                //System.out.println(String.format(
                //       "Found file: '%1$s/%2$s'", localeSecondary, f1.getName() ));

                FileResults fr = compareFile(f1, f2);
                results.addFileResult(fr);

                //check for compare_merge on file2
                if (this.shouldMerge)
                {
                    boolean isEnglish = isEnglishLocale(localeSecondary);
                    mergeInMissingExtraKeys(f1, f2, isEnglish, fr);
                }
            }
        }

        return results;
    }

    private static boolean isEnglishLocale(String localeSecondary)
    {
        return (localeSecondary.toLowerCase().startsWith("en"));
    }
    private static boolean isTestLocale(String folderFullPath)
    {
        return (folderFullPath.toLowerCase().contains("test"));
    }

    private static JsonObject getJsonFromFile(File file)
    {
        JsonParser jp = new JsonParser();
        JsonElement root = null;

        String targetFilePath = file.toPath().toString();
        String jsonContent = Utils.getResourceAsLocalFile(targetFilePath);
        root = jp.parse(jsonContent);
        JsonObject jsonObj = root.getAsJsonObject();
        return jsonObj;
    }

    private void mergeInMissingExtraKeys(File f1, File f2, boolean isEnglish, FileResults fr) {
        try {
            String targetFilePath = f2.toPath().toString();
            System.out.println("MERGE: " + targetFilePath);

            HashMap<String, String> map1 = LocaleManager.convertJsonToHashmap(
                    Utils.getResourceAsLocalFile(f1.toString()), f1.toString());

            JsonObject jsonObj = getJsonFromFile(f2);

            //STEP 0: English -> always overwrite
            if (isEnglish) {
                for (String translatedKey : fr.getTranslatedKeys()) {
                    String sourceValue = map1.get(translatedKey);
                    String targetValue = jsonObj.get(translatedKey).getAsString();
                    String englishExpectedValue = sourceValue.replace("TT_", "");

                    if (!targetValue.equals(englishExpectedValue)) {
                        //Still a placeholder, does not match, therefore update
                        jsonObj.remove(translatedKey);
                        jsonObj.addProperty(translatedKey, englishExpectedValue);
                        System.out.println("Updated placeholder key: " + translatedKey);
                    }
                }
            }

            //STEP 1: Update any placeholder keys to use the latest placeholder text
            for (String placeholderKey : fr.getPlaceholderKeys()) {
                String sourceValue = map1.get(placeholderKey);

                String targetValue = jsonObj.get(placeholderKey).getAsString();
                //Do safety check
                if (targetValue != null) {
                    if (isEnglish) {
                        //always update English version from test

                        String englishValue = sourceValue.replace("TT_", "");
                        if (!sourceValue.equals(targetValue)) {
                            //Still a placeholder, does not match, therefore update
                            jsonObj.remove(placeholderKey);
                            jsonObj.addProperty(placeholderKey, englishValue);
                            System.out.println("Updated placeholder key: " + placeholderKey);
                        }

                    } else {
                        //Non-English
                        if (targetValue.startsWith("TT_")) {
                            if (!sourceValue.equals(targetValue)) {
                                //Still a placeholder, does not match, therefore update
                                jsonObj.remove(placeholderKey);
                                jsonObj.addProperty(placeholderKey, sourceValue);
                                System.out.println("Updated placeholder key: " + placeholderKey);
                            }
                        }
                    }
                }
            }

            //STEP 2: Add missing keys to secondary
            for (String missingKey : fr.getMissingKeys()) {
                String strPlaceholderValue = map1.get(missingKey);

                //Shortcut: If English, then remove "TT_". Test is just "TT_" + English
                if (isEnglish) {
                    strPlaceholderValue = strPlaceholderValue.replace("TT_", "");
                } else {
                    if (!strPlaceholderValue.startsWith("TT_")) {
                        strPlaceholderValue = "TT_" + strPlaceholderValue;
                    }
                }

                System.out.println("Merged in key: " + missingKey);
                jsonObj.addProperty(missingKey, strPlaceholderValue);
            }

            //STEP 3: Remove extra keys from secondary
            for (String extraKey : fr.getExtraKeys()) {
                System.out.println("Removed extra key: " + extraKey);
                jsonObj.remove(extraKey);
            }

            //re-sort!
            jsonObj = resortJsonMembers(f1, jsonObj);

            //save!
            String content = Utils.toJsonPrettyFormat(jsonObj);
            //Ensure writes as UTF-8
            Utils.writeFileUtf(content, targetFilePath);

            System.out.println("done - file updated");
        } catch (Exception ex) {
            System.out.println(Utils.getExcetionToString(ex));
        }
    }

    private static JsonObject resortJsonMembers( File file1,  JsonObject jsonObj2) {
        //have key order of 2 match 1

        //1 is primary
        //clear
        /*
        for (Object key : jsonObj2.keySet()) {
            //based on you key types
            String keyStr = (String) key;
            jsonObj2.remove(keyStr);
        }
        */

        //Hashmap does not guarantee order
        JsonObject jsonObj1 = getJsonFromFile(file1);

        //re-add
        JsonObject jNew = new JsonObject();
        for (Object key : jsonObj1.keySet()) {
            String keyStr = (String) key;
            String valueString = jsonObj2.get(keyStr).getAsString();
            jNew.addProperty(keyStr, valueString );
        }

        return jNew;
    }

    private FileResults compareFile(File f1, File f2)
    {
        FileResults fr = new FileResults();
        fr.setFileName(f2.getName());

        //load each as hashMap, then loop through hashmaps
        HashMap<String, String> map1 = null;
        HashMap<String, String> map2 =null;
        try {
             map1 = LocaleManager.convertJsonToHashmap(
                    Utils.getResourceAsLocalFile(f1.toString()), f1.toString());
            fr.setIsValidJsonPrimary(true);
        }
        catch (Exception ex)
        {
            fr.setIsValidJsonPrimary(false);
            return fr;
        }

        try {
           map2 = LocaleManager.convertJsonToHashmap(
                    Utils.getResourceAsLocalFile(f2.toString()), f2.toString());
            fr.setIsValidJsonSecondary(true);
        }
        catch (Exception ex)
        {
            fr.setIsValidJsonSecondary(false);
            return  fr;
        }

        //VALIDATE if Test --> ensure each key starts with TT_
        if (isTestLocale(f1.getAbsolutePath())) {
            int iErrorMissingTT = 0;
            for (String key : map1.keySet()) {
                String value = map1.get(key);
                if (!value.startsWith("TT_")) {
                    //bad!
                    iErrorMissingTT++;
                    System.out.println(String.format("ERROR: Test Key does not start with TT: '%1$s'", key));
                }
            }
        }

        //have two valid JSON files - now march through and compare keys:
        //Check that Secondary has all keys from Primary
        for (String key : map1.keySet()) {
            if (!map2.containsKey(key))
            {
                //bad
                fr.addMissingKey(key);
            }
            else {
                //key may exist, but if it isn't translated (starts with "TT_", then still treat it as missing
                String value = map2.get(key);

                if (value.startsWith("TT_")) {
                    //bad
                    fr.addPlaceholderKeys(key);
                } else {
                    //good! correctly translated
                    fr.addTranslatedKey(key);
                }

            }
        }

        //Check if secondary has any extra
        for (String key : map2.keySet()) {
            if (!map1.containsKey(key))
            {
                //bad
                fr.addExtraKey(key);
            }
        }

        return fr;
    }

    //private static String getContent

    private File getFile(File[] secondaryItems, String fileNameOnly)
    {
        for (File f : secondaryItems)
        {
            if (f.getName().equals(fileNameOnly))
            {
                //found it!
                return f;
            }
        }

        return null;
    }

    //Read from source resources, as that's what inital admin will edit

    //returns path to locale: ...\nodecore-wallet-ui\src\main\resources\locale
    private String getRootLocaleFolder() {

        File f = new File("");

        //  ...\IdeaProjects\nodecore-wallet-ui
        String strRoot = (new File("")).getAbsolutePath();
        String subFolder = "src/main/resources/locale";
        String finalPath = Paths.get(strRoot, subFolder).toAbsolutePath().toString();

        if (Utils.doesFolderExist(finalPath))
        {
            //great!
            System.out.println("Reading files from: " + finalPath);
            return finalPath;
        }
        else
        {
            //bad
            System.out.println("Could not find resource folder: " + finalPath);
            return null;
        }
    }


    private class Results
    {
        public Results(String localePrimary, String localeSecondary)
        {
            _localePrimary = localePrimary;
            _localeSecondary = localeSecondary;

            _missingSecondaryFile = new ArrayList<>();
            _extraSecondaryFile = new ArrayList<>();

            _foundSecondaryFile = new ArrayList<FileResults>();

        }

        private String _localePrimary;
        private String _localeSecondary;

        private String getLocalePrimary()
        {
            return _localePrimary;
        }
        private String getLocaleSecondary()
        {
            return _localeSecondary;
        }

        private List<String> _missingSecondaryFile;
        private List<String> _extraSecondaryFile;

        private List<FileResults> _foundSecondaryFile;

        public void addMissingSecondaryFile(String value) {
            _missingSecondaryFile.add(value);
        }

        public void addExraSecondaryFile(String value) {
            _extraSecondaryFile.add(value);
        }

        public void addFileResult(FileResults fr)
        {
            _foundSecondaryFile.add(fr);
        }

        public List<String> getMissingSecondaryFiles()
        {
            return _missingSecondaryFile;
        }
        public List<String> getExtraSecondaryFile()
        {
            return _extraSecondaryFile;
        }

        public List<FileResults> getFileResults()
        {
            return _foundSecondaryFile;
        }
    }

    private class FileResults {

        public FileResults() {
            _isValidJsonPrimary = true; //Primary should be valid as admin creates it
            _isValidJsonSecondary = true;
            _missingKeys = new ArrayList<>();
            _extraKeys = new ArrayList<>();
            _translatedKeys = new ArrayList<>();
            _placeholderKeys = new ArrayList<>();
        }

        @Override
        public String toString() {
            if (_isValidJsonSecondary && _isValidJsonPrimary) {
                return String.format("%1$s: Extra:%2$s, Missing:%3$s, Placeholder:%4$s",
                        _fileName, _extraKeys.size(), _missingKeys.size(), _placeholderKeys.size());
            }
            else
            {
                return String.format("%1$s: ValidJsonPrimary:%2$s, ValidJsonSecondary::%3$s",
                        _fileName, _isValidJsonPrimary, _isValidJsonSecondary);

            }
        }

        public boolean isSuccess() {
            if (_isValidJsonPrimary && _isValidJsonSecondary
                    && _missingKeys.size() == 0 && _extraKeys.size() == 0 && _placeholderKeys.size() == 0) {
                return true;
            } else {
                return false;
            }
        }

        private String _fileName;
        private boolean _isValidJsonPrimary;
        private boolean _isValidJsonSecondary;
        private List<String> _missingKeys;
        private List<String> _extraKeys;
        private List<String> _translatedKeys;
        private List<String> _placeholderKeys;

        public void setFileName(String value)
        {
            _fileName = value;
        }
        public String getFileName()
        {
            return _fileName;
        }

        public void setIsValidJsonPrimary(boolean value)
        {
            _isValidJsonPrimary = value;
        }
        public boolean getIsValidJsonPrimary()
        {
            return _isValidJsonPrimary;
        }

        public void setIsValidJsonSecondary(boolean value)
        {
            _isValidJsonSecondary = value;
        }
        public boolean getIsValidJsonSecondary()
        {
            return _isValidJsonSecondary;
        }

        public void addMissingKey(String s)
        {
            _missingKeys.add(s);
        }
        public List<String> getMissingKeys()
        {
            return _missingKeys;
        }

        public void addPlaceholderKeys(String s)
        {
            _placeholderKeys.add(s);
        }
        public List<String> getPlaceholderKeys()
        {
            return _placeholderKeys;
        }

        public void addExtraKey(String s)
        {
            _extraKeys.add(s);
        }
        public List<String> getExtraKeys()
        {
            return _extraKeys;
        }

        public void addTranslatedKey(String s)
        {
            _translatedKeys.add(s);
        }
        public List<String> getTranslatedKeys()
        {
            return _translatedKeys;
        }

        public int getTotalKeys()
        {
            return _missingKeys.size() + _placeholderKeys.size() + _translatedKeys.size();
        }

        public int getMissingOrPlaceholderCount()
        {
            return _missingKeys.size() + _placeholderKeys.size();
        }

        public boolean doesEntireFileNeedTranslating()
        {
            //No extra keys, nothing translated yet, everything is Missing/Placeholder
            if (getExtraKeys().size() == 0 && getTranslatedKeys().size() == 0
                    && getMissingOrPlaceholderCount() > 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean isJsonValid()
        {
            return (getIsValidJsonPrimary() && getIsValidJsonSecondary());
        }

    }

}

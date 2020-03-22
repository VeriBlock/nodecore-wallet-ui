// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final Logger _logger = LoggerFactory.getLogger(Utils.class);

    public static void openFolder(String folderPath)
    {
        try
        {
            // Create Desktop object
            Desktop desktop = Desktop.getDesktop();

            // Browse a URL, say google.com
            desktop.open(new File(folderPath));
        }
        catch (Exception ex)
        {
            //swallow
            _logger.info("Could not open folder '{}', exception={}", folderPath, ex.toString());
        }
    }


    public static void openLink(String url)
    {
        try
        {
            // Create Desktop object
            Desktop desktop = Desktop.getDesktop();

            // Browse a URL, say google.com
            desktop.browse(new URI(url));
        }
        catch (Exception ex)
        {
            //swallow
        }
    }

    //https://coderwall.com/p/ab5qha/convert-json-string-to-pretty-print-java-gson
    public static String toJsonPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    public static String toJsonPrettyFormat(JsonObject json)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    //region DateTime

    public static DateTime convertFromEpoch(long epoch2)
    {
        long timeStamp = epoch2 * 1000;
        Date date = new Date(timeStamp);
        return new DateTime(date);
    }

    //Get total seconds from dt1 to dt2
    public static int getSecondsDiff(DateTime dt1, DateTime dt2)
    {
        int seconds = (int)((dt2.toDate().getTime() - dt1.toDate().getTime()) / 1000 );
        return seconds;
    }

    //Formats as "11:26:06 AM"
    public static String getTimeOfDayNow()
    {
        String s = DateTime.now().toString("hh:mm:ss a");
        return s;
    }

    public static long getEpochCurrent()
    {

        Instant instant = Instant.now();
        long timeStampSeconds = instant.getEpochSecond();
        return timeStampSeconds;
    }

    //endregion

    //region dataType

    public static boolean isLong(String s) {

        try {
            Long.parseLong(s);
            return true;
        } catch (Exception ex)
        {
        }
        return false;
    }

    public static double parseDouble(String input, double valueIfBadInput) {
        if (input == null || input.length() == 0) {
            return valueIfBadInput;
        }

        try {
            return Double.parseDouble(input);
        } catch (Exception ex) {
            return valueIfBadInput;
        }
    }

    public static boolean parseBoolean(String input, boolean valueIfBadInput) {
        if (input == null || input.length() == 0) {
            return valueIfBadInput;
        }

        try {
            return Boolean.parseBoolean(input);
        } catch (Exception ex) {
            return valueIfBadInput;
        }
    }
    public static boolean isBoolean(String input)
    {
        try {
            Boolean.parseBoolean(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    //endregion

    //Expects NC versions of the form: 0.3.11-rc1
    //Return true
    public static boolean isHigherNodeCoreVersion(String actualVersion, String minVersion)
    {
        //Treat nulls as wild card
        if (actualVersion == null || actualVersion.length() == 0)
        {
            return true;
        }
        if (minVersion == null || minVersion.length() == 0)
        {
            return true;
        }

        long actual = parseNodeCoreVersion(actualVersion);
        long min = parseNodeCoreVersion(minVersion);
        if (actual == 0 || min == 0)
        {
            return true;
        }

        //Finally:
        return actual >= min;
    }
    private static long parseNodeCoreVersion(String version)
    {
        //1.3.0
        //0.3.11
        //0.3.11-rc1
        //0.3.11-rc.2.dev.35.uncommitted+popminer.16672a4

        //if malformed, then user by-passed convention (renamed), and just default to true.
        //Not a security risk, rather a user convenience
        long result = 0;
        try
        {
             String[] arr = version.split("\\.", -1);
             if (arr.length < 3)
             {
                 //bad
                 return 0;
             }
             int iPart1 = Integer.parseInt(arr[0]);
             int iPart2 = Integer.parseInt(arr[1]);
             String part3 = arr[2];
             int iHyphen = part3.indexOf("-");
             if (iHyphen > 0)
             {
                 part3 = part3.substring(0, iHyphen);
             }
            int iPart3 = Integer.parseInt(part3);
            result = iPart1 * 1000 * 1000
                    + iPart2 * 1000
                    + iPart3;
        }
        catch (Exception ex)
        {
            return 0;
        }

        return result;
    }

    //region File IO

    public static String getCurrentDirectory()
    {
        return System.getProperty("user.dir");
    }

    public static void writeFile(String strContent, String strPath)
    {
        try {
            FileWriter fw = new FileWriter(strPath);
            fw.write(strContent);
            fw.close();
        }
        catch ( Exception e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

    public static void writeFileUtf(String strContent, String strPath) {
        //https://stackoverflow.com/questions/1001540/how-to-write-a-utf-8-file-with-java
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(strPath), "UTF-8"));
            try {
                out.write(strContent);
            } finally {
                out.close();
            }
        } catch (Exception ex) {
            //swallow
            System.out.println("Error: " + ex.toString());
        }
    }

    public static String getCanonicalFolderPath(String folder)
    {
        //will collapse ../../
        try {
            String s2 = (new File(folder)).getCanonicalPath().toString();
            return s2;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static boolean doesFileExist(String fileName)
    {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean doesFolderExist(String directoryName)
    {
        File file = new File(directoryName);
        if (file.exists() && file.isDirectory()) {
            return true;
        }
        else
        {
            return false;
        }
    }

    //No change if folder already exists. Else creates folder.
    public static void createFolder(String directoryName)
    {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    //https://stackoverflow.com/questions/285712/java-reading-a-file-into-an-array
    public static String[] readLines(String filename)  {
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            return lines.toArray(new String[lines.size()]);
        }
        catch (Exception ex)
        {
            return new String[0];
        }
    }

    public static String readAllText(String filename)  {
        String text = "";
        try
        {
            text = new String(Files.readAllBytes(Paths.get(filename)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return text;
    }

    //endregion

    //region Resources

    public static String resourceAsExternal(String resource) {
        String result = Utils.class.getClassLoader().getResource(resource).toExternalForm();
        return result;
    }

    //Must handle UTF-8 encoding
    public static String getResourceAsLocalFile(String resourcePath)
    {
        //assume local folder is just copied
        String content = null;
        try {
            InputStream inputStream = new FileInputStream(resourcePath);
            Reader reader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));

            int data = reader.read();
            StringBuilder sb = new StringBuilder();
            while (data != -1) {
                char theChar = (char) data;
                data = reader.read();

                sb.append(theChar);
            }

            reader.close();
            content = sb.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return content;
    }

    public static String getResourceFileAsString(String resourcePath) {
        String s = null;
        try {
            InputStream stream = Utils.class.getClassLoader().getResourceAsStream(resourcePath);
            byte[] raw = new byte[stream.available()];
            stream.read(raw);
            s = new String(raw, "UTF-8");
        } catch (Exception ex) {
        }
        return s;
    }

    public static  String readContentFromBIS(BufferedInputStream in) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] contents = new byte[1024];
            int bytesRead = 0;

            while ((bytesRead = in.read(contents)) != -1) {
                sb.append(new String(contents, 0, bytesRead));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return sb.toString();
    }

    //endregion

    public static String getExcetionToString(Exception ex)
    {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return exceptionAsString;
    }

    public static void waitNSeconds(int seconds)
    {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch (Exception ex)
        {

        }
    }

    public static void waitNMilliSeconds(int milliseconds)
    {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        }
        catch (Exception ex)
        {

        }
    }

    //returns empty string if Java object is null, else returns toString()
    public static String isNull(Object object)
    {
        if (object == null)
        {
            return "";
        }
        else {
            return object.toString();
        }
    }

}

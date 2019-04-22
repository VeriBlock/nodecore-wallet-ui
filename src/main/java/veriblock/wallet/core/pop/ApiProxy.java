// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import veriblock.wallet.core.pop.entities.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ApiProxy {

    public ApiProxy(String baseUrl)
    {
        _baseUrl = baseUrl;
    }

    private String _baseUrl;

    private String getUrl(String command)
    {
        return  _baseUrl + "/api/" + command;
    }

    public boolean isConnected()
    {
        //http://127.0.0.1:8600/api/config

        //Does not check miner properties as that could get SEED.

        ConfigEntity configTest = this.getConfig();
        //TODO - better api check?
        if (configTest != null && configTest.bitcoinFeeMax != 0) {
            return true;
        }
        else
        {
            return false;
        }
    }

    ///api/config
    //Returns null if not found (such as service not running)
    public ConfigEntity getConfig()
    {
        //HttpResponseMessage response = await client.PutAsync("/api/config", new StringContent(body.ToString(), Encoding.UTF8, "application/json"));
        //response.EnsureSuccessStatusCode();
        //
        String fullUrl = getUrl("config");
        String strContent = getUrlContent(fullUrl);
        if (strContent == null || strContent.length() == 0)
        {
            return null;
        }

        JsonObject json = getUrlJson(strContent);
        ConfigEntity entity = PopEntityParser.parseConfigEntity(json);

        return entity;
    }

    public List<OperationEntity> getOperations() {
        List<OperationEntity> results = new ArrayList<>();

        String fullUrl = getUrl("operations");
        String strContent = getUrlContent(fullUrl);
        if (strContent == null || strContent.length() == 0) {
            return null;
        }

        try {
            JsonArray jsonArray = getUrlJsonArray(strContent);
            for (JsonElement item : jsonArray) {
                JsonObject jo = item.getAsJsonObject();
                results.add(PopEntityParser.parseOperationEntity(jo));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return results;
    }


    public SetConfigEntity setConfig(ConfigConstants.Key configKey, String value )
    {
        String key = ConfigConstants.getKeyString(configKey);
        String fullUrl = getUrl("config");

        String input = "{\n" +
                "    \"key\": \"" + key + "\",\n" +
                "    \"value\": \"" + value + "\"\n" +
                "}";
        String strContent = doPut(fullUrl, input);
        JsonObject json = getUrlJson(strContent);

        SetConfigEntity result = PopEntityParser.parseSetConfigEntity(json);
        return result;
    }

    public MineResultEntity mine()
    {
        return mine(-1);
    }

    public MineResultEntity mine(int block)
    {
        String fullUrl = getUrl("mine");

        String input = "{}";
        if (block > 0)
        {
            input = "{\n" +
                    "    \"block\": " + Integer.toString(block) + "\n" +
                    "}";
        }

        String strContent = doPost(fullUrl, input);
        JsonObject json = getUrlJson(strContent);

        MineResultEntity result = PopEntityParser.parseMineResultEntity(json);
        return result;
    }

    public OperationDetailEntity getOperation(String operationId)
    {
        String fullUrl = getUrl("operations/" + operationId);
        String strContent = getUrlContent(fullUrl);
        if (strContent == null)
        {
            return null;
        }

        JsonObject json = getUrlJson(strContent);

        OperationDetailEntity result = PopEntityParser.parseOperationDetailEntity(json);
        return result;
    }

    public MinerPropertiesEntity getMinerPropertiesEntity()
    {
        String fullUrl = getUrl("miner");
        String strContent = getUrlContent(fullUrl);
        if (strContent == null)
        {
            return null;
        }

        JsonObject json = getUrlJson(strContent);

        MinerPropertiesEntity result = PopEntityParser.parseMinerPropertiesEntity(json);
        return result;
    }

    //region Plumbing

    //TODO --> set up the http object once
    private static JsonObject getUrlJson(String strContent) {

        if (strContent == null)
        {
            return null;
        }
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(strContent);
        JsonObject rootobj = root.getAsJsonObject();

        return rootobj;
    }

    private static JsonArray getUrlJsonArray(String strContent) {

        if (strContent == null)
        {
            return null;
        }
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(strContent);

        JsonArray arr = root.getAsJsonArray();

        return arr;
    }



    private static String doPut(String fullUrl, String input) {
        return doPostPut(fullUrl, input, "PUT");
    }

    private static String doPost(String fullUrl, String input) {
        return doPostPut(fullUrl, input, "POST");
    }
    //TODO - extract to utility
    //https://www.journaldev.com/7148/java-httpurlconnection-example-java-http-request-get-post
    private static String doPostPut(String fullUrl, String input, String httpVerb) {
        String result = null;
        try {
            URL obj = new URL(fullUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(httpVerb);
            //con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "application/json");

            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            int responseCode = con.getResponseCode();
            //System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                result =  response.toString();
                //System.out.println(response.toString());
            } else {
                //System.out.println("POST request not worked");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    //TODO - extract to utility
    private static String getUrlContent(String url)
    {
        int timeoutSeconds = 2;

        String result = null;
        try
        {
            //http://chillyfacts.com/java-send-http-getpost-request-and-read-json-response/
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(timeoutSeconds * 1000);
            con.setReadTimeout(timeoutSeconds * 1000);
            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print in String
            result = response.toString();
        }
        catch (Exception ex)
        {
            //Very reasonable that PoP Miner isn't running, so no endpoint and this crashes
            //null;   //result = "ERROR: " + ex.getMessage();
            //ex.printStackTrace();
        }

        return result;
    }

    //endregion
}

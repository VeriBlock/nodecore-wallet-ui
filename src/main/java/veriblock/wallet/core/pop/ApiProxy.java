// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop;

import com.google.gson.*;
import veriblock.wallet.core.pop.entities.*;
import veriblock.wallet.core.pop.entities.OperationSummaryListEntity;
import veriblock.wallet.core.pop.entities.OperationSummaryEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ApiProxy {
    private static final Gson gson = new Gson();

    public ApiProxy(String baseUrl) {
        _baseUrl = baseUrl;
    }

    private String _baseUrl;

    private String getUrl(String command) {
        return  _baseUrl + "/api/" + command;
    }

    public boolean isConnected() {
        //TODO - better api check?
        final BtcFeeConfigEntity configTest = getBtcFeeConfig();
        return configTest != null && configTest.maxFee != 0;
    }

    public AutoMineConfigEntity getAutoMineConfig() {
        final String fullUrl = getUrl("config/automine");
        final String strContent = getUrlContent(fullUrl);
        if (strContent == null || strContent.length() == 0) {
            return null;
        }
        final AutoMineConfigEntity entity = gson.fromJson(strContent, AutoMineConfigEntity.class);
        return entity;
    }

    public BtcFeeConfigEntity getBtcFeeConfig() {
        final String fullUrl = getUrl("config/btc-fee");
        final String strContent = getUrlContent(fullUrl);
        if (strContent == null || strContent.length() == 0) {
            return null;
        }
        final BtcFeeConfigEntity entity = gson.fromJson(strContent, BtcFeeConfigEntity.class);
        return entity;
    }

    public List<OperationSummaryEntity> getOperations() {
        String fullUrl = getUrl("operations");
        String strContent = getUrlContent(fullUrl);
        if (strContent == null || strContent.length() == 0) {
            return null;
        }

        final OperationSummaryListEntity operationSummaryListEntity = gson.fromJson(strContent, OperationSummaryListEntity.class);
        return operationSummaryListEntity.operations;
    }

    public void setAutoMineConfig(
            AutoMineConfigEntity autoMineConfigEntity
    ) {
        final String fullUrl = getUrl("config/automine");
        String input = gson.toJson(autoMineConfigEntity);
        String strContent = doPut(fullUrl, input);
    }

    public void setBtcFee(
            BtcFeeConfigEntity btcFeeConfigEntity
    ) {
        final String fullUrl = getUrl("config/btc-fee");
        String input = gson.toJson(btcFeeConfigEntity);
        String strContent = doPut(fullUrl, input);
    }

    public MineResultEntity mine()
    {
        return mine(-1);
    }

    public MineResultEntity mine(int block) {
        final String fullUrl = getUrl("mine");

        String input = "{}";
        if (block > 0)
        {
            input = "{\n" +
                    "    \"block\": " + Integer.toString(block) + "\n" +
                    "}";
        }
        final String strContent = doPost(fullUrl, input);
        final MineResultEntity mineResultEntity = gson.fromJson(strContent, MineResultEntity.class);
        return mineResultEntity;
    }

    public OperationDetailEntity getOperation(String operationId) {
        String fullUrl = getUrl("operations/" + operationId);
        String strContent = getUrlContent(fullUrl);
        if (strContent == null) {
            return null;
        }

        final OperationDetailEntity operationDetailEntity = gson.fromJson(strContent, OperationDetailEntity.class);
        return operationDetailEntity;
    }

    public MinerPropertiesEntity getMinerPropertiesEntity() {
        final String fullUrl = getUrl("miner");
        final String strContent = getUrlContent(fullUrl);
        if (strContent == null) {
            return null;
        }

        final MinerPropertiesEntity result = gson.fromJson(strContent, MinerPropertiesEntity.class);
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

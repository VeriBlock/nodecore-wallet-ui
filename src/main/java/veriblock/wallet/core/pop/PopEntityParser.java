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
import veriblock.wallet.core.pop.entities.*;

public class PopEntityParser {

    public static ConfigEntity parseConfigEntity(JsonObject json)
    {
        /*
        {
          "auto.mine.round1": "false",
          "pop.cron.schedule": "",
          "bitcoin.fee.max": "9000",
          "bitcoin.fee.perkb": "20000",
          "http.api.port": "8600",
          "bitcoin.minrelayfee.enabled": "true",
          "nodecore.rpc.password": "",
          "nodecore.rpc.ssl": "false",
          "nodecore.rpc.cert.chain.path": "",
          "http.api.address": "127.0.0.1",
          "bitcoin.network": "mainnet",
          "nodecore.rpc.host": "127.0.0.1",
          "pop.action.timeout": "90",
          "nodecore.rpc.port": "10501",
          "auto.mine.round4": "false",
          "auto.mine.round3": "false",
          "auto.mine.round2": "false"
        }
         */

        ConfigEntity entity = new ConfigEntity();
        try {
            entity.bitcoinFeeMax = json.get("bitcoin.fee.max").getAsInt();
            entity.bitcoinFeePerKb = json.get("bitcoin.fee.perkb").getAsInt();

            entity.autoMineRound1 = json.get("auto.mine.round1").getAsBoolean();
            entity.autoMineRound2 = json.get("auto.mine.round2").getAsBoolean();
            entity.autoMineRound3 = json.get("auto.mine.round3").getAsBoolean();
            entity.autoMineRound4 = json.get("auto.mine.round4").getAsBoolean();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    public static MineResultEntity parseMineResultEntity(JsonObject json)
    {

        /*
        String strContent = "{\n" +
                "              \"operationId\": \"7db63161\",\n" +
                "              \"failed\": false,\n" +
                "              \"messages\": [\n" +
                "                {\n" +
                "                  \"code\": \"V201\",\n" +
                "                  \"message\": \"Mining operation started\",\n" +
                "                  \"details\": [\n" +
                "                    \"To view details, run command: getoperation 7db63161\"\n" +
                "                  ],\n" +
                "                  \"error\": false\n" +
                "                }\n" +
                "              ]\n" +
                "            }";
                */

        MineResultEntity entity = new MineResultEntity();

        try {
            entity.operationId = getString(json, "operationId");
            entity.failed = json.get("failed").getAsBoolean();
            entity.messages = getMessageEntities(json);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return entity;
    }

    public static SetConfigEntity parseSetConfigEntity(JsonObject json)
    {
        /*
        {
            "failed": false,
            "messages": [
                {
                    "code": "V200",
                    "message": "Success",
                    "error": false
                }
            ]
        }
         */

        SetConfigEntity entity = new SetConfigEntity();
        entity.failed =  json.get("failed").getAsBoolean();
        entity.messages = getMessageEntities(json);
        return entity;
    }

    private static MessageEntity[] getMessageEntities(JsonObject json) {

        JsonArray arr = json.get("messages").getAsJsonArray();
        MessageEntity[] result = new MessageEntity[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            JsonObject item = arr.get(i).getAsJsonObject();
            MessageEntity message = new MessageEntity();

            try {
                message.code = getString(item, "code");
                message.message = getString(item, "message");

                //could be an array
                message.details = cleanWhitespace(getString(item, "details"));

                message.error = item.get("error").getAsBoolean();
            } catch (Exception ex) {

            }
            result[i] = message;
        }

        return result;
    }

    public static OperationEntity parseOperationEntity(JsonObject jo) {
        /*
              {
                "operationId": "0ec9aed9",
                "endorsedBlockNumber": 379683,
                "status": "RUNNING",
                "actionMessage": "Waiting for transaction to be included in Bitcoin block",
                "message": ""
              },
        */
        OperationEntity entity = new OperationEntity();
        entity.operationId = jo.get("operationId").getAsString();
        entity.endorsedBlockNumber = getInt(jo, "endorsedBlockNumber");
        entity.status = parsePopStatusEnum(getString(jo, "state"));
        entity.actionMessage = getString(jo, "action");
        entity.message = getString(jo, "message");

        return entity;
    }

    public static OperationDetailEntity parseOperationDetailEntity(JsonObject jo) {
        /*
            {
              "operationId": "942bd892",
              "status": "RUNNING",
              "currentAction": "CONFIRM",
              "popData": {
                "publicationData": "000621590001A9276EA06EE1D5DD3054D63A372C551A3C2498E911C5A1B8C524AC65F37FA246ABFD5637A8441D7CEF09D48DD90E5C5A1348065D0B1E7C1B979A90BB9002371455422F0231D659722F3F",
                "endorsedBlockHeader": "000621590001A9276EA06EE1D5DD3054D63A372C551A3C2498E911C5A1B8C524AC65F37FA246ABFD5637A8441D7CEF09D48DD90E5C5A1348065D0B1E7C1B979A",
                "minerAddress": "V9uC3ywS12qW4ESD9783eZXVU3voMp"
              },
              "transaction": "010000000103CE28502B3200CE428EA653E2C57554E065E055BF85BB5EDDE0AE3C6D161BC0010000006B483045022100C33B1FA4C18EFABAE4C6C0C89806F4B3753429254DCB20D7FE17CFD30140A3A002200B844D2EB93856ACD8F37AFCE2CF338AA5A1ABAFD6F6F22DE08C97D70A522114012102871477DBB1B528FBF24B55933BFD74A23A79CD16FC26BE68B72BE1253AB0CF95FFFFFFFF020000000000000000536A4C50000621590001A9276EA06EE1D5DD3054D63A372C551A3C2498E911C5A1B8C524AC65F37FA246ABFD5637A8441D7CEF09D48DD90E5C5A1348065D0B1E7C1B979A90BB9002371455422F0231D659722F3FB0310500000000001976A9146D14D3AF53688AD384A01912C16723D9F32F07C988AC00000000",
              "submittedTransactionId": "cf3ac25e52bf88a1fa1093932f74410e6b9f5cf16dd32b1dd9cb8c07b311d819",
              "bitcoinBlockHeaderOfProof": "00000020350A54A2CBD35C1D5CF7FFE942AD0A5C4E6203E52847010000000000000000003A8F7E4B359D9AB4160EC657FE928F8C4ABA991F583A7193BE954D4C9C3DFE275E155A5C356830174A7C265F",
              "merklePath": "1111:19D811B3078CCBD91D2BD36DF15C9F6B0E41742F939310FAA188BF525EC23ACF:8E154286058DCEC3E444553A10A76EB94103BD90932C61FE6E55C2E9BDF4F7EB:6CA1029A496A0977FCD68665658C307B79319EC8793691E8EA82DA0BD72E0BD2:F57A93DA682009F84A706F9FC3A58BCCD56E8EA5D706A13D1EA796FE7E9DFFCE:F8F8A1217ABF8AB0A64F5030ECDE761DCB2903E057E87D5F8B96D69B3A58F4BA:4A8B6B7E9C3C3758AF46AC67AD0A5777B94F88F2181E75C9FA6D4457D8AFE5B3:FB2C74981550CF50BB4AF28E70FCCF69716CEE4EF1F8BF81395438249E1C701D:A94DA6B3D16649936D24F713639BADE8084BAE251CE1557AC91F31651F393602:90A472757EFD34910F48FA1926A71F219239F48E33F826E836EE39E0F6B62C41:C0E139E0BB43FB788930BD1CB878D2ABA1D725A36EAAA2085909A4157EB79670:AD14B82EE963851F988A79441806189F1CE44EC6D709645337248DD436CAB37E:12D0C95EABDBAC5DF2F0D9204817B60CAFABD6FA238B146E3D3C6B91302A6912:11DD0A03AC7B4FBC4FB69452D5731D16D281AC6464CEA0EB748F6976605B1BE5",
              "detail": "",
              "popTransactionId": "1F490595F4CBBE41ACBF076834D31CDBFBC3011E3555C991D1A40B2D5B84F820"
            }
        */

        OperationDetailEntity entity = new OperationDetailEntity();
        try {
            entity.operationId = jo.get("operationId").getAsString();
            entity.status = parsePopStatusEnum(jo.get("status").getAsString());
            entity.currentAction = parsePopActionEnum(getString(jo, "currentAction"));
            entity.btcTransactionId = getString(jo, "submittedTransactionId");
            entity.merklePath = getString(jo, "merklePath");
            entity.detail = cleanWhitespace(getString(jo, "detail"));
            entity.vbkPopTransactionId = getString(jo, "popTransactionId");
            entity.operationId = jo.get("operationId").getAsString();

            if (jo.get("popData") != null)
            {
                JsonObject joPopDate = jo.get("popData").getAsJsonObject();
                entity.opReturnHex = joPopDate.get("publicationData").getAsString();
                entity.vbkMinerAddress = joPopDate.get("minerAddress").getAsString();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return entity;
    }

    public static MinerPropertiesEntity parseMinerPropertiesEntity(JsonObject jo) {
        /*
            {
              "minerAddress": "V9uC3ywS12qW4ESD9783eZXVU3voMp",
              "bitcoinAddress": "1CRb4b5u2AYdaoovgV9aQWV3kSt9x66rJm",
              "bitcoinBalance": 0,
              "walletSeed": [
                "1548958861",
                "cave",
                "conduct",
                "pottery"
              ]
            }
        */

        MinerPropertiesEntity entity = new MinerPropertiesEntity();
        try {
            entity.minerAddress = getString(jo, "minerAddress");
            entity.bitcoinAddress = getString(jo, "bitcoinAddress");
            entity.bitcoinBalance = getLong(jo, "bitcoinBalance");

            JsonElement seeds = jo.get("walletSeed");
            String[] astr = null;
            if (seeds != null) {
                JsonArray arr = seeds.getAsJsonArray();
                astr = new String[arr.size()];
                for (int i = 0; i < astr.length; i++) {
                    astr[i] = arr.get(i).getAsString();
                }
            }
            entity.walletSeeds = astr;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entity;
    }

    //region utils

    private static String cleanWhitespace(String s)
    {
        if (s == null)
        {
            return null;
        }
        else
        {
            //Remove tab and newline, replace with single space
            return s.trim()
                    .replace("\t", " ")
                    .replace("\n", " ");
        }
    }

    private static String getString(JsonObject jo, String memberName)
    {
        JsonElement e = jo.get(memberName);
        if (e != null)
        {
            return e.getAsString();
        }
        else
        {
            return null;
        }
    }

    private static int getInt(JsonObject jo, String memberName)
    {
        JsonElement e = jo.get(memberName);
        if (e != null)
        {
            return e.getAsInt();
        }
        else
        {
            return 0;
        }
    }

    private static long getLong(JsonObject jo, String memberName)
    {
        JsonElement e = jo.get(memberName);
        if (e != null)
        {
            return e.getAsLong();
        }
        else
        {
            return 0;
        }
    }

    public static PopStatus parsePopStatusEnum(String status)
    {
      /*
        STARTED,
        RUNNING,
        COMPLETE,
        FAILED
       */
        status = status.toUpperCase();

        switch (status)
        {
            case "STARTED":
                return PopStatus.STARTED;
            case "RUNNING":
                return PopStatus.RUNNING;
            case "COMPLETE":
                return PopStatus.COMPLETE;
            case "FAILED":
                return PopStatus.FAILED;
            default:
                return PopStatus.UNKNOWN;

        }
    }

    public static PopAction parsePopActionEnum(String status)
    {
        status = status.toUpperCase();

        switch (status) {
            case "READY":
                return PopAction.READY;
            case "PUBLICATION_DATA":
                return PopAction.PUBLICATION_DATA;
            case "TRANSACTION":
                return PopAction.TRANSACTION;
            case "WAIT":
                return PopAction.WAIT;
            case "PROOF":
                return PopAction.PROOF;
            case "CONTEXT":
                return PopAction.CONTEXT;
            case "SUBMIT":
                return PopAction.SUBMIT;
            case "CONFIRM":
                return PopAction.CONFIRM;
            case "DONE":
                return PopAction.DONE;
            default:
                return PopAction.UNKNOWN;

        }
    }
    //endregion
}

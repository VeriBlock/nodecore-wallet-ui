// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.tests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;
import veriblock.wallet.core.pop.*;
import veriblock.wallet.core.pop.entities.MineResultEntity;
import veriblock.wallet.core.pop.entities.MinerPropertiesEntity;
import veriblock.wallet.core.pop.entities.OperationDetailEntity;


public class PopEntityParserTest {

    @Test
    public void parseMineResultEntity_1() {


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
        JsonObject json = parseFromString(strContent);

        MineResultEntity result = PopEntityParser.parseMineResultEntity(json);


        Assert.assertEquals("7db63161", result.operationId);
        Assert.assertEquals(false, result.failed);
    }

    private static JsonObject parseFromString(String strContent)
    {
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(strContent);
        JsonObject json = root.getAsJsonObject();
        return json;
    }

    @Test
    public void parseMinerPropertiesEntity_1() {

        String strContent = "{\n" +
                "  \"minerAddress\": \"V9uC3ywS12qW4ESD9783eZXVU3voMp\",\n" +
                "  \"bitcoinAddress\": \"1CRb4b5u2AYdaoovgV9aQWV3kSt9x66rJm\",\n" +
                "  \"bitcoinBalance\": 2345,\n" +
                "  \"walletSeed\": [\n" +
                "    \"1548958861\",\n" +
                "    \"cave\",\n" +
                "    \"conduct\",\n" +
                "    \"pottery\",\n" +
                "    \"skull\",\n" +
                "    \"shop\",\n" +
                "    \"evoke\",\n" +
                "    \"hold\",\n" +
                "    \"drastic\",\n" +
                "    \"flip\",\n" +
                "    \"disagree\",\n" +
                "    \"rebel\",\n" +
                "    \"damp\"\n" +
                "  ]\n" +
                "}";
        JsonObject json = parseFromString(strContent);

        MinerPropertiesEntity result = PopEntityParser.parseMinerPropertiesEntity(json);

        Assert.assertEquals("V9uC3ywS12qW4ESD9783eZXVU3voMp", result.minerAddress);
        Assert.assertEquals("1CRb4b5u2AYdaoovgV9aQWV3kSt9x66rJm", result.bitcoinAddress);
        Assert.assertEquals(2345, result.bitcoinBalance);
        Assert.assertEquals("damp", result.walletSeeds[12]);
    }

    @Test
    public void parseOperationDetailEntity_1() {

        String strContent = "// 20190205193637\n" +
                "// http://localhost:8600/api/operations/942bd892\n" +
                "\n" +
                "{\n" +
                "  \"operationId\": \"942bd892\",\n" +
                "  \"status\": \"RUNNING\",\n" +
                "  \"currentAction\": \"CONFIRM\",\n" +
                "  \"popData\": {\n" +
                "    \"publicationData\": \"000621590001A9276EA06EE1D5DD3054D63A372C551A3C2498E911C5A1B8C524AC65F37FA246ABFD5637A8441D7CEF09D48DD90E5C5A1348065D0B1E7C1B979A90BB9002371455422F0231D659722F3F\",\n" +
                "    \"endorsedBlockHeader\": \"000621590001A9276EA06EE1D5DD3054D63A372C551A3C2498E911C5A1B8C524AC65F37FA246ABFD5637A8441D7CEF09D48DD90E5C5A1348065D0B1E7C1B979A\",\n" +
                "    \"minerAddress\": \"V9uC3ywS12qW4ESD9783eZXVU3voMp\"\n" +
                "  },\n" +
                "  \"transaction\": \"010000000103CE28502B3200CE428EA653E2C57554E065E055BF85BB5EDDE0AE3C6D161BC0010000006B483045022100C33B1FA4C18EFABAE4C6C0C89806F4B3753429254DCB20D7FE17CFD30140A3A002200B844D2EB93856ACD8F37AFCE2CF338AA5A1ABAFD6F6F22DE08C97D70A522114012102871477DBB1B528FBF24B55933BFD74A23A79CD16FC26BE68B72BE1253AB0CF95FFFFFFFF020000000000000000536A4C50000621590001A9276EA06EE1D5DD3054D63A372C551A3C2498E911C5A1B8C524AC65F37FA246ABFD5637A8441D7CEF09D48DD90E5C5A1348065D0B1E7C1B979A90BB9002371455422F0231D659722F3FB0310500000000001976A9146D14D3AF53688AD384A01912C16723D9F32F07C988AC00000000\",\n" +
                "  \"submittedTransactionId\": \"cf3ac25e52bf88a1fa1093932f74410e6b9f5cf16dd32b1dd9cb8c07b311d819\",\n" +
                "  \"bitcoinBlockHeaderOfProof\": \"00000020350A54A2CBD35C1D5CF7FFE942AD0A5C4E6203E52847010000000000000000003A8F7E4B359D9AB4160EC657FE928F8C4ABA991F583A7193BE954D4C9C3DFE275E155A5C356830174A7C265F\",\n" +
                "  \"merklePath\": \"1111:19D811B3078CCBD91D2BD36DF15C9F6B0E41742F939310FAA188BF525EC23ACF:8E154286058DCEC3E444553A10A76EB94103BD90932C61FE6E55C2E9BDF4F7EB:6CA1029A496A0977FCD68665658C307B79319EC8793691E8EA82DA0BD72E0BD2:F57A93DA682009F84A706F9FC3A58BCCD56E8EA5D706A13D1EA796FE7E9DFFCE:F8F8A1217ABF8AB0A64F5030ECDE761DCB2903E057E87D5F8B96D69B3A58F4BA:4A8B6B7E9C3C3758AF46AC67AD0A5777B94F88F2181E75C9FA6D4457D8AFE5B3:FB2C74981550CF50BB4AF28E70FCCF69716CEE4EF1F8BF81395438249E1C701D:A94DA6B3D16649936D24F713639BADE8084BAE251CE1557AC91F31651F393602:90A472757EFD34910F48FA1926A71F219239F48E33F826E836EE39E0F6B62C41:C0E139E0BB43FB788930BD1CB878D2ABA1D725A36EAAA2085909A4157EB79670:AD14B82EE963851F988A79441806189F1CE44EC6D709645337248DD436CAB37E:12D0C95EABDBAC5DF2F0D9204817B60CAFABD6FA238B146E3D3C6B91302A6912:11DD0A03AC7B4FBC4FB69452D5731D16D281AC6464CEA0EB748F6976605B1BE5\",\n" +
                "  \"detail\": \"\",\n" +
                "  \"popTransactionId\": \"1F490595F4CBBE41ACBF076834D31CDBFBC3011E3555C991D1A40B2D5B84F820\"\n" +
                "}";
        JsonObject json = parseFromString(strContent);

        OperationDetailEntity result = PopEntityParser.parseOperationDetailEntity(json);

        Assert.assertEquals("942bd892", result.operationId);
        Assert.assertEquals(PopStatus.RUNNING, result.status);
        Assert.assertEquals(PopAction.CONFIRM, result.currentAction);
        Assert.assertEquals("1F490595F4CBBE41ACBF076834D31CDBFBC3011E3555C991D1A40B2D5B84F820", result.vbkPopTransactionId);
        Assert.assertEquals("cf3ac25e52bf88a1fa1093932f74410e6b9f5cf16dd32b1dd9cb8c07b311d819", result.btcTransactionId);
        Assert.assertTrue(result.opReturnHex.length() > 0);
    }
}

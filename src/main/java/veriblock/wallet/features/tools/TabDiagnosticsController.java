// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.tools;

import com.google.gson.GsonBuilder;
import org.veriblock.core.utilities.DiagnosticInfo;
import org.veriblock.core.utilities.DiagnosticUtility;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.entities.StateInfoEntity;
import javafx.scene.control.*;
import veriblock.wallet.features.LocaleModuleResource;

public class TabDiagnosticsController  extends BaseController  {

    public void initialize() {

    }

    private LocaleModule _localeModule;

    public void init() {
        loadDiagnostics();
        setLocale();
    }

    public Label lblMessage;
    public TextArea txtResults;

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabDiagnostics);
        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));
        lblMessage.setText(_localeModule.getString("label.top"));
    }

    private void loadDiagnostics() {
        //If connected to NodeCore --> great tell about that

        CommandResult<StateInfoEntity> commandResult = NodeCoreGateway.getInstance().getGetStateInfo();

        String nodeCoreStateInfo = null;
        String nodeCoreDiag = null;
        if (!commandResult.isSuccess())
        {
            nodeCoreStateInfo = "\"NOT_CONNECTED\"";
            nodeCoreDiag = "\"NOT_CONNECTED\"";
        }
        else
        {
            //Get NC connection info
            nodeCoreStateInfo = (new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(commandResult.getPayload()));
            nodeCoreDiag =  NodeCoreGateway.getInstance().getDiagnosticInfoRaw();
        }

        DiagnosticInfo di = DiagnosticUtility.getDiagnosticInfo();
        String walletAppDiag = (new GsonBuilder().setPrettyPrinting().create().toJson(di));

        OtherData otherData = getOtherData();
        String otherDataString = (new GsonBuilder().setPrettyPrinting().create().toJson(otherData));

        //Format as JSON
        String result = String.format("{\n" +
                "  \"walletApp\": %1$s,\n" +
                "  \"otherData\": %2$s,\n" +
                "  \"nodeCoreStateInfo\": %3$s,\n" +
                "  \"nodeCoreDiagnostics\": %4$s\n" +
                "}",
                walletAppDiag, otherDataString, nodeCoreStateInfo, nodeCoreDiag);

        //reformat
        result = Utils.toJsonPrettyFormat(result);

        //remove any final double '\\'
        //C:\\Users\\....\\nodecore-wallet-ui",
        result = result.replace("\\\\", "\\");
        this.txtResults.setText(result);

    }

    private OtherData getOtherData()
    {
        OtherData o = new OtherData();

        o.settingsDirectory = (new FileManager()).getRootDirectory();
        o.logDirectory = (new FileManager()).getLogDirectory();

        return o;
    }

    public class OtherData
    {
        public String settingsDirectory;
        public String logDirectory;

    }
}

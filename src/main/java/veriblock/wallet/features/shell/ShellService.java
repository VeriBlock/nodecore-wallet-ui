// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.uicommon.ControlHelper;

import java.util.List;

public class ShellService {

    //Reusable methods could be called from many places

    private static final Logger _logger = LoggerFactory.getLogger(ShellService.class);

    public static void setupNodeCoreConnection(AppContext appContext)
    {
        //TODO --> check properties, maybe redirect
        NodeCoreGateway ngw = NodeCoreGateway.getInstance();
        String address = appContext.Configuration.getNodecoreAddress();
        Integer port = appContext.Configuration.getNodecorePort();
        String password = appContext.Configuration.getNodecorePassword();

        ConnectionInput input = new ConnectionInput(address, port, password);
        try {
            ngw.setConnectionInfo(input);
        }
        catch (Exception ex)
        {
            //TODO
            _logger.info("Exception swallowed: setupNodeCoreConnection: {}", ex);
        }
    }

    public static ValidationInfo doExport(List<Exportable> rows)
    {
        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        //Check confirm
        ValidationInfo vi = new ValidationInfo();
        if (rows == null || rows.size() == 0)
        {
            _logger.info("Do export: 0 rows");
            vi.setMessageWarning(lm.getString("general_export_nothing"));
            return vi;
        }

        vi.setMessageInfo(String.format(lm.getString("general_export_confirm"), rows.size()));
        boolean shouldContinue = ControlHelper.showAlertYesNoDialog(vi);
        if (!shouldContinue)
        {
            _logger.info("Do export: {} rows", rows.size());
            vi.setMessageWarning(lm.getString("general_export_cancelled"));
            return vi;
        }

        ExportManager em = new ExportManager();

        em.setupData(rows);

        //write data
        ValidationInfo viFinal = em.createCsv();

        return viFinal;
    }
}

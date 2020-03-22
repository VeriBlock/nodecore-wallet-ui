// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;

import java.nio.file.Paths;
import java.util.List;

public class ExportManager {
    public ExportManager()
    {

    }

    public void setupData(List<Exportable> rows) {
        _rows = rows;
    }

    //private String _headerRow;
    private List<Exportable> _rows;

    private String getFileName() {
        String strFileName = Utils.getEpochCurrent() + ".csv";

        FileManager fm = new FileManager();
        fm.getExportDirectory();

        String result = Paths.get(fm.getExportDirectory(), strFileName).toString();
        return result;
    }

    /*
    Returns full path to newly created csv
     */
    public ValidationInfo createCsv() {
        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        ValidationInfo vi = new ValidationInfo();
        if (_rows == null || _rows.size() == 0) {
            vi.setMessageWarning(lm.getString("general_export_nothing"));
            return vi;
        }

        String fullFilePath = getFileName();

        StringBuilder sb = new StringBuilder();

        //create header
        String headerRow = _rows.get(0).createCsvRow(true);

        sb.append(headerRow + System.lineSeparator());

        try {
            //create rows
            for (Exportable e : _rows) {
                String rowString = e.createCsvRow(false);
                sb.append(rowString + System.lineSeparator());
            }

            //Output
            String strContent = sb.toString();
            Utils.writeFile(strContent, fullFilePath);

            vi.setMessageSuccess(String.format(lm.getString("general_export_success"),
                    _rows.size(), fullFilePath));
        } catch (Exception ex) {
            vi.setMessageError(lm.getString("general_export_error"));
        }

        //open
        Utils.openFolder(fullFilePath);

        return vi;
    }


}

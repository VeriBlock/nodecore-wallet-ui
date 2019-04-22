// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.uicommon;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import veriblock.wallet.core.AppContext;
import veriblock.wallet.core.GenericFunction;
import veriblock.wallet.core.NavigationData;
import veriblock.wallet.core.locale.LocaleItem;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.SupportedLocales;
import veriblock.wallet.features.LocaleModuleResource;

import java.net.URL;
import java.util.List;

public class ControlBuilder {

    public static FXMLLoader getFXMLLoader(String pageName)
    {
        URL fxmlPath = (new Object()).getClass().getResource(
                String.format("/veriblock/wallet/features/%1$s.fxml", pageName));;
        FXMLLoader loader = new FXMLLoader(fxmlPath);
        return loader;
    }

    public static String showGetValueDialog(AppContext appContext, DialogGetValueInput input)
    {
        String pageName = "shell/GetValue";
        NavigationData navigationData = new NavigationData();
        navigationData.setData(input);

        String promptMessage = input.message;
        Object result = ControlHelper.showCustomDialog(pageName, appContext, promptMessage, navigationData);

        if (result == null)
        {
            return null;
        }
        else
        {
            return result.toString();
        }
    }

    public static Pair<String, Boolean> showPasswordDialog( AppContext appContext, String promptMessage, GetPasswordInput input)
    {

        String pageName = "shell/GetPassword";
        NavigationData navigationData = new NavigationData();
        navigationData.setData(input);

        Object result = ControlHelper.showCustomDialog(pageName, appContext, promptMessage, navigationData);
        if (result == null)
        {
            return null;
        }
        else
        {
            return (Pair<String, Boolean>)result;
        }
    }

    public static void setupLanguageDropdown(ChoiceBox<LocaleItem> ddLanguageList,  OnChangeDropdownItem<LocaleItem> onChangeDropdownItem)
    {
        List<LocaleItem> list = LocaleManager.getInstance().getSupportedLocales();

        ddLanguageList.getItems().addAll(list);

        //LocalManager already populated, so check that:
        SupportedLocales.SupportedLocale currentLocale = LocaleManager.getInstance().getLocal();
        for (LocaleItem o : ddLanguageList.getItems())
        {
            if (o.Locale == currentLocale)
            {
                ddLanguageList.setValue(o);
                break;
            }
        }

        //Add change listener
        ddLanguageList.valueProperty().addListener((obs, oldval, newval) -> {
            SupportedLocales.SupportedLocale locale = newval.Locale;

            //System.out.println("Selected Locale: " + locale.toString());

            LocaleManager.getInstance().setLocal(locale);

            //OnChange method
            if (onChangeDropdownItem != null) {
                onChangeDropdownItem.call(newval);
            }
        });

        ddLanguageList.setConverter(new StringConverter<LocaleItem>() {
            @Override
            public String toString(LocaleItem entity) {
                return entity.DisplayText;
            }

            @Override
            public LocaleItem fromString(String value) {
                return null;
            }

        });
    }

    public static void setupExportButton(Button button)
    {
        final Glyph DOWNLOAD = new Glyph("FontAwesome", FontAwesome.Glyph.DOWNLOAD)
                .color(Color.valueOf(Styles.VALUE_BUTTON_GREEN));
        button.setGraphic(DOWNLOAD);
        button.setText("");

        ControlHelper.setToolTip(button,
                LocaleManager.getInstance().getModule(LocaleModuleResource.Main).getString("general_export_tooltip"));
    }
}

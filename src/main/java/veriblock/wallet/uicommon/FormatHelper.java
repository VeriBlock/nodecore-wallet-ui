// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.uicommon;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import veriblock.wallet.core.VbkUtils;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.entities.AddressBalanceEntity;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;
import veriblock.wallet.entities.WalletTransactionEntity;
import veriblock.wallet.features.LocaleModuleResource;

import java.util.Locale;

public class FormatHelper {

    public static String getTextDropdownAll()
    {
        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);
        return String.format("--%1$s--", lm.getString("general.dropdownAll"));
    }

    public static void formatAddressDropdown(ChoiceBox<AddressBalanceEntity> dropdown, String unit) {
        formatAddressDropdown(dropdown, unit, getTextDropdownAll());
    }

    public static String formatPercentWhole(double percent)
    {
        //Force it to round down. 0.2699999 should return 26%
        String formattedPercent = Integer.toString((int)Math.floor(100.0 * percent)) + "%";
        return formattedPercent;
        /*
        //Note - could add decimal places: "##.##%"
        DecimalFormat df = new DecimalFormat("##%");
        String formattedPercent = df.format(percent);
        return formattedPercent;
        */
    }

    public static void formatAddressDropdown(ChoiceBox<AddressBalanceEntity> dropdown, String unit, String defaultNullValue)
    {
        dropdown.setConverter(new StringConverter<AddressBalanceEntity>() {
            @Override
            public String toString(AddressBalanceEntity entity) {

                if (defaultNullValue != null) {
                    if (entity == null) {
                        return defaultNullValue;    //"--ALL--";
                    }
                }

                String strDefault = "";
                LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);
                if (entity.getIsDefault()) {
                    strDefault = String.format("(%1$s) ", lm.getString("general.dropdownDefaultItem"));
                }
                String nickNamePrefix = "        "; //make an indent so we can see addresses with nicknames
                if (entity.getNickName() != null && entity.getNickName().length() > 0)
                {
                    nickNamePrefix = String.format("%1$s: ", entity.getNickName());
                }
                return String.format("%5$s%1$s%2$s (%3$s %4$s)",
                        strDefault,
                        entity.getAddress(),
                        VbkUtils.convertAtomicToVbkString( entity.getAmountConfirmedAtomic()),
                        unit,
                        nickNamePrefix
                );
            }

            @Override
            public AddressBalanceEntity fromString(String value) {
                return null;
            }

        });
    }

    public static void setAddressDropdownByValue(ChoiceBox<AddressBalanceEntity> dropdown, String address)
    {
        //Default to first item
        if (address == null)
        {
            dropdown.getSelectionModel().selectFirst();
            return;
        }

        //loop through and find match
        for (AddressBalanceEntity entity: dropdown.getItems())
        {
            if (entity != null && entity.getAddress().equals(address)) {

                dropdown.setValue(entity);
                return;
            }
        }
    }

    //region Column format callbacks
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> addressCellCallBacks(String color){
        return new Callback<TableColumn<T, String>, TableCell<T, String>>() {
            @Override
            public TableCell<T, String> call(final TableColumn<T, String> param) {
                final TableCell<T, String> cell = new TableCell<T, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                        setStyle("-fx-text-fill: "+(color==null? "#00a688": color));
                    }
                };
                return cell;
            }
        };
    }

    public static <T> Callback<TableColumn<T, DateTime>, TableCell<T, DateTime>> dateFormatCallback(){
        return new Callback<TableColumn<T, DateTime>, TableCell<T, DateTime>>() {
            @Override
            public TableCell<T, DateTime> call(
                    final TableColumn<T, DateTime> param) {
                final TableCell<T, DateTime> cell = new TableCell<T, DateTime>() {

                    @Override
                    public void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                        }
                        else {
                            //TODO --> format per locale?
                            //  11/19/18 9:52PM
                            if (item == null) {
                                setText(null);
                            } else {
                                Locale locale = LocaleManager.getInstance().convertToJavaLocale();
                                setText(item.toDateTime().toString(
                                        DateTimeFormat.shortDateTime().withLocale(locale)
                                ));
                            }
                        }
                    }
                };
                return cell;
            }
        };
    }

    //endregion

    /*
    //TODO --> format hash with 000..11C..45C

    //0000000011CEB1136F6DB95AA60C4FABF2DDCD3FEDC8145C
    //returns
    //11C..45C
    public static String TruncateHash(String strHash) {
        if (strHash == null || strHash.length() == 0)
        {
            return "";
        }

        String sNoZeros = strHash.replaceFirst("^0+(?!$)", "");

        //Get first 3, last 3
        int iLength = sNoZeros.length();
        String s1 =  sNoZeros.substring(0,3);
        String s2 = sNoZeros.substring(iLength-3, iLength);
        return s1 + ".." + s2;
    }
    */
}

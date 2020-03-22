// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.storage;

import com.google.gson.Gson;
import veriblock.wallet.core.Utils;

import java.lang.reflect.Type;

public class UserSettings {

    //region save
    public static void save(String key, Integer value)
    {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        db.save(key, value.toString());
    }

    public static void save(String key, String value)
    {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        db.save(key, value);
    }

    public static void saveJson(String key, Object jsonData)
    {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();

        String value = (new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(jsonData));;

        db.save(key, value);
    }

    //endregion

    //region get

    public static boolean doesValueExist(String key)
    {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        String s = db.getValue(key);
        if (s == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean getValue(String key, boolean notFoundValue) {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        String s = db.getValue(key);
        if (s == null) {
            return notFoundValue;
        } else {
            //assume can parse to bool
            try {
                return Boolean.parseBoolean(s);
            } catch (Exception ex) {
                return notFoundValue;
            }
        }
    }

    public static String getValue(String key)
    {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        return db.getValue(key);
    }

    public static boolean getValueBoolean(String key, boolean notFoundValue) {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        String s = db.getValue(key);
        if (s == null) {
            return notFoundValue;
        } else {
            if (Utils.isBoolean(s))
            {
                //expect this
                return Boolean.parseBoolean(s);
            }
            else
            {
                //clean up bad data
                db.save(key, Boolean.toString(notFoundValue));
                return notFoundValue;
            }
        }
    }


    public static Integer getValueInt(String key, Integer notFoundValue)
    {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        String s = db.getValue(key);
        if (s == null)
        {
            return notFoundValue;
        }
        else
        {
            return Integer.parseInt(s);
        }
    }

    //For List<T>, pass in classType = T[].class, deserialize as Arrays.asList(...)
    public static <T> T getValueJson(String key, Object classType)
    {
        OrmLiteGenericRepository db = new OrmLiteGenericRepository();
        String strData = db.getValue(key);
        if (strData == null)
        {
            return null;
        }
        else
        {
            //NOTE --> tried this, but it returned a treemap
            //   new TypeToken<T>(){}.getType()

            Gson gson = new Gson();
            T result = gson.fromJson(strData, (Type)classType);
            if (result == null) {
                return null;
            }
            return result;
        }
    }

    //endregion
}

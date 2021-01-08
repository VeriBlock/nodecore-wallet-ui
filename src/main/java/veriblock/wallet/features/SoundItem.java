// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features;

import veriblock.wallet.core.SoundManager;

public class SoundItem {

    //Class just for type-safety

    //region Add new sound here

    public enum SoundEnum
    {
        ButtonClick,
        Send,
        CoinsReceived,
        PoP
    }

    public String getFile()
    {
        switch(_soundEnum)
        {
            case ButtonClick:
                return "button6.wav";
            case Send:
                return "swoosh.wav";
            case CoinsReceived:
                return "cash-register.wav";
            case PoP:
                return "pop.wav";
            default:
                return "";
        }
    }

    //endregion

    //region Plumbing

    private SoundItem(SoundEnum soundEnum)
    {
        _soundEnum = soundEnum;
    }
    private SoundEnum _soundEnum;


    public static SoundItem get(SoundEnum soundEnum)
    {
        return new SoundItem(soundEnum);
    }

    //endregion

    //region specific

    public static void playButtonClick()
    {
        SoundManager sm = SoundManager.getInstance();
        sm.play(SoundItem.get(SoundEnum.ButtonClick));
    }

    public static void playSend()
    {
        SoundManager sm = SoundManager.getInstance();
        sm.play(SoundItem.get(SoundEnum.Send));
    }

    public static void playPoP()
    {
        SoundManager sm = SoundManager.getInstance();
        sm.play(SoundItem.get(SoundEnum.PoP));
    }

    public static void playCoinsReceived()
    {
        SoundManager sm = SoundManager.getInstance();
        sm.play(SoundItem.get(SoundEnum.CoinsReceived));
    }

    //endregion
}


// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.features.SoundItem;

import java.util.HashMap;

public class SoundManager {

    private static final Logger _logger = LoggerFactory.getLogger(SoundManager.class);

    private static SoundManager single_instance = null;
    private SoundManager()
    {
    }

    // static method to create instance of Singleton class
    public static SoundManager getInstance()
    {
        if (single_instance == null)
            single_instance = new SoundManager();

        return single_instance;
    }


    public boolean isSoundEnabled() {
        //Disable sound until we can fix packaging media in a JAR file
        //return false;


        boolean defaultEnabled = true;
        boolean isSoundEnabled = UserSettings.getValue(SettingsConstants.SOUND_ENABLED, defaultEnabled);
        return isSoundEnabled;

    }

    public void enableSound(boolean shouldEnable) {
        UserSettings.save(SettingsConstants.SOUND_ENABLED, Boolean.toString(shouldEnable));
    }

    public void play(SoundItem sound) {
        String s = sound.getFile();
        play(s);

    }

    public void play(String soundFile) {
        boolean enabled = isSoundEnabled();
        if (!enabled) {
            return;
        }

        String url = null;

        if (_cachedAudio.containsKey(soundFile))
        {
            AudioClip audio = _cachedAudio.get(soundFile);
            audio.play();
            return;
        }

        try {
            url = Utils.resourceAsExternal("sounds/" + soundFile);

            //Use AudioClip for short sound effects rather than Media
            AudioClip audio = new AudioClip(url);
            audio.play();

            _cachedAudio.put(soundFile, audio);

        } catch (Exception ex) {
            _logger.info("Could not play sound item: {}, error: {}", soundFile, ex);
            return;
        }

    }

    //Check cache for performance:
    private HashMap<String, AudioClip> _cachedAudio = new HashMap<String, AudioClip>();

}

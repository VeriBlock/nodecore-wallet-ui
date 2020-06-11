// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.tools;

import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.locale.SupportedLocales;
import veriblock.wallet.core.storage.UserSettings;
import veriblock.wallet.core.storage.MyAddressNicknameData;
import veriblock.wallet.core.storage.OrmLiteAddressRepository;
import veriblock.wallet.entities.AddressBalanceEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.features.wallet.SettingsConstants;
import veriblock.wallet.features.wallet.WalletService;
import veriblock.wallet.uicommon.BackgroundTask;
import veriblock.wallet.uicommon.ControlBuilder;
import veriblock.wallet.uicommon.ControlHelper;
import veriblock.wallet.uicommon.GetPasswordInput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TabTestController  extends BaseController {

    private static final Logger _logger = LoggerFactory.getLogger(TabTestController.class);

    public void initialize() {

    }

    public void init()
    {
        _appContext.UIManager.setTitle("Test Section");

        ControlHelper.setLabelCopyProperties(lblCopy1);

        _backgroundTask = new BackgroundTask();
    }

    private LocaleModule _localeModule;
    public void setLocale() {
        //_localeModule = LocaleManager.getInstance().getModule("TabSettings");

    }

    public TextField lblCopy1;
    public Label lblResult;

    //region Set Status

    public void clickStatusSuccess() {
        ValidationInfo result = new ValidationInfo(ValidationInfo.Status.Success, "Some success message, big long text sentence here, keep going and going and going. This is long enough to force it off the page and be more than just one line."
                + " Some messages can be long, and the status bar should account for that in one way or another. [END]"
                + " More and more text this is going to go long, force it to wrap. Blah blah blah. Imagine a long GRPC message here"
                + " It could go onto several lines. What will happen?");
        _appContext.UIManager.setStatusMessage(result);
    }

    public void clickStatusError()
    {
        ValidationInfo result = new ValidationInfo(ValidationInfo.Status.Error, "Bad stuff happened, and this is a lot of text to explain it. Want to be a long sentence so we can see what the red color looks like...");
        _appContext.UIManager.setStatusMessage(result);
    }

    public void clickStatusClear() {
        _appContext.UIManager.clearStatusMessage();

    }

    public void clickStatusWarning()
    {
        _appContext.UIManager.setStatusMessageWarning("This is a warning");
    }

    public void clickStatusInfo()
    {
        _appContext.UIManager.setStatusMessage("Some info here...");
    }
    //endregion

    public void clickShowPrompt() {

        //boolean strResult = ControlHelper.showAlertYesNoDialog("Hello");
        String promptMessage = "Please enter passphrase to unlock wallet. This was set when first encrypting the wallet.";
        GenericFunction<String, String> testFunc = (strPassword) -> {
            return WalletService.testWalletPassword(strPassword);
        };

        GetPasswordInput input = new GetPasswordInput();
        input.showShouldRemember = true;
        input.defaultShouldRemember = true;
        input.createNewPassword = true;
        input.testFunc = testFunc;

        Pair<String, Boolean> result = ControlBuilder.showPasswordDialog(_appContext, promptMessage, input);

        if (result == null)
        {
            _appContext.UIManager.setStatusMessageWarning("Cancelled");
        }
        else
        {
            _appContext.UIManager.setStatusMessageSucess(String.format("Result: %1$s, shouldRemember:%2$s",
                    result.getKey(), result.getValue()));
        }

    }

    public void clickAlert()
    {
        ControlHelper.showAlertYesNoDialog("Some question");

        ValidationInfo vi = new ValidationInfo();
        vi.setMessageInfo("This is a warning question...");
        String strCopy = "Not the answer you're looking for? Browse other questions tagged java javafx or ask your own question.";

        //ControlHelper.showAlertYesNoDialog(vi, strCopy);
        ControlHelper.showConfirmDialog(vi, strCopy);

        /*
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Look, a Confirmation Dialog");
        alert.setContentText("Are you ok with this?");
        alert.setGraphic(CONFIRM);
        //alert.initStyle(StageStyle.UNDECORATED);
        alert.initStyle(StageStyle.UTILITY);    //make popup movable
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Utils.resourceAsExternal("default.css"));
        dialogPane.getStyleClass().add("dialog");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            // ... user chose OK
            this.lblResult.setText("Yes!");
        } else {
            this.lblResult.setText("Cancel!");
        }
        */
    }

    public void clickShowPopup()
    {
        _appContext.UIManager.showPopup("Welcome.fxml");
    }

    public void saveProperties()
    {
        //update value, persist

        //_appContext.Configuration.setNodecoreContainingDirectory("someDirectory");
        _appContext.Configuration.save();
    }

    public void clickTestProcess()
    {
        //ProcessUtils.getRunningProcesses();
    }

    public void clickTestLocale()
    {
        LocaleManager lm = LocaleManager.getInstance();

        SupportedLocales.SupportedLocale locale = lm.getSystemLocale();

        lm.setLocal(SupportedLocales.SupportedLocale.TEST);

        LocaleModule mod = lm.getModule(LocaleModuleResource.Welcome);
        String s1 = mod.getString("label.title"); //lm.getString("welcome","label.title");
        String s2 = mod.getString("text.main");//lm.getString("welcome","text.main");
    }

    public void clickUnhandledException()
    {
        //force an error:
        String s = null;
        s.startsWith("Force an error");
    }

    //SQL LITE!!
    public void clickSqlLite()
    {

        //--------
        //Test general

        //OrmLiteGenericRepository db2 = new OrmLiteGenericRepository();
        //
        //db2.save(key1, "Some value");
        //String value1 = db2.getValue(key1);


        //This works:
        List<AddressBalanceEntity> cachedAddresses =
                Arrays.asList(
                UserSettings.getValueJson(SettingsConstants.MYADDRESSES_CACHEDADDRESSS,
                AddressBalanceEntity[].class));


        //---
        String key1g = "key1.generic";
        MyAddressNicknameData g1 = new MyAddressNicknameData();
        g1.nickname = "test";
        g1.address = "dddd";

        UserSettings.saveJson(key1g, g1);
        //Retrieve it!
        MyAddressNicknameData g2 = UserSettings.getValueJson(key1g, MyAddressNicknameData.class);

        //--------------

        String key1 = "key1.test";
        UserSettings.save(key1, "blah");
        String value1 = UserSettings.getValue(key1);

        String key2 = "key2.testint";
        UserSettings.save(key2, 23);
        int value2 = UserSettings.getValueInt(key2, -1);

        //--------------
        //save new address
        OrmLiteAddressRepository db = new OrmLiteAddressRepository();

        MyAddressNicknameData row = new MyAddressNicknameData();
        row.address = "V123";
        row.nickname = "nickname1";
        db.save(row.address, row.nickname);

        row = new MyAddressNicknameData();
        row.address = "V456a";
        row.nickname = "nickname2";
        db.save(row.address, row.nickname);

        //List<MyAddressNicknameData> rows = db.getAll();
        HashMap<String, String> map = db.getAllAsHashMap();
        int i = 0;
        //get address


    }

    //region Background Helper

    private BackgroundTask _backgroundTask;

    public void clickBackgroundProcessStart()
    {
        int intervalSeconds = 3;

        _backgroundTask.start(intervalSeconds,
                () -> {
                    doBackgroundStuffOnNonUIThread();
                    return null;
                },
                () -> {
                    updateUIThread();
                    return null;
                }
                );

        _appContext.UIManager.setStatusMessage("Started background thread");
    }

    private String _backgroundData = null;

    private void doBackgroundStuffOnNonUIThread()
    {
        String s = Utils.getTimeOfDayNow();
        //Do long-running work, should not hang the UI
        Utils.waitNSeconds(5);

        //return s;
        _backgroundData = s;
    }

    private void updateUIThread()
    {
        String data = _backgroundData;
        this.lblResult.setText("Updated from background thread: " + data);
    }



    public void clickBackgroundProcessStop()
    {
        _backgroundTask.stop();
        _appContext.UIManager.setStatusMessage("Stopped background thread");
    }

    public TextField txtResourceInput;

    public void clickTestResource(){

        try
        {
            String input = txtResourceInput.getText();
            String result = Utils.resourceAsExternal(input);
            if (result != null) {
                _appContext.UIManager.setStatusMessage("Resource: " + result);
            }
            else
            {
                _appContext.UIManager.setStatusMessage("Resource: could not find" + input);
            }
        }
        catch (Exception ex)
        {
            _appContext.UIManager.setStatusMessageError("Resource: error finding: " + ex.getMessage());
        }
    }

    public TextField txtSoundPath;

    public void clickSound()
    {
        try {
            //todo

            SoundManager sm = SoundManager.getInstance();

            String strSoundFullPath = txtSoundPath.getText();
            if (strSoundFullPath != null && strSoundFullPath.length() > 0)
            {
                //override with that!

                try {

                    AudioClip audio = new AudioClip(strSoundFullPath);
                    audio.play();

                    /*
                    Media sound = new Media(strSoundFullPath);
                    MediaException mediaError = sound.getError();
                    _logger.info("clickSound - media error: '{}'", mediaError);
                    _logger.info("clickSound - source: '{}'", sound.getSource());
                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                    mediaPlayer.play();
            */
                    //---

                }
                catch (Exception ex)
                {
                    _appContext.UIManager.setStatusMessageError("Error playing sound: " + ex.getMessage());
                }
            }
            else
            {
                sm.play(SoundItem.get(SoundItem.SoundEnum.ButtonClick));
            }


            /*
            //kicks off async, and does not stop. Will play whole thing -
            sm.play("long_alphabet.wav");

            Thread.currentThread().wait(2 * 1000);
            sm.play("long_alphabet.wav");

            Thread.currentThread().wait(2 * 1000);
            sm.play("long_alphabet.wav");
            */

            // sm.play("button6.wav");
            // sm.play("cash-register.wav");
            //sm.play("swoosh.wav");
        }
        catch (Exception ex)
        {

        }
    }


    //endregion

    public void clickTestLocking()
    {
        boolean isLocked = NodeCoreGateway.getInstance().isWalletLocked();
        int i = 0;
    }

    //region BaseController events

    public Label lblOnChangeBlockHeight;
    public Label lblOnChangeBalance;

    public void onCurrentBlockChanged(int oldBlockHeight, int newBlockHeight) {
        String s = String.format("BlockHeight: Old: %1$s, New: %2$s [%3$s]", oldBlockHeight, newBlockHeight,
                Utils.getTimeOfDayNow());
        this.lblOnChangeBlockHeight.setText(s);
        _logger.info("onCurrentBlockChanged: " + s);
    }
    public void onBalanceChanged(boolean didChange, long oldAmount, long newAmount)
    {
        String s = String.format("Amount: Changed: %1$s, Old: %2$s, New: %3$s [%4$s]",
                didChange, oldAmount, newAmount,  Utils.getTimeOfDayNow());
        this.lblOnChangeBalance.setText(s);
        _logger.info("onBalanceChanged: " + s);
    }

    //endregion
}

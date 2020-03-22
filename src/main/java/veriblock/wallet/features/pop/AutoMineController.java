// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.pop;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.core.pop.*;
import veriblock.wallet.core.pop.entities.ConfigEntity;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.features.SoundItem;
import veriblock.wallet.uicommon.ControlHelper;

public class AutoMineController extends DialogController {

    private static final Logger _logger = LoggerFactory.getLogger(AutoMineController.class);
    public void initialize() {
    }

    private LocaleModule _localeModule;

    public void init() {

        //set current props
        _clientProxy = PopService.getApiProxy();

        try {
            setLocale();
            setupModel();
            setupUI();
            applyModelToUI();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public ImageView imgRoundsBackground;
    public ImageView imgRounds1;
    public ImageView imgRounds2;
    public ImageView imgRounds3;
    public ImageView imgRounds4;

    public CheckBox chkRound1;
    public CheckBox chkRound2;
    public CheckBox chkRound3;
    public CheckBox chkRound4;
    public Label lblSummaryAmount;
    public Label lblWarning;

    public Button btnApply;
    public Button btnCancel;

    private void setupUI()
    {
        //load image
        //  pop_rounds_v2.png
        ControlHelper.setImage(imgRoundsBackground, "pop/pop_rounds_v2.png");

        ControlHelper.setImage(imgRounds1, "pop/pop_rounds_r1.png");
        imgRounds1.setOpacity(0.5);

        ControlHelper.setImage(imgRounds2, "pop/pop_rounds_r2.png");
        imgRounds2.setOpacity(0.5);

        ControlHelper.setImage(imgRounds3, "pop/pop_rounds_r3.png");
        imgRounds3.setOpacity(0.5);

        ControlHelper.setImage(imgRounds4, "pop/pop_rounds_r4.png");
        imgRounds4.setOpacity(0.5);
    }

    private AutoMineModel _autoMineModel;

    private void applyModelToUI()
    {
        updateUIForRounds();

        //update checkboxes
        this.chkRound1.setSelected(_autoMineModel.shouldMineRound1);
        this.chkRound2.setSelected(_autoMineModel.shouldMineRound2);
        this.chkRound3.setSelected(_autoMineModel.shouldMineRound3);
        this.chkRound4.setSelected(_autoMineModel.shouldMineRound4);
    }

    private void updateUIForRounds()
    {
        imgRounds1.setVisible(_autoMineModel.shouldMineRound1);
        imgRounds2.setVisible(_autoMineModel.shouldMineRound2);
        imgRounds3.setVisible(_autoMineModel.shouldMineRound3);
        imgRounds4.setVisible(_autoMineModel.shouldMineRound4);

        String blockPercentMineMessage = null;
        if (_autoMineModel.areNoRoundsSelected())
        {
            //nothing
            blockPercentMineMessage = _localeModule.getString("AutoMine_label_summaryAmount_none");
        }
        else if (_autoMineModel.areAllRoundsSelected())
        {
            //everything
            blockPercentMineMessage = _localeModule.getString("AutoMine_label_summaryAmount_all");
        }
        else
        {
            int iCount = _autoMineModel.getMinedBlockCount();
            blockPercentMineMessage = String.format(_localeModule.getString("AutoMine_label_summaryAmount_some"),
                    iCount);
        }

        lblSummaryAmount.setText(blockPercentMineMessage);

        //set warning label
        if (_autoMineModel.areNoRoundsSelected())
        {
            this.lblWarning.setVisible(false);
        }
        else
        {
            this.lblWarning.setVisible(true);
        }
    }

    private ApiProxy _clientProxy;

    private void setupModel()
    {
        _autoMineModel = new AutoMineModel();

        //pull from api
        ConfigEntity config = _clientProxy.getConfig();
        updateModelFromConfig(config);
    }

    private void updateModelFromConfig(ConfigEntity config)
    {
        _autoMineModel.shouldMineRound1 = config.autoMineRound1;
        _autoMineModel.shouldMineRound2 = config.autoMineRound2;
        _autoMineModel.shouldMineRound3 = config.autoMineRound3;
        _autoMineModel.shouldMineRound4 = config.autoMineRound4;
    }

    public Label lblMessage1;
    public Label lblMessage2;
    public Hyperlink hlnkHelp;
    public Label lblSelectRounds;

    public void setLocale()
    {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabPoP);

        lblMessage1.setText(_localeModule.getString("AutoMine_label_message1"));
        lblMessage2.setText(_localeModule.getString("AutoMine_label_message2"));
        hlnkHelp.setText(_localeModule.getString("AutoMine_hlnk_help"));

        lblSelectRounds.setText(_localeModule.getString("AutoMine_label_selectRounds"));
        chkRound1.setText(_localeModule.getString("AutoMine_label_round1"));
        chkRound2.setText(_localeModule.getString("AutoMine_label_round2"));
        chkRound3.setText(_localeModule.getString("AutoMine_label_round3"));
        chkRound4.setText(_localeModule.getString("AutoMine_label_round4"));
        lblWarning.setText(_localeModule.getString("AutoMine_label_warningContinue"));

        btnApply.setText(_localeModule.getString("AutoMine_button_save"));
        btnCancel.setText(_localeModule.getString("AutoMine_button_cancel"));
    }

    public void clickHelpRounds()
    {
        //open wiki link
        //https://wiki.veriblock.org/index.php?title=HowTo_run_PoP_Miner#VeriBlock_.22Rounds.22
        String url = "https://wiki.veriblock.org/index.php?title=HowTo_run_PoP_Miner#VeriBlock_.22Rounds.22";
        Utils.openLink(url);
    }

    //region Click Rounds

    public void clickRound1()
    {
        SoundItem.playButtonClick();
        _autoMineModel.shouldMineRound1 = this.chkRound1.isSelected();
        updateUIForRounds();
    }

    public void clickRound2()
    {
        SoundItem.playButtonClick();
        _autoMineModel.shouldMineRound2 = this.chkRound2.isSelected();
        updateUIForRounds();
    }

    public void clickRound3()
    {
        SoundItem.playButtonClick();
        _autoMineModel.shouldMineRound3 = this.chkRound3.isSelected();
        updateUIForRounds();
    }

    public void clickRound4()
    {
        SoundItem.playButtonClick();
        _autoMineModel.shouldMineRound4 = this.chkRound4.isSelected();
        updateUIForRounds();
    }


    //endregion

    public void clickApplyAndClose() {
        //save
        boolean blnSuccess = saveRounds();

        //exist
        if (blnSuccess) {
            this.closeDialog(_autoMineModel);
        }
    }

    private boolean saveRounds()
    {
        boolean blnSuccess = true;
        try {
            _clientProxy.setConfig(ConfigConstants.Key.AUTO_MINE_ROUND1, Boolean.toString(_autoMineModel.shouldMineRound1));
            _clientProxy.setConfig(ConfigConstants.Key.AUTO_MINE_ROUND2, Boolean.toString(_autoMineModel.shouldMineRound2));
            _clientProxy.setConfig(ConfigConstants.Key.AUTO_MINE_ROUND3, Boolean.toString(_autoMineModel.shouldMineRound3));
            _clientProxy.setConfig(ConfigConstants.Key.AUTO_MINE_ROUND4, Boolean.toString(_autoMineModel.shouldMineRound4));
        }
        catch (Exception ex)
        {
            //Should be able to connect and save, but just in case...

            _logger.error("Could not save Auto_Mine: {}", ex);
            ValidationInfo vi = new ValidationInfo();
            vi.setMessageError(_localeModule.getString("AutoMine_save_error"));
            ControlHelper.showConfirmDialog(vi);
            blnSuccess = false;
        }

        return blnSuccess;
    }

    public void clickCancel()
    {
        this.closeDialog(null);
    }

}

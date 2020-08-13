// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.pop;

import veriblock.wallet.core.pop.entities.*;
import veriblock.wallet.entities.PoPEndorsementInfoEntity;

import java.util.List;

public class TabPoPModel {
    public TabPoPModel()
    {
        //by default, not populated
        this.popOperationEntities = null;
        this.viewRecentRewards = null;
        this.lastManualMineTimeStamp = 0;
        this.isInitialSetup = true;
        this.didAlreadyPromptConnectPopup = false;
        //by default user hasn't cancelled the connect dialog
        this.didUserCancelConnectDialog= false;
        //this.statusHasPleaseConnectConnectWarning = false;
    }

    public final int secondsToWaitBetweenMining = 5;

    public int currentBlock;

    //public boolean statusHasPleaseConnectConnectWarning;
    public boolean didAlreadyPromptConnectPopup;
    public boolean isInitialSetup;
    public boolean showInitialMessage;
    public boolean isConnected;
    public boolean didUserCancelConnectDialog;

    public AutoMineConfigEntity getAutoMineConfig() {
        return autoMineConfig;
    }

    public BtcFeeConfigEntity getBtcFeeConfigEntity() {
        return btcFeeConfigEntity;
    }

    public void setAutoMineConfig(AutoMineConfigEntity autoMineConfig) {
        this.autoMineConfig = autoMineConfig;
    }

    public void setBtcFeeConfigEntity(BtcFeeConfigEntity btcFeeConfigEntity) {
        this.btcFeeConfigEntity = btcFeeConfigEntity;
    }

    public List<OperationSummaryEntity> getPopOperationEntities() {
        return popOperationEntities;
    }

    public void setPopOperationEntities(List<OperationSummaryEntity> popOperationEntities) {
        this.popOperationEntities = popOperationEntities;
    }

    private AutoMineConfigEntity autoMineConfig;
    private BtcFeeConfigEntity btcFeeConfigEntity;
    private boolean isPopServiceConnected;
    private List<OperationSummaryEntity> popOperationEntities;

    public MinerPropertiesEntity getMinerProperties() {
        return minerProperties;
    }

    public void setMinerProperties(MinerPropertiesEntity minerProperties) {
        this.minerProperties = minerProperties;
    }

    private MinerPropertiesEntity minerProperties;

    public List<PoPEndorsementInfoEntity> getViewRecentRewards() {
        return viewRecentRewards;
    }

    public void setViewRecentRewards(List<PoPEndorsementInfoEntity> viewRecentRewards) {
        this.viewRecentRewards = viewRecentRewards;
    }

    private List<PoPEndorsementInfoEntity> viewRecentRewards;

    public long lastManualMineTimeStamp;

    public boolean getIsSufficientPoPVersion()
    {
        //Eventually have API return a version. For now check that correct data is there
        //Should have minerProperties.bitcoinAddress

        if (minerProperties != null)
        {
            String btcAddress = minerProperties.bitcoinAddress;
            if (btcAddress != null && btcAddress.length() > 0)
            {
                //GOOD
                return true;
            }
        }

        return false;
    }
}

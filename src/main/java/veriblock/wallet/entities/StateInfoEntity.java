// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

//GRPC: getStateInfo
public class StateInfoEntity
{
    public StateInfoEntity()
    {
    }

    public String toString()
    {
        return String.format("LocalBlockchainHeight=%1$s", this.LocalBlockchainHeight);
    }

    public int NetworkHeight;
    public int LocalBlockchainHeight;
    public String NetworkVersion;
    public int WalletCacheSyncHeight;

    public String ProgramVersion;
    public long NodeCoreStartTime;
    public int ConnectedPeerCount;
    public String DataDirectory;
    public String OperatingState;
    public WalletLockState walletLockState;

    public boolean isWalletCacheBuilding()
    {
        int iBuffer = 2;
        return (this.WalletCacheSyncHeight + iBuffer < this.LocalBlockchainHeight);
    }

    public static WalletLockState parseWalletLockState(String value)
    {
        if (value == null || value.length() == 0)
        {
            return WalletLockState.UNKNOWN;
        }
        value =value.toUpperCase();

        switch (value)
        {
            case "DEFAULT":
                return WalletLockState.DEFAULT;
            case "UNLOCKED":
                return WalletLockState.UNLOCKED;
            case "LOCKED":
                return WalletLockState.LOCKED;
            default:
                return WalletLockState.UNKNOWN;
        }
    }

    /*
       "blockchain_state": "LOADED",
        "operating_state": "RUNNING",
        "network_state": "DISCONNECTED",
        "connected_peer_count": 0,
        "current_sync_peer": "",
        "network_height": 0,
        "local_blockchain_height": 8,
        "network_version": "Alpha",
        "data_directory": "",
        "program_version": "0.3.7-dev.11.uncommitted+83ee19c",
        "nodecore_starttime": 1540179193
     */


}

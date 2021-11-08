// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.entities;

import nodecore.api.grpc.RpcPopEndorsementInfo;
import org.veriblock.core.utilities.Utility;

public class PoPEndorsementInfoEntity {
    public PoPEndorsementInfoEntity(final RpcPopEndorsementInfo popEndorsementInfo) {

        minerAddress = Utility.bytesToBase58(popEndorsementInfo.getMinerAddress().toByteArray());
        endorsedVeriBlockBlockHash = Utility.bytesToHex(popEndorsementInfo.getEndorsedVeriblockBlockHash().toByteArray());
        containedInVeriBlockBlockHash = Utility.bytesToHex(popEndorsementInfo.getContainedInVeriblockBlockHash().toByteArray());
        veriBlockTransactionId = Utility.bytesToHex(popEndorsementInfo.getVeriblockTxId().toByteArray());
        bitcoinTransaction = Utility.bytesToHex(popEndorsementInfo.getBitcoinTransaction().toByteArray());
        bitcoinTransactionId = Utility.bytesToHex(popEndorsementInfo.getBitcoinTxId().toByteArray());
        bitcoinBlockHeader = Utility.bytesToHex(popEndorsementInfo.getBitcoinBlockHeader().toByteArray());
        bitcoinBlockHeaderHash = Utility.bytesToHex(popEndorsementInfo.getBitcoinBlockHeaderHash().toByteArray());
        reward = popEndorsementInfo.getReward();
        finalized = popEndorsementInfo.getFinalized();
        endorsedBlockNumber = popEndorsementInfo.getEndorsedBlockNumber();
    }

    public String minerAddress;
    public String endorsedVeriBlockBlockHash;
    public String containedInVeriBlockBlockHash;
    public String veriBlockTransactionId;
    public String bitcoinTransaction;
    public String bitcoinTransactionId;
    public String bitcoinBlockHeader;
    public String bitcoinBlockHeaderHash;
    public long reward;
    public boolean finalized;
    public int endorsedBlockNumber;
}
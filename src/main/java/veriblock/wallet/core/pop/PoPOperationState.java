// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.pop;

public enum PoPOperationState {
    INITIAL(0, "Initial", "Retrieve Mining Instruction"),
    INSTRUCTION(1, "Mining Instruction retrieved", "Submit Endorsement Transaction"),
    ENDORSEMENT_TRANSACTION(2,"Endorsement Transaction submitted", "Confirm Endorsement Transaction"),
    CONFIRMED(3, "Endorsement Transaction Confirmed", "Wait for Block of Proof"),
    BLOCK_OF_PROOF(4, "Block of Proof received", "Prove Endorsement Transaction"),
    PROVEN(5, "Endorsement Transaction proven", "Build Context"),
    CONTEXT(6, "Context determined", ""),
    SUBMITTED_POP_DATA(7, "Publications submitted", "Wait for Payout Block"),
    PAYOUT_DETECTED(8, "Payout detected", "Complete and save"),
    COMPLETED(100, "Completed", ""),
    FAILED(-1, "Failed", "");

    private int id;
    private String name;
    private String taskName;

    PoPOperationState(int id, String name, String taskName) {
       this.id = id;
       this.name = name;
       this.taskName = taskName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTaskName() {
        return taskName;
    }
}

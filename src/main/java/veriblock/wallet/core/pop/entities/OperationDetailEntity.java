// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.
package veriblock.wallet.core.pop.entities;

import veriblock.wallet.core.pop.PoPOperationState;
import java.util.Map;

public class OperationDetailEntity {
    public String operationId;
    public String status;
    public String currentAction;
    public Map<String, String> detail;

    public boolean isSuccess() {
        return PoPOperationState.valueOf(this.status.toUpperCase()) != PoPOperationState.FAILED;
    }

    public int getWorkFlowNumber() {
        for (PoPOperationState state : PoPOperationState.values()) {
            if (state.getName().equalsIgnoreCase(status)) {
                return state.getId() + 1;
            }
        }
        return 0;
    }
}

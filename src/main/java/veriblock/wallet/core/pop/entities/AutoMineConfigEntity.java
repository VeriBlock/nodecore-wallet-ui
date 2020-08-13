package veriblock.wallet.core.pop.entities;

import java.util.ArrayList;
import java.util.List;

public class AutoMineConfigEntity {
    public boolean round1;
    public boolean round2;
    public boolean round3;
    public boolean round4;

    public List<Integer> getAutoMineRounds() {
        final List<Integer> autoMineRounds = new ArrayList<>();
        if (this.round1) {
            autoMineRounds.add(1);
        }
        if (this.round2) {
            autoMineRounds.add(2);
        }
        if (this.round3) {
            autoMineRounds.add(3);
        }
        if (this.round4) {
            autoMineRounds.add(4);
        }
        return autoMineRounds;
    }
}

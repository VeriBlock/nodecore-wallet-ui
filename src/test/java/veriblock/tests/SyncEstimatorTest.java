// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.tests;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import veriblock.wallet.core.syncEstimator.Estimator;
import veriblock.wallet.core.syncEstimator.SyncHeight;
import veriblock.wallet.core.Utils;
import veriblock.wallet.core.syncEstimator.SyncResult;
import veriblock.wallet.core.syncEstimator.SyncStatusEnum;

import java.util.ArrayList;


public class SyncEstimatorTest {

    @Test
    public void basic_1() {

        DateTime dt = new DateTime();

        SyncHeight h1 = new SyncHeight(1000,
                -999999, //doesn't matter
                dt.minusSeconds(-2));
        SyncHeight h2 = new SyncHeight(1200, 7000, dt.minusSeconds(-22));

        SyncResult result = Estimator.getConvergeEstimate(h1, h2);
        SyncHeight hFinal = result.SyncFinalHeight;

        Assert.assertEquals(SyncStatusEnum.SUCCESS_ESTIMATE, result.Status);
        Assert.assertEquals(10, result.LocalSyncRateBlocksPerSecond);
        Assert.assertEquals(9, result.MinutesToSync);
        Assert.assertEquals(7019, hFinal.LocalHeight);
        Assert.assertEquals(7019, hFinal.NetworkHeight);

        int iSeconds = Utils.getSecondsDiff(h2.Timestamp, hFinal.Timestamp);
        Assert.assertEquals(581, iSeconds);
    }

    @Test
    public void noChange() {

        DateTime dt = new DateTime();

        SyncHeight h1 = new SyncHeight(1000, -999999, dt.minusSeconds(-2));
        SyncHeight h2 = new SyncHeight(1001, 7000, dt.minusSeconds(-3));

        SyncResult result = Estimator.getConvergeEstimate(h1, h2);
        Assert.assertEquals(SyncStatusEnum.NO_CHANGE, result.Status);
    }

    @Test
    public void alreadyInSync_1() {

        DateTime dt = new DateTime();

        SyncHeight h1 = new SyncHeight(1000, -999999, dt.minusSeconds(-2));
        SyncHeight h2 = new SyncHeight(7000, 7001, dt.minusSeconds(-3));

        SyncResult result = Estimator.getConvergeEstimate(h1, h2);
        Assert.assertEquals(SyncStatusEnum.ALREADY_INSYNC, result.Status);
    }

    @Test
    public void forceError_1() {

        DateTime dt = new DateTime();

        //decrease height, which is impossible
        SyncHeight h1 = new SyncHeight(1000, -999999, dt.minusSeconds(-2));
        SyncHeight h2 = new SyncHeight(500, 7005, dt.minusSeconds(-3));

        SyncResult result = Estimator.getConvergeEstimate(h1, h2);
        Assert.assertEquals(SyncStatusEnum.COULD_NOT_COMPUTE, result.Status);
    }

    @Test
    public void addPoints_1() {

        Estimator e = new Estimator(3);

        DateTime dt = new DateTime();

        SyncHeight h1 = new SyncHeight(1000, -999999, dt.minusSeconds(-2));
        SyncHeight h2 = new SyncHeight(1050, -999999, dt.minusSeconds(-10));
        SyncHeight h3 = new SyncHeight(1100, -999999, dt.minusSeconds(-15));
        SyncHeight h4 = new SyncHeight(1200, 1200, dt.minusSeconds(-22));
        ArrayList<SyncHeight> points = null;

        //Nothing added
        points = e.getPoints();
        Assert.assertEquals(0, points.size());

        //1 added
        e.addBlockHeight(h1);
        points = e.getPoints();
        Assert.assertEquals(1, points.size());
        Assert.assertEquals(1000, e.getPointOldest().LocalHeight);
        Assert.assertEquals(1000, e.getPointMostRecent().LocalHeight);

        //2 added
        e.addBlockHeight(h2);
        points = e.getPoints();
        Assert.assertEquals(2, points.size());
        Assert.assertEquals(1000, e.getPointOldest().LocalHeight);
        Assert.assertEquals(1050, e.getPointMostRecent().LocalHeight);

        //3 added --> queue full
        e.addBlockHeight(h3);
        points = e.getPoints();
        Assert.assertEquals(3, points.size());
        Assert.assertEquals(1000, e.getPointOldest().LocalHeight);
        Assert.assertEquals(1100, e.getPointMostRecent().LocalHeight);

        //4 added --> removed
        e.addBlockHeight(h4);
        points = e.getPoints();
        Assert.assertEquals(3, points.size());
        Assert.assertEquals(1050, e.getPointOldest().LocalHeight);
        Assert.assertEquals(1200, e.getPointMostRecent().LocalHeight);

        //clear
        e.clearPoints();
        points = e.getPoints();
        Assert.assertEquals(0, points.size());
        Assert.assertEquals(null, e.getPointOldest());
        Assert.assertEquals(null, e.getPointMostRecent());
    }

        @Test
    public void addMeasurements_2() {

        Estimator e = new Estimator();

        DateTime dt = new DateTime();

        SyncHeight h1 = new SyncHeight(1000, -999999, dt.minusSeconds(-2));
        SyncHeight h2 = new SyncHeight(1050, -999999, dt.minusSeconds(-10));
        SyncHeight h3 = new SyncHeight(1100, -999999, dt.minusSeconds(-15));
        SyncHeight h4 = new SyncHeight(1200, 7000, dt.minusSeconds(-22));

        e.addBlockHeight(h1);
        e.addBlockHeight(h2);
        e.addBlockHeight(h3);
        e.addBlockHeight(h4);

        SyncResult result = e.getConvergeEstimate(100);

        SyncHeight hFinal = result.SyncFinalHeight;
        Assert.assertEquals(SyncStatusEnum.SUCCESS_ESTIMATE, result.Status);
        Assert.assertEquals(10, result.LocalSyncRateBlocksPerSecond);
        Assert.assertEquals(7019, hFinal.LocalHeight);
        Assert.assertEquals(7019, hFinal.NetworkHeight);
        int iSeconds = Utils.getSecondsDiff(h4.Timestamp, hFinal.Timestamp);
        Assert.assertEquals(581, iSeconds);
    }
}

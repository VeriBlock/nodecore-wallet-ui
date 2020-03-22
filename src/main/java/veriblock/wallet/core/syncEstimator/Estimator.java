// VeriBlock GUI Wallet
// Copyright 2017-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core.syncEstimator;

import veriblock.wallet.core.Utils;

import java.util.ArrayList;

public class Estimator {

    public Estimator()
    {
        _points = new  ArrayList<SyncHeight>();
    }

    public Estimator(int pointsToKeep)
    {
        _maxQueueLength = pointsToKeep;
        _points = new  ArrayList<SyncHeight>();
    }

    //region Points management

    public void addBlockHeight(int localHeight, int networkHeight)
    {
        SyncHeight h = new SyncHeight(localHeight, networkHeight);
        addBlockHeight(h);
    }
    public void addBlockHeight(SyncHeight syncHeight)
    {
        //add item
        //remove last

        _heightMostRecent = syncHeight;

        _points.add(0, syncHeight);

        //possible remove last item?
        if (_points.size() > _maxQueueLength)
        {
            _points.remove(_points.size() - 1);
        }

        //get last item in list
        _heightOldest = _points.get(_points.size() - 1);

    }

    private int _maxQueueLength = 30;   //default
    private ArrayList<SyncHeight> _points;
    private SyncHeight _heightMostRecent;
    private SyncHeight _heightOldest;

    public SyncHeight getPointMostRecent()
    {
        return _heightMostRecent;
    }
    public SyncHeight getPointOldest()
    {
        return _heightOldest;
    }
    public ArrayList<SyncHeight> getPoints() {
        return _points;
    }
    public void clearPoints()
    {
        _points = new ArrayList<>();
        _heightMostRecent= null;
        _heightOldest = null;
    }

    //endregion

    public SyncResult getConvergeEstimate() {
        int minBlockDiffBeforeCalculating = 500;    //default
        return getConvergeEstimate(minBlockDiffBeforeCalculating);
    }

    public SyncResult getConvergeEstimate( int minBlockDiffBeforeCalculating) {

        //Pick first and last for most accurate rate (avoid blips)
        SyncHeight h1 = _heightOldest;
        SyncHeight h2 = _heightMostRecent;
        if (h1 == null || h2 == null)
        {
            SyncResult result = new SyncResult();
            result.Status = SyncStatusEnum.COULD_NOT_COMPUTE;
            return result;
        }

        //ensure data points at least X apart before computing
        if (h2.LocalHeight - h1.LocalHeight < minBlockDiffBeforeCalculating)
        {
            SyncResult result = new SyncResult();
            result.Status = SyncStatusEnum.COULD_NOT_COMPUTE;
            return result;
        }

        return getConvergeEstimate(h1, h2);
    }

    public static SyncResult getConvergeEstimate(SyncHeight h1, SyncHeight h2)
    {
        SyncResult result = new SyncResult();

        result.OldestHeight = h1;
        result.MostCurrentHeight = h2;

        int thresholdInSync = 5;
        if (Math.abs(h2.NetworkHeight - h2.LocalHeight) < 5)
        {
            //already in sync
            result.Status = SyncStatusEnum.ALREADY_INSYNC;
            return result;
        }

        if (h2.LocalHeight - h1.LocalHeight < 0)
        {
            //cannot go backwards
            result.Status = SyncStatusEnum.COULD_NOT_COMPUTE;
            return result;
        }

        int changeThreshold = 3;    //should be syncing 100s of blocks
        if (h2.LocalHeight - h1.LocalHeight < changeThreshold)
        {
            //no change, cannot estimate
            result.Status = SyncStatusEnum.NO_CHANGE;
            return result;
        }

        try {
            double syncrate = (h2.LocalHeight - h1.LocalHeight)
                    / Utils.getSecondsDiff(h1.Timestamp, h2.Timestamp);

            result.LocalSyncRateBlocksPerSecond = (int) syncrate;

            //Must account for new blocks being added
            final double newBlockRate = 1.0 / 30.0; //new block every 30 seconds

            double top = h2.NetworkHeight - h2.LocalHeight;
            double bottom = syncrate - newBlockRate;

            double convergeTimeSeconds = top / bottom;

            //Will converge at current_height + (time * rate)

            //These two should match within 1
            double convergeLocalHeight = h2.LocalHeight + convergeTimeSeconds * syncrate;
            double convergeNetworkHeight = h2.NetworkHeight + convergeTimeSeconds * newBlockRate;

            SyncHeight hFinal = new SyncHeight(
                    (int) convergeLocalHeight,
                    (int) convergeNetworkHeight,
                    h2.Timestamp.minusSeconds(-1 * (int) convergeTimeSeconds)
            );
            result.SyncFinalHeight = hFinal;
            result.MinutesToSync = (int) (convergeTimeSeconds / 60.0);

            //sanity check
            if (result.LocalSyncRateBlocksPerSecond < 1)   //should be hundreds
            {
                result.Status = SyncStatusEnum.COULD_NOT_COMPUTE;
            } else if (convergeTimeSeconds < 0) //should be future time
            {
                result.Status = SyncStatusEnum.COULD_NOT_COMPUTE;
            } else {
                result.Status = SyncStatusEnum.SUCCESS_ESTIMATE;
            }

        }
        catch (Exception ex)
        {
            //TODO --> flesh out what exactly error could be?
            result.Status = SyncStatusEnum.COULD_NOT_COMPUTE;
        }

        return result;
    }


}

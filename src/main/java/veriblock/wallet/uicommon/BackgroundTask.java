// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.uicommon;

import javafx.application.Platform;
import javafx.concurrent.Task;

//Background Helper, runs a task at X frequency.
//Will wait for the task to finish before starting it again
public class BackgroundTask
{
    public BackgroundTask()
    {

        _shouldRun = false;
        initialDelaySeconds = 0;
    }
    private int _intervalSeconds;

    /*
    Initial one-time delay before starting background process. Default=0. This will not hang the UI thread.
     */
    public int initialDelaySeconds;

    public void start(int intervalSeconds, DoWork backgroundWork, DoWork uiWork)
    {
        _intervalSeconds = intervalSeconds;
        _shouldRun =true;

        _methodBackgroundWork = backgroundWork;
        _methodUIWork = uiWork;
        kickoffBackground(_intervalSeconds);
    }

    public void dispose()
    {
        _continueLoop = false;  //gracefully shut down thread

        //kill events so nothing is fired
        _methodBackgroundWork = null;
        _methodUIWork = null;
    }

    public void stop()
    {
        _shouldRun = false;
    }

    private boolean _shouldRun;

    private GetBackgroundTask _backgroundTask;

    private DoWork _methodBackgroundWork;
    private DoWork _methodUIWork;
    public interface DoWork {
        Void doWork();
    }

    private boolean _continueLoop = true;

    private void kickoffBackground(int waitNSeconds) {
        Task task1 = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                //initial one-time delay
                if (initialDelaySeconds > 0)
                    Thread.sleep(1000 * initialDelaySeconds); //This will not block the UI thread

                while (_continueLoop) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            updateBackground_start();
                        }
                    });
                    Thread.sleep(1000 * waitNSeconds); //This will not block the UI thread
                }
                return null;
            }
        };
        Thread th1 = new Thread(task1);
        th1.setDaemon(true);
        th1.start();

    }

    public void updateBackground_start() {
        //do this every N seconds
        //don't start new ping until previous one finished
        if ( !this._shouldRun)
        {
            return;
        }

        if (_backgroundTask == null)
        {
            _backgroundTask = new GetBackgroundTask();
            _backgroundTask.setOnSucceeded(e -> {
                updateBackround_done();
            });
            new Thread(_backgroundTask).start();
        }
    }

    public class GetBackgroundTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {

            //Custom work here
            if (_methodBackgroundWork != null) {
                _methodBackgroundWork.doWork();
            }
            return null;
        }
    }

    //Finish call (run continuously)
    private void updateBackround_done()
    {
        if (_methodUIWork != null)
        {
            //_backgroundTask.getValue()
            _methodUIWork.doWork();
        }

        _backgroundTask = null;   //reset to allow next loop to call it
    }

}

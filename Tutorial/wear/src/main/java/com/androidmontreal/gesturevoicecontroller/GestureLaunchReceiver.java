package com.androidmontreal.gesturevoicecontroller;

import watch.nudge.gesturelibrary.AppControllerReceiverService;

/**
 * Created by on 15-12-12.
 */
public class GestureLaunchReceiver extends AppControllerReceiverService {
    @Override
    protected Class getWatchActivityClass() {
        return MainWatchActivity.class;
    }
}

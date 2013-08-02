package com.onextent.oemap.presence;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OeMapPresenceService extends Service {

    /**
     * todo:
     *      start at intent invoke:
     *        on boot
     *        add map intent
     *          insert map to db
     *        del map intent
     *          remove from db
     *        if maps > 0 and worker not running, run it
     *        if maps > 0 and worker running, wake it up
     *
     *      run worker thread
     *        on each loop
     *          see if we are in any maps
     *            if no, stop running
     *            if yes, send presence
     *              store cohort presences
     *                broadcast our presence update intent
     *
     * @param intent
     * @return
     */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


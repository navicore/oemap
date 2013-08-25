/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.presence;

public class PresenceException extends Exception {

    public PresenceException(String msg) {
        super(msg);
    }
    public PresenceException(String msg, Throwable err) {
        super(msg, err);
    }
    public PresenceException(Throwable err) {
        super(err);
    }
}

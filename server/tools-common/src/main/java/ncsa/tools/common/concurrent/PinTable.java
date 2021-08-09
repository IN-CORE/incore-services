/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.concurrent;

import java.util.HashMap;
import java.util.Map;

public class PinTable {
    private Map pinned = new HashMap();

    public void pin(String key) {
        if (key == null)
            throw new IllegalArgumentException("key was null");
        synchronized (pinned) {
            while (pinned.containsKey(key)) {
                try {
                    pinned.wait();
                } catch (InterruptedException ignored) {
                }
            }
            pinned.put(key, null);
        }
    }

    public void unpin(String key) {
        if (key == null)
            throw new IllegalArgumentException("key was null");
        synchronized (pinned) {
            pinned.remove(key);
            pinned.notifyAll();
        }
    }
}

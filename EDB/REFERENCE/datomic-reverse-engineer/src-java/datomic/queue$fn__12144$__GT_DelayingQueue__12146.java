/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue.DelayingQueue;

public final class queue$fn__12144$__GT_DelayingQueue__12146
extends AFunction {
    public Object invoke(Object delay, Object delay_queue, Object thread2) {
        Object object = delay;
        delay = null;
        Object object2 = delay_queue;
        delay_queue = null;
        Object object3 = thread2;
        thread2 = null;
        return new DelayingQueue(object, object2, object3);
    }
}


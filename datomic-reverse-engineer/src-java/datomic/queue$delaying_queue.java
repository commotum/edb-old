/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue$delaying_queue$fn__12149;
import datomic.queue.DelayingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class queue$delaying_queue
extends AFunction {
    public static Object invokeStatic(Object msec, Object dest_queue) {
        LinkedBlockingQueue delay_queue = new LinkedBlockingQueue();
        Object object = dest_queue;
        dest_queue = null;
        Thread t = new Thread((Runnable)((Object)new queue$delaying_queue$fn__12149(object, delay_queue)));
        t.start();
        Object object2 = msec;
        msec = null;
        LinkedBlockingQueue linkedBlockingQueue = delay_queue;
        delay_queue = null;
        Thread thread2 = t;
        t = null;
        return new DelayingQueue(object2, linkedBlockingQueue, thread2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return queue$delaying_queue.invokeStatic(object3, object4);
    }
}


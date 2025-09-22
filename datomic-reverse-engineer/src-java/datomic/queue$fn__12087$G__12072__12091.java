/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue.BlockingConsumer;

public final class queue$fn__12087$G__12072__12091
extends AFunction {
    public Object invoke(Object gf__source__12088, Object gf__or_else__12089, Object gf__msec__12090) {
        Object object = gf__source__12088;
        gf__source__12088 = null;
        Object object2 = gf__or_else__12089;
        gf__or_else__12089 = null;
        Object object3 = gf__msec__12090;
        gf__msec__12090 = null;
        return ((BlockingConsumer)object).poll_b(object2, object3);
    }
}


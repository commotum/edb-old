/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.CountDownLatch;

public final class promise$settable_future$reify__10384$fn__10387
extends AFunction {
    Object lockee__5436__auto__;
    Object d;

    public promise$settable_future$reify__10384$fn__10387(Object object, Object object2) {
        this.lockee__5436__auto__ = object;
        this.d = object2;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Object invoke() {
        synchronized (this.lockee__5436__auto__) {
            ((CountDownLatch)this.d).countDown();
            Object var1_1 = null;
            return var1_1;
        }
    }
}


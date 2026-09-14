/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.BlockingQueue;

public final class queue$queue_seq$fill__12153
extends AFunction {
    Object q;

    public queue$queue_seq$fill__12153(Object object) {
        this.q = object;
    }

    public Object invoke(Object s) {
        Object object = s;
        s = null;
        ((BlockingQueue)this.q).put(object);
        return null;
    }
}


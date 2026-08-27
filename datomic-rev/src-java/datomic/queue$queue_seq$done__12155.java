/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.BlockingQueue;

public final class queue$queue_seq$done__12155
extends AFunction {
    Object q;

    public queue$queue_seq$done__12155(Object object) {
        this.q = object;
    }

    public Object invoke() {
        ((BlockingQueue)this.q).put(this.q);
        return null;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue.BlockingConsumer;

public final class queue$fn__12076$G__12070__12078
extends AFunction {
    public Object invoke(Object gf__source__12077) {
        Object object = gf__source__12077;
        gf__source__12077 = null;
        return ((BlockingConsumer)object).take();
    }
}


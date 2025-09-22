/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue.Consumer;

public final class queue$fn__12021$G__12017__12024
extends AFunction {
    public Object invoke(Object gf__source__12022, Object gf__or_else__12023) {
        Object object = gf__source__12022;
        gf__source__12022 = null;
        Object object2 = gf__or_else__12023;
        gf__or_else__12023 = null;
        return ((Consumer)object).poll_nb(object2);
    }
}


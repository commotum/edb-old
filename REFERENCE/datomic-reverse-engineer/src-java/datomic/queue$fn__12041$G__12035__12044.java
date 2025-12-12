/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue.BlockingProducer;

public final class queue$fn__12041$G__12035__12044
extends AFunction {
    public Object invoke(Object gf__sink__12042, Object gf__item__12043) {
        Object object = gf__sink__12042;
        gf__sink__12042 = null;
        Object object2 = gf__item__12043;
        gf__item__12043 = null;
        return ((BlockingProducer)object).put(object2);
    }
}


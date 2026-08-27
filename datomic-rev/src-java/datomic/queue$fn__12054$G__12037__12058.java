/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue.BlockingProducer;

public final class queue$fn__12054$G__12037__12058
extends AFunction {
    public Object invoke(Object gf__sink__12055, Object gf__item__12056, Object gf__msec__12057) {
        Object object = gf__sink__12055;
        gf__sink__12055 = null;
        Object object2 = gf__item__12056;
        gf__item__12056 = null;
        Object object3 = gf__msec__12057;
        gf__msec__12057 = null;
        return ((BlockingProducer)object).offer_b(object2, object3);
    }
}


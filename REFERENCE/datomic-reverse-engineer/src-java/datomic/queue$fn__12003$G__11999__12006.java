/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.queue.Producer;

public final class queue$fn__12003$G__11999__12006
extends AFunction {
    public Object invoke(Object gf__sink__12004, Object gf__item__12005) {
        Object object = gf__sink__12004;
        gf__sink__12004 = null;
        Object object2 = gf__item__12005;
        gf__item__12005 = null;
        return ((Producer)object).offer_nb(object2);
    }
}


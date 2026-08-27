/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.TWatcher;

public final class peer$fn__21445$G__21394__21448
extends AFunction {
    public Object invoke(Object gf_____21446, Object gf__t__21447) {
        Object object = gf_____21446;
        gf_____21446 = null;
        Object object2 = gf__t__21447;
        gf__t__21447 = null;
        return ((TWatcher)object).sync_t(object2);
    }
}


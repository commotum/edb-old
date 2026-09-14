/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.memcached.RecoveringClientImpl;

public final class memcached$fn__9971$G__9967__9973
extends AFunction {
    public Object invoke(Object gf_____9972) {
        Object object = gf_____9972;
        gf_____9972 = null;
        return ((RecoveringClientImpl)object).rc_reset_if_crashed();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.reconnector2.Reconnectable;

public final class reconnector2$fn__17117$G__17113__17119
extends AFunction {
    public Object invoke(Object gf_____17118) {
        Object object = gf_____17118;
        gf_____17118 = null;
        return ((Reconnectable)object).reconnect();
    }
}


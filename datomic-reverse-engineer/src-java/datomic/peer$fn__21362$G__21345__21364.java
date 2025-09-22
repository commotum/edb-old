/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.RemoteConnection;

public final class peer$fn__21362$G__21345__21364
extends AFunction {
    public Object invoke(Object gf_____21363) {
        Object object = gf_____21363;
        gf_____21363 = null;
        return ((RemoteConnection)object).get_olookup();
    }
}


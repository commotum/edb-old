/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.RemoteConnection;

public final class peer$fn__21351$G__21343__21353
extends AFunction {
    public Object invoke(Object gf_____21352) {
        Object object = gf_____21352;
        gf_____21352 = null;
        return ((RemoteConnection)object).get_cluster();
    }
}


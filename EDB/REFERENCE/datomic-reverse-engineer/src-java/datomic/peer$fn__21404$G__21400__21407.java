/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.TWatcher;

public final class peer$fn__21404$G__21400__21407
extends AFunction {
    public Object invoke(Object gf_____21405, Object gf__new_db__21406) {
        Object object = gf_____21405;
        gf_____21405 = null;
        Object object2 = gf__new_db__21406;
        gf__new_db__21406 = null;
        return ((TWatcher)object).release_pending_syncs(object2);
    }
}


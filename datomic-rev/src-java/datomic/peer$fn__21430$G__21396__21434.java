/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.TWatcher;

public final class peer$fn__21430$G__21396__21434
extends AFunction {
    public Object invoke(Object gf_____21431, Object gf__btype__21432, Object gf__t__21433) {
        Object object = gf_____21431;
        gf_____21431 = null;
        Object object2 = gf__btype__21432;
        gf__btype__21432 = null;
        Object object3 = gf__t__21433;
        gf__t__21433 = null;
        return ((TWatcher)object).sync_background_t(object2, object3);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.simple_kv.KV;

public final class simple_kv$fn__16686$G__16678__16690
extends AFunction {
    public Object invoke(Object gf_____16687, Object gf__key__16688, Object gf__val__16689) {
        Object object = gf_____16687;
        gf_____16687 = null;
        Object object2 = gf__key__16688;
        gf__key__16688 = null;
        Object object3 = gf__val__16689;
        gf__val__16689 = null;
        return ((KV)object).put(object2, object3);
    }
}


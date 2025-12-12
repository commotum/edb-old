/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.simple_kv.KV;

public final class simple_kv$fn__16701$G__16682__16704
extends AFunction {
    public Object invoke(Object gf_____16702, Object gf__key__16703) {
        Object object = gf_____16702;
        gf_____16702 = null;
        Object object2 = gf__key__16703;
        gf__key__16703 = null;
        return ((KV)object).delete(object2);
    }
}


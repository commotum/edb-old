/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.simple_kv.KV;

public final class simple_kv$fn__16714$G__16680__16717
extends AFunction {
    public Object invoke(Object gf_____16715, Object gf__key__16716) {
        Object object = gf_____16715;
        gf_____16715 = null;
        Object object2 = gf__key__16716;
        gf__key__16716 = null;
        return ((KV)object).get(object2);
    }
}


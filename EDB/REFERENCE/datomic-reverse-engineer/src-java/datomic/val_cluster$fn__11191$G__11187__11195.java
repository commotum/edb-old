/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.val_cluster.Impl;

public final class val_cluster$fn__11191$G__11187__11195
extends AFunction {
    public Object invoke(Object gf_____11192, Object gf__val_key__11193, Object gf__opts__11194) {
        Object object = gf_____11192;
        gf_____11192 = null;
        Object object2 = gf__val_key__11193;
        gf__val_key__11193 = null;
        Object object3 = gf__opts__11194;
        gf__opts__11194 = null;
        return ((Impl)object)._get(object2, object3);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.fs.Impl;

public final class fs$fn__21274$G__21268__21278
extends AFunction {
    public Object invoke(Object gf_____21275, Object gf__k__21276, Object gf__opts__21277) {
        Object object = gf_____21275;
        gf_____21275 = null;
        Object object2 = gf__k__21276;
        gf__k__21276 = null;
        Object object3 = gf__opts__21277;
        gf__opts__21277 = null;
        return ((Impl)object)._sync_get(object2, object3);
    }
}


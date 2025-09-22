/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.fs.Impl;

public final class fs$fn__21304$G__21270__21309
extends AFunction {
    public Object invoke(Object gf_____21305, Object gf__k__21306, Object gf__v__21307, Object gf__opts__21308) {
        Object object = gf_____21305;
        gf_____21305 = null;
        Object object2 = gf__k__21306;
        gf__k__21306 = null;
        Object object3 = gf__v__21307;
        gf__v__21307 = null;
        Object object4 = gf__opts__21308;
        gf__opts__21308 = null;
        return ((Impl)object)._sync_put(object2, object3, object4);
    }
}


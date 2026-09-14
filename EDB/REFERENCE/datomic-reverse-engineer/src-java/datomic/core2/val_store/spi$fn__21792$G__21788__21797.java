/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.spi.Put;

public final class spi$fn__21792$G__21788__21797
extends AFunction {
    public Object invoke(Object gf_____21793, Object gf__k__21794, Object gf__v__21795, Object gf__opts__21796) {
        Object object = gf_____21793;
        gf_____21793 = null;
        Object object2 = gf__k__21794;
        gf__k__21794 = null;
        Object object3 = gf__v__21795;
        gf__v__21795 = null;
        Object object4 = gf__opts__21796;
        gf__opts__21796 = null;
        return ((Put)object)._put(object2, object3, object4);
    }
}


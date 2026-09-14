/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.spi.Get;

public final class spi$fn__21814$G__21810__21818
extends AFunction {
    public Object invoke(Object gf_____21815, Object gf__k__21816, Object gf__opts__21817) {
        Object object = gf_____21815;
        gf_____21815 = null;
        Object object2 = gf__k__21816;
        gf__k__21816 = null;
        Object object3 = gf__opts__21817;
        gf__opts__21817 = null;
        return ((Get)object)._get(object2, object3);
    }
}


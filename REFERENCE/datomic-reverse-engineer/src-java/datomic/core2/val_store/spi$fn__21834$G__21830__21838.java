/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.spi.Delete;

public final class spi$fn__21834$G__21830__21838
extends AFunction {
    public Object invoke(Object gf_____21835, Object gf__k__21836, Object gf__opts__21837) {
        Object object = gf_____21835;
        gf_____21835 = null;
        Object object2 = gf__k__21836;
        gf__k__21836 = null;
        Object object3 = gf__opts__21837;
        gf__opts__21837 = null;
        return ((Delete)object)._delete(object2, object3);
    }
}


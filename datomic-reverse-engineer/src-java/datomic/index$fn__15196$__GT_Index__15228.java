/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.index.Index;

public final class index$fn__15196$__GT_Index__15228
extends AFunction {
    public Object invoke(Object lookup, Object cmpi, Object root, Object cached_count, Object order) {
        Object object = lookup;
        lookup = null;
        Object object2 = cmpi;
        cmpi = null;
        Object object3 = root;
        root = null;
        Object object4 = cached_count;
        cached_count = null;
        Object object5 = order;
        order = null;
        return new Index(object, object2, object3, object4, object5);
    }
}

